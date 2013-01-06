package models

import java.util.Date
import java.sql.Timestamp
import scala.slick.driver.ExtendedProfile
import slick.session.{ Session => SlickSession }
import controllers.Store

import Store.driver.simple._

case class User(
    id: Option[Int],
    name: String,
    password: String,
    salt: String,
    timestamp: Timestamp = new Timestamp(new Date().getTime())
)


object Users extends Table[User]("T_USER_USR") {
    def id = column[Int]("usr_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("usr_name")
    def password = column[String]("usr_password")
    def salt = column[String]("usr_salt")
    def timestamp = column[Timestamp]("usr_timestamp")
    def * = id.? ~ name ~ password ~ salt ~ timestamp <> (User, User.unapply _)
}