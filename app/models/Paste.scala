package models

import java.sql.Timestamp
import java.util.Date
import org.squeryl.annotations.Column
import org.squeryl.Schema


case class Paste(
    @Column("pas_id")
    id: String,
    @Column("lex_id")
    lexerId: Int,
    @Column("usr_id")
    userId: Option[Int],
    @Column("pas_name")
    name: Option[String],
    @Column("pas_content")
    content: String,
    @Column("pas_content_processed")
    contentProcessed: String,
    @Column("pas_remote_ip")
    remoteIp: String,
    @Column("pas_timestamp")
    timestamp: Timestamp = new Timestamp(new Date().getTime())
)