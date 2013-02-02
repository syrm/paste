package controllers

import anorm._
import java.io._
import java.util.Date
import models._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.Play.current
import scala.language.postfixOps
import sys.process._

object Application extends Controller with Secured {

  val pygmentOption = "encoding=utf-8,linenos=inline,lineanchors=L,anchorlinenos=True,style=monokai"

  val pasteForm = Form(
    tuple(
      "name" -> optional(text.verifying("Maximum length of name is 60", _.length < 61)),
      "lexer" -> number,
      "content" -> text.verifying("Paste cannot be empty", _.nonEmpty)))

  def index = withOptionalUser { implicit user =>
    implicit request =>
      Ok(views.html.application.index(flash, Lexer.getAll))
  }

  def paste = withOptionalUser { implicit user =>
    implicit request =>
      pasteForm.bindFromRequest.fold(
        formWithErrors => Redirect(routes.Application.index()).flashing("error" -> formWithErrors.errors(0).message),
        {
          case (name, lexerId, content) =>
            try {
              val hash = java.util.UUID.randomUUID().toString().replaceAll("-", "")
              val userId = user match {
                case Some(user) => Some(user.id.get)
                case None       => None
              }

              val futurString = scala.concurrent.Future { processHighlight(lexerId, content) }

              Async {
                futurString.orTimeout("Oops", 1000).map { eitherStringOrTimeout =>
                  eitherStringOrTimeout.fold(
                    contentProcessed => {
                      Paste.create(
                        new Paste(
                          NotAssigned,
                          hash,
                          None,
                          lexerId,
                          userId,
                          name,
                          content,
                          contentProcessed,
                          request.remoteAddress.substring(0, request.remoteAddress.length.min(39)),
                          new Date))
                      Redirect(routes.Application.show(hash))
                    },
                    timeout => InternalServerError(timeout))
                }
              }

            } catch {
              case e: Exception =>
                Logger.error(e.getMessage())
                Redirect(routes.Application.index())
            }
        })

  }

  def show(hash: String) = withOptionalUser { implicit user =>
    implicit request =>

      Paste.getByHash(hash) match {
        case Some((paste, owner)) => Ok(views.html.application.show(paste, owner))
        case None                 => Ok(views.html.application.pasteNotFound())
      }
  }

  def raw(hash: String) = withOptionalUser { implicit user =>
    implicit request =>
      Paste.getByHash(hash) match {
        case Some((paste, owner)) => Ok(paste.content)
        case None                 => Ok(views.html.application.pasteNotFound())
      }
  }

  def processHighlight(lexerId: Int, paste: String): String = {
    val lexerName = Lexer.getById(lexerId) match {
        case Some(lexer: Lexer) => lexer.name
        case None               => "auto"
      }

    val lexerOption = if (lexerName == "auto") {
      "-g"
    } else {
      "-l " + lexerName
    }

    val content = try {
      val pasteStream: InputStream = new ByteArrayInputStream(paste getBytes "UTF-8")
      ("pygmentize " + lexerOption + " -O " + this.pygmentOption + " -f html") #< pasteStream !!
    } catch {
      case e: Exception => "Error : " + e
    }
    content

  }

}