package models

import anorm._
import anorm.SqlParser._
import java.sql.Timestamp
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Paste(
  id: Pk[Long],
  hash: String,
  parentId: Option[Long],
  lexerId: Long,
  userId: Option[Long],
  name: Option[String],
  content: String,
  contentProcessed: String,
  remoteIp: String,
  timestamp: Date
)

object Paste {

  // -- Parsers

  /**
   * Parse a Paste from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("t_paste_pas.pas_id") ~
    get[String]("t_paste_pas.pas_hash") ~
    get[Option[Long]]("t_paste_pas.pas_id_parent") ~
    get[Long]("t_paste_pas.lex_id") ~
    get[Option[Long]]("t_paste_pas.usr_id") ~
    get[Option[String]]("t_paste_pas.pas_name") ~
    get[String]("t_paste_pas.pas_content") ~
    get[String]("t_paste_pas.pas_content_processed") ~
    get[String]("t_paste_pas.pas_remote_ip") ~
    get[Date]("t_paste_pas.pas_timestamp") map {
        case id ~ hash ~ parentId ~lexerId ~ userId ~ name ~ content ~ contentProcessed ~ remoteIp ~ timestamp =>
          Paste(id, hash, parentId, lexerId, userId, name, content, contentProcessed, remoteIp, timestamp)
      }
  }

  val withUser = Paste.simple ~ (User.simple ?) map {
    case paste ~ user => (paste, user)
  }

  // -- Queries

  /**
   * Retrieve a Paste from hash.
   */
  def getByHash(hash: String): Option[(Paste, Option[User])] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select * from T_PASTE_PAS pas
          left join T_USER_USR usr ON (usr.usr_id = pas.usr_id)
          where pas_hash = {hash}
        """).on(
        'hash -> hash).as(Paste.withUser.singleOpt)
    }
  }

  /**
   * Create a Paste.
   */
  def create(paste: Paste): Paste = {
    DB.withConnection { implicit connection =>
      SQL("""
          insert into T_PASTE_PAS (
              pas_hash, lex_id, usr_id, pas_name, pas_content, pas_content_processed, pas_remote_ip
            ) values (
              {hash}, {lexerId}, {userId}, {name}, {content}, {contentProcessed}, {remoteIp}
            )
        """).on(
        'hash             -> paste.hash,
        'lexerId          -> paste.lexerId,
        'userId           -> paste.userId,
        'name             -> paste.name,
        'content          -> paste.content,
        'contentProcessed -> paste.contentProcessed,
        'remoteIp         -> paste.remoteIp
      ).executeUpdate()

      paste

    }
  }

}