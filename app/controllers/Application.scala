package controllers

import java.io._
import models._
import models.Database._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Session
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.Files
import play.api.libs.Files._
import play.api.mvc._
import play.api.Play.current
import sys.process._


object Application extends Controller with Secured {

    val pygmentOption = "encoding=utf-8,linenos=inline,lineanchors=L,anchorlinenos=True,style=monokai"

    val pasteForm = Form(
        tuple(
            "lexer"   -> number,
            "content" -> text
        )
    )


    def index = withOptionalUser { implicit user => implicit request =>
        inTransaction {
            val lexers = from(Lexers)(lexer =>
                select(lexer)
                orderBy(lexer.name)
            ).toSeq
            Ok(views.html.index(lexers))
        }
    }


    def paste = withOptionalUser { implicit user => implicit request =>
        pasteForm.bindFromRequest.fold(
            errors => Redirect(routes.Application.index),
            {
                case(lexerId, content) =>
                    try {
                        val id = java.util.UUID.randomUUID().toString().replaceAll("-", "")
                        val userId = user match {
                            case Some(user) => user.id
                            case None => None
                        }

                        Files.writeFile(TemporaryFile(new File("/tmp/paste-" + id + ".txt")).file, content)
                        val futurString = scala.concurrent.Future { processHighlight(lexerId, "/tmp/paste-" + id + ".txt") }

                        Async {
                            futurString.orTimeout("Oops", 1000).map { eitherStringOrTimeout =>
                                eitherStringOrTimeout.fold(
                                    contentProcessed => {
                                        inTransaction {
                                            Pastes.insert(new Paste(id, lexerId, userId, content, contentProcessed, request.remoteAddress.substring(0, request.remoteAddress.length.min(39))))
                                        }
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


    def show(id: String) = withOptionalUser { implicit user => implicit request =>
        inTransaction {
           join(Pastes, Users.leftOuter)((paste, user) =>
                where(paste.id === id)
                select(paste, user)
                on(paste.userId === user.get.id)
            ).headOption match {
                case Some((paste, pasteUser)) => {
                    Ok(views.html.show(paste, pasteUser))
                }
                case None => Ok(views.html.pasteNotFound())
            }
        }
    }


    def raw(id: String) = withOptionalUser { implicit user => implicit request =>
        inTransaction {
            Pastes.where(_.id === id).headOption match {
                case Some(paste: Paste) => Ok(paste.content)
                case None => Ok(views.html.pasteNotFound())
            }
        }
    }


    def processHighlight(lexerId: Int, file: String): String = {
        val lexerName = inTransaction {
            Lexers.where(_.id === lexerId).headOption match {
                case Some(lexer: Lexer) => lexer.name
                case None => "auto"
           }
        }

        val lexerOption = if (lexerName == "auto") {
            "-g"
        } else {
            "-l " + lexerName
        }

        val content = try {
            ("pygmentize " + lexerOption + " -O " + this.pygmentOption + " -f html " + file).!!
        } catch {
            case e: Exception => "Error : " + e
        }
        content

    }


}