package models

import scala.slick.driver.ExtendedProfile
import slick.session.{ Session => SlickSession }
import controllers.Store

import Store.driver.simple._

case class Lexer(
    id: Option[Int],
    name: String
)


object Lexers extends Table[Lexer]("T_LEXER_LEX") {
    def id = column[Int]("lex_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("lex_name")
    def * = id.? ~ name <> (Lexer, Lexer.unapply _)
}