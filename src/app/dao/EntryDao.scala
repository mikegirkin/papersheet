package dao

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime

import common.AnormExtension._
import model._

trait EntryDao {
  def insert(entry: Entry): Entry
  def getById(id: Long): Option[Entry]
  def listForUser(accountId: Long): Seq[Entry]
  def update(entry: Entry): Entry
}

class PsqlEntryDao extends EntryDao with SqlHelpers {

  val parser =
    get[Pk[Long]]("id") ~
    long("creatorId") ~
    long("stateId") ~
    get[DateTime]("created") ~
    str("content") map { case id ~ creatorId ~ stateId ~ created ~ content => Entry(id, creatorId, stateId, created, content)}

  val fields = Seq("id", "creatorId", "stateId", "created", "content")
  val allFields = fields.mkString(", ")

  def insert(entry: Entry): Entry = {
    val key = I("""
      insert into Entry
        (creatorId, stateId, created, content)
      values
        ({creatorId}, {stateId}, {created}, {content})
    """.stripMargin)(
      'creatorId -> entry.creatorId,
      'stateId -> entry.stateId,
      'created -> entry.created,
      'content -> entry.content
    )
    entry.copy(id = Id(key))
  }

  def getById(id: Long): Option[Entry] =
    Q(s"""
      select $allFields
      from Entry
      where id = {id}
    """.stripMargin)(
      'id -> id
    )(
      parser.singleOpt
    )

  def listForUser(accountId: Long): Seq[Entry] =
    Q(s"""
      select $allFields
      from Entry
      where creatorId = {creatorId}
    """.stripMargin)(
      'creatorId -> accountId
    )(
      parser.*
    )

  def update(entry: Entry): Entry = {
    U("""
      update set
        stateId = {stateId},
        content = {content}
    """.stripMargin)(
      'stateId -> entry.stateId,
      'content -> entry.content
    )
    entry
  }

}
