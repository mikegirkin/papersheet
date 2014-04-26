package dao

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime

import common.AnormExtension._
import model._

case class AnormParameters(
  whereParts: Seq[String],
  parameterParts: Seq[(Symbol, ParameterValue[_])]
)

trait EntryQueryParams {
  val categoryId: Option[Long]
}

object NoParams extends EntryQueryParams {
  val categoryId: Option[Long] = None

  def anormParams(): (Seq[String], Seq[(Symbol, ParameterValue[_])]) = {
    categoryId.map { id =>
      Seq(("categoryId = {categoryId}", 'categoryId -> toParameterValue(id)))
    }.getOrElse(
      Seq()
    ).unzip()
  }
}

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
    long("groupId") ~
    get[DateTime]("created") ~
    str("content") map {
      case id ~ creatorId ~ stateId ~ groupId ~ created ~ content =>
        Entry(id, creatorId, stateId, groupId, created, content)
    }

  val fields = Seq("id", "creatorId", "stateId", "groupId", "created", "content")
  val allFields = fields.mkString(", ")

  def insert(entry: Entry): Entry = {
    val key = I[Long]("""
      insert into Entry
        (creatorId, stateId, groupId, created, content)
      values
        ({creatorId}, {stateId}, {groupId}, {created}, {content})
    """)(
      'creatorId -> entry.creatorId,
      'stateId -> entry.stateId,
      'groupId -> entry.groupId,
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
    """)(
      'id -> id
    )(
      parser.singleOpt
    )

  def listForUser(accountId: Long, params: EntryQueryParams): Seq[Entry] =
    Q(s"""
      select $allFields
      from Entry
      where
        creatorId = {creatorId}
    """)(
      'creatorId -> accountId
    )(
      parser.*
    )

  def update(entry: Entry): Entry = {
    U("""
      update set
        stateId = {stateId},
        groupId = {groupId},
        content = {content}
      """)(
      'stateId -> entry.stateId,
      'content -> entry.content
    )
    entry
  }

}
