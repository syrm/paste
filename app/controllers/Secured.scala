package controllers

import play.api._
import play.api.db.DB
import play.api.Play.current
import play.api.mvc._
import Store.driver.simple._
import com.github.nremond.PBKDF2
import models._


trait Secured extends Persistent {
    implicit val session = db.createSession

    val pbkdf2_iterations = 10000
    val pbkdf2_size =  16

    def username(request: RequestHeader): Option[String] = request.session.get(Security.username)

    def user(username: Option[String]): Option[User] = Query(Users).filter(_.name is username).firstOption

    def auth(username: String, password: String): Boolean = {
        user(Some(username)) match {
            case Some(user: User) => PBKDF2(password, user.salt, pbkdf2_iterations, pbkdf2_size) match {
                case user.password => true
                case _ => false
            }
            case None => false
        }
    }


    def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.register)

    def withUser(f: => String => Request[AnyContent] => Result) = {
        Security.Authenticated(username, onUnauthorized) { user =>
            Action(request => f(user)(request))
        }
    }

    def withOptionalUser(f: => Option[User] => Request[AnyContent] => Result) = {
        Action(request => f(user(username(request)))(request))
    }

}


/*

trait Secured2 {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.User.signin)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  /**
   * This method shows how you could wrap the withAuth method to also fetch your user
   * You will need to implement UserDAO.findOneByUsername
   */
  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    UserDAO.findOneByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}
*/