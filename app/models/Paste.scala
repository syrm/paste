package models

import java.util.Date
import java.sql.Timestamp
import scala.slick.driver.ExtendedProfile
import slick.session.{ Session => SlickSession }
import controllers.Store

import Store.driver.simple._

case class Paste(
    id: String,
    lexerId: Int,
    content: String,
    contentProcessed: String,
    timestamp: Timestamp = new Timestamp(new Date().getTime())
)


object Pastes extends Table[Paste]("T_PASTE_PAS") {
    def id = column[String]("pas_id", O.PrimaryKey)
    def lexerId = column[Int]("lex_id")
    def content = column[String]("pas_content")
    def contentProcessed = column[String]("pas_content_processed")
    def timestamp = column[Timestamp]("pas_timestamp")
    def * = id ~ lexerId ~ content ~ contentProcessed ~ timestamp <> (Paste, Paste.unapply _)

    def lexer = foreignKey("lexer_fk", lexerId, Lexers)(_.id)
}