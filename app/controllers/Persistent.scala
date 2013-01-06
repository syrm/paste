package controllers

import play.api.mvc._
import play.api.db.DB
import play.api.Play._
import play.Configuration
import slick.session.Database
import slick.session.{ Session => SlickSession }
import slick.driver.ExtendedProfile

object Store {
    lazy val driver = Configuration.root().getString("db.default.driver") match {
        case "org.h2.Driver" => scala.slick.driver.H2Driver
        case "com.mysql.jdbc.Driver" => scala.slick.driver.MySQLDriver
    }
}

trait Persistent {

    lazy val db = Database.forDataSource(DB.getDataSource())

}