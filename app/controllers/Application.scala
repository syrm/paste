package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.Files
import play.api.libs.Files._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import java.io._
import sys.process._
import Store.driver.simple._
import models._


object Application extends Controller with Persistent {

    implicit val session = db.createSession

    val pygmentOption = "linenos=inline,lineanchors=L,anchorlinenos=True,style=monokai"

    val pasteForm = Form(
        tuple(
            "lexer"   -> number,
            "content" -> text
        )
    )


    def index = TransAction { request =>
        Ok(views.html.index(Query(Lexers).list))
    }


    def paste = TransAction { implicit request =>
        pasteForm.bindFromRequest.fold(
            errors => Redirect(routes.Application.index),
            {
                case(lexerId, content) =>
                    try {
                        val id = java.util.UUID.randomUUID().toString().replaceAll("-", "")
                        Files.writeFile(TemporaryFile(new File("/tmp/paste-" + id + ".txt")).file, content)
                        val futurString = scala.concurrent.Future { processHighlight(lexerId, "/tmp/paste-" + id + ".txt") }

                        Async {
                            futurString.orTimeout("Oops", 1000).map { eitherStringOrTimeout =>
                                eitherStringOrTimeout.fold(
                                    contentProcessed => {
                                        Pastes.insert(Paste(id, lexerId, content, contentProcessed))
                                        Redirect(routes.Application.show(id))
                                    },
                                    timeout => InternalServerError(timeout)
                                )
                            }
                        }

                    } catch {
                        case e: Exception =>
                        Logger.error(e.getMessage())
                        Redirect(routes.Application.index())
                    }
            }
        )

    }


    def show(id: String) = TransAction { request =>

        Query(Pastes).filter(_.id is id).firstOption match {
            case Some(paste: Paste) => Ok(views.html.show(paste.id, paste.contentProcessed))
            case None => Ok(views.html.show("", "Paste not found"))
        }

    }


    def raw(id: String) = TransAction { request =>

        Query(Pastes).filter(_.id is id).firstOption match {
            case Some(paste: Paste) => Ok(paste.content)
            case None => Ok(views.html.show("", "Paste not found"))
        }

    }


    def processHighlight(lexerId: Int, file: String): String = {

        val lexerName = Query(Lexers).filter(_.id is lexerId).firstOption match {
            case Some(lexer: Lexer) => lexer.name
            case None => "auto"
        }

        val lexerOption = if (lexerName == "auto") {
            "-g"
        } else {
            "-l " + lexerName
        }

        val content = ("pygmentize " + lexerOption + " -O " + this.pygmentOption + " -f html " + file).!!
        content

    }


}