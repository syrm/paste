package models

import org.squeryl.annotations.Column
import org.squeryl.Schema

case class Lexer(
  @Column("lex_id") id: Int,
  @Column("lex_name") name: String)