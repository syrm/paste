package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class User(
  id:        Pk[Long],
  name:      String,
  password:  String,
  salt:      String,
  timestamp: Date = new Date
)

object User {

  // -- Parsers

  /**
   * Parse an User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("t_user_usr.usr_id") ~
    get[String]("t_user_usr.usr_name") ~
    get[String]("t_user_usr.usr_password") ~
    get[String]("t_user_usr.usr_salt") ~
    get[Date]("t_user_usr.usr_timestamp") map {
      case id~name~password~salt~timestamp => User(id, name, password, salt, timestamp)
    }
  }

  // -- Queries

  /**
   * Retrieve an User from name.
   */
  def getByName(name: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select * from T_USER_USR
          where usr_name = {name}
        """).on(
        'name -> name).as(User.simple.singleOpt)
    }
  }

  /**
   * Create an User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL("""
          insert into T_USER_USR (
              usr_name, usr_password, usr_salt, usr_timestamp
            ) values (
              {name}, {password}, {salt}, {timestamp}
            )
        """).on(
        'name      -> user.name,
        'password  -> user.password,
        'salt      -> user.salt,
        'timestamp -> user.timestamp).executeUpdate()

      user
    }
  }

}