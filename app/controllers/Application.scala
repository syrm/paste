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
      inTransaction {
        val lexers = from(Lexers)(lexer =>
          select(lexer)
            orderBy (lexer.name)).toSeq
        Ok(views.html.application.index(flash, lexers))
      }
  }

  def paste = withOptionalUser { implicit user =>
    implicit request =>
      pasteForm.bindFromRequest.fold(
        formWithErrors => Redirect(routes.Application.index()).flashing("error" -> formWithErrors.errors(0).message),
        {
          case (name, lexerId, content) =>
            try {
              val id = java.util.UUID.randomUUID().toString().replaceAll("-", "")
              val userId = user match {
                case Some(user) => user.id
                case None => None
              }

              val futurString = scala.concurrent.Future { processHighlight(lexerId, content) }

              Async {
                futurString.orTimeout("Oops", 1000).map { eitherStringOrTimeout =>
                  eitherStringOrTimeout.fold(
                    contentProcessed => {
                      inTransaction {
                        Pastes.insert(new Paste(id, lexerId, userId, name, content, contentProcessed, request.remoteAddress.substring(0, request.remoteAddress.length.min(39))))
                      }
                      Redirect(routes.Application.show(id))
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

  def show(id: String) = withOptionalUser { implicit user =>
    implicit request =>
      inTransaction {
        join(Pastes, Users.leftOuter)((paste, user) =>
          where(paste.id === id)
            select (paste, user)
            on (paste.userId === user.get.id)).headOption match {
          case Some((paste, pasteUser)) => {
            Ok(views.html.application.show(paste, pasteUser))
          }
          case None => Ok(views.html.application.pasteNotFound())
        }
      }
  }

  def raw(id: String) = withOptionalUser { implicit user =>
    implicit request =>
      inTransaction {
        Pastes.where(_.id === id).headOption match {
          case Some(paste: Paste) => Ok(paste.content)
          case None => Ok(views.html.application.pasteNotFound())
        }
      }
  }

  def processHighlight(lexerId: Int, paste: String): String = {
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
      val pasteStream: InputStream = new ByteArrayInputStream(paste getBytes "UTF-8")
      ("pygmentize " + lexerOption + " -O " + this.pygmentOption + " -f html") #< pasteStream !!
    } catch {
      case e: Exception => "Error : " + e
    }
    content

  }

}