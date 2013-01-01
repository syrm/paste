package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Promise
import play.api.Play.current

import java.util.regex.Pattern
import scalax.file.Path
import scalax.file.PathMatcher._
import sys.process._

import models.Lexer


object Application extends Controller {

    val source        = "data/raw/"
    val processed     = "data/processed/"
    val pygmentOption = "linenos=inline,lineanchors=L,anchorlinenos=True,style=monokai"

    val pasteForm = Form(
        tuple(
            "lexer"   -> text.verifying(pattern("""^[a-zA-Z-_]+$""".r)),
            "content" -> text
        )
    )


    def index = Action {
        Ok(views.html.index(Lexer.all))
    }


    def paste = Action { implicit request =>
        pasteForm.bindFromRequest.fold(
            errors => Redirect(routes.Application.index),
            {
                case(lexer, content) =>
                    val id = java.util.UUID.randomUUID().toString().replaceAll("-", "")
                    val name = id + "." + lexer + ".source"
                    Path.fromString(this.source + name).write(content)

                    Redirect(routes.Application.show(id))
            }
        )

    }


    def show(id: String) = Action {

        Path.fromString(this.processed + id + ".html") match {
            case Exists(file) => Ok(views.html.show(id, file.lines().mkString("\r\n")))
            case _ =>
                val promiseOfString: Promise[String] = Akka.future { processHighlight(id) }

                Async {
                    promiseOfString.orTimeout("Oops", 1000).map { eitherStringOrTimeout =>
                        eitherStringOrTimeout.fold(
                            content => Ok(views.html.show(id, content)),
                            timeout => InternalServerError(timeout)
                        )
                    }
                }
        }

    }


    def raw(id: String) = Action {

        (Path.fromString(this.source) ** (id + ".*.source")).toList match {
            case List(file) => Ok(file.lines().mkString("\r\n"))
            case _ => Ok(views.html.show("", "Paste not found"))
        }

    }


    def processHighlight(paste: String): String = {
        (Path.fromString(this.source) ** (paste + ".*.source")).toList match {
            case List(file) => {
                val lexer = file.toString().split("\\.")(1)
                val lexerOption = if (lexer == "auto") {
                    "-g"
                } else {
                    "-l " + lexer
                }

                ("pygmentize " + lexerOption + " -O " + this.pygmentOption + " -f html -o " + this.processed + paste + ".html " + file.path).!

                val content = postProcess(Path.fromString(this.processed + paste + ".html").lines().mkString("\r\n"))
                Path.fromString(this.processed + paste + ".html").write(content)
                content

            }
            case _ => "Paste not found"
        }

    }


    def postProcess(content: String): String = {

        content
            .replaceAll(Pattern.quote("<title></title>\n"), "")
            .replaceAll(Pattern.quote("<h2></h2>\n\n"), "")

    }

}