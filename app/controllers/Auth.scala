package controllers

import com.github.nremond.PBKDF2
import models._
import models.Database._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Session
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._


object Auth extends Controller with Secured {

    val loginForm = Form(
        tuple(
            "username" -> text.verifying(nonEmpty),
            "password" -> text.verifying(nonEmpty)
        ) verifying ("Invalid username or password", result => result match {
            case (username, password) => auth(username, password)
        })
    )


    val registerForm = Form(
        tuple(
            "username" -> text.verifying(nonEmpty, maxLength(20)),
            "password" -> text.verifying(nonEmpty, minLength(4))
        )
    )


    def login = Action { implicit request =>
        implicit val user = None
        Ok(views.html.auth.login(loginForm))
    }


    def authenticate = Action { implicit request =>
        implicit val user = None
        loginForm.bindFromRequest.fold(
            formWithErrors => BadRequest(views.html.auth.login(formWithErrors)),
            {
                case (username, password) => Redirect(routes.Application.index).withSession(Security.username -> username)
            }
        )
    }


    def logout = Action { implicit request =>
        implicit val user = None
        Redirect(routes.Application.index).withNewSession
    }


    def register = Action { implicit request =>
        implicit val user = None
        Ok(views.html.auth.register(registerForm))
    }


    def create = Action { implicit request =>
        implicit val user = None
        registerForm.bindFromRequest.fold(
            formWithErrors => BadRequest(views.html.auth.register(formWithErrors)),
            {
                case (username, password) => {
                    val salt            = java.util.UUID.randomUUID().toString().replaceAll("-", "")
                    val passwordCrypted = PBKDF2(password, salt, pbkdf2_iterations, pbkdf2_size)
                    inTransaction {
                        Users.insert(new User(None, username, passwordCrypted, salt))
                    }
                    Redirect(routes.Application.index).withSession(Security.username -> username)
                }
            }
        )
    }


}