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

object UserController extends Controller with Secured {

  def me = withOptionalUser { implicit user =>
    implicit request =>
    Ok(views.html.user.pastes(Paste.getByUser(user.get)))
  }

}