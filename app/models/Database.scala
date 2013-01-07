package models

import org.squeryl.Schema


object Database extends Schema {
    val Lexers = table[Lexer]("T_LEXER_LEX")
    val Users = table[User]("T_USER_USR")
    val Pastes = table[Paste]("T_PASTE_PAS")
}