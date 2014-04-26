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
  val groupId: Option[Long]

  def anormParams(): AnormParameters = {
    val params =
      groupId.map { id =>
        Seq(("groupId = {groupId}", 'groupId -> toParameterValue(id)))
      }.getOrElse(
        Seq()
      ).unzip(x => (x._1, x._2))
    AnormParameters(params._1, params._2)
  }
}

object NoParams extends EntryQueryParams {
  val groupId: Option[Long] = None
}

object EntryQueryParams {
  def apply(groupId: Option[Long]) = {
    val gid = groupId

    new EntryQueryParams {
      val groupId: Option[Long] = gid
    }
  }
}

trait EntryDao {
  def insert(entry: Entry): Entry
  def getById(id: Long): Option[Entry]
  def listForUser(accountId: Long, params: EntryQueryParams): Seq[Entry]
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

  def listForUser(accountId: Long, params: EntryQueryParams): Seq[Entry] ={
    val anormParams = params.anormParams()

    val whereString = (anormParams.whereParts :+ "creatorId = {creatorId}").mkString(" and ")
    Q(s"""
      select $allFields
      from Entry
      where
         $whereString
      order by id
    """)(
      (anormParams.parameterParts :+ ('creatorId -> toParameterValue(accountId))):_*
    )(
      parser.*
    )
  }

  def update(entry: Entry): Entry = {
    U("""
      update Entry set
        stateId = {stateId},
        groupId = {groupId},
        content = {content}
      where id = {id}
      """)(
      'stateId -> entry.stateId,
      'groupId -> entry.groupId,
      'content -> entry.content,
      'id -> entry.id
    )
    entry
  }

}
