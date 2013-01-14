package controllers

import com.github.nremond.PBKDF2
import models._
import models.Database._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Session
import play.api._
import play.api.db.DB
import play.api.mvc._
import play.api.Play.current

trait Secured {

  val pbkdf2_iterations = 10000
  val pbkdf2_size = 16

  def username(request: RequestHeader): Option[String] = request.session.get(Security.username)

  def user(username: Option[String]): Option[User] = {
    inTransaction {
      username match {
        case Some(username) => Users.where(user => user.name === username).headOption
        case None => None
      }
    }
  }

  def auth(username: String, password: String): Boolean = {
    user(Some(username)) match {
      case Some(user: User) => PBKDF2(password, user.salt, pbkdf2_iterations, pbkdf2_size) match {
        case user.password => true
        case _ => false
      }
      case None => false
    }
  }

  def onUnauthorized(request: RequestHeader) = Results.Redirect("routes.Auth.register")

  def withUser(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withOptionalUser(f: => Option[User] => Request[AnyContent] => Result) = {
    Action(request => f(user(username(request)))(request))
  }

}