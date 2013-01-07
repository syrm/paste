package models

import java.sql.Timestamp
import java.util.Date
import org.squeryl.annotations.Column
import org.squeryl.Schema


case class User(
    @Column("usr_id")
    id: Option[Int],
    @Column("usr_name")
    name: String,
    @Column("usr_password")
    password: String,
    @Column("usr_salt")
    salt: String,
    @Column("usr_timestamp")
    timestamp: Timestamp = new Timestamp(new Date().getTime())
)