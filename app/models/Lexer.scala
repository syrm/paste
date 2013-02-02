package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Lexer(
  id: Pk[Long],
  name: String
)

object Lexer {

  // -- Parsers

  /**
   * Parse a Lexer from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("t_lexer_lex.lex_id") ~
    get[String]("t_lexer_lex.lex_name") map {
      case id~name => Lexer(id, name)
    }
  }

  // -- Queries

  /**
   * Retrieve all Lexer
   */
  def getAll: Seq[Lexer] = {
    DB.withConnection { implicit connection =>
      SQL("select * from T_LEXER_LEX").as(Lexer.simple *)
    }
  }

  /**
   * Retrieve a Lexer from id.
   */
  def getById(id: Long): Option[Lexer] = {
    DB.withConnection { implicit connection =>
      SQL("select * from T_LEXER_LEX where lex_id = {id}").on(
        'id -> id
      ).as(Lexer.simple.singleOpt)
    }
  }

}