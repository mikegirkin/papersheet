package dao

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime

import common.AnormExtension._
import model._

trait EntryGroupDao {
  def insert(group: EntryGroup): EntryGroup
  def listForUser(account: Account): Seq[EntryGroup]
  def getById(id: Long): Option[EntryGroup]
}

class PsqlEntryGroupDao extends EntryGroupDao with SqlHelpers {

  val allFields = "id, ownerId, name"

  val parser =
    get[Pk[Long]]("id") ~
    long("ownerId") ~
    str("name") map {
      case id ~ ownerId ~ name => EntryGroup(id, ownerId, name)
    }

  def insert(group: EntryGroup): EntryGroup = {
    val key = I[Long]("""
      insert into EntryGroup
        (ownerId, name)
      values
        ({ownerId}, {name})
    """)(
      'ownerId -> group.ownerId,
      'name -> group.name
    )
    group.copy(id = Id(key))
  }

  def listForUser(account: Account): Seq[EntryGroup] =
    Q(s"""
      select $allFields
      from EntryGroup
      where
        ownerId = {ownerId}
    """)(
      'ownerId -> account.id.get
    )(
      parser.*
    )

  def getById(id: Long): Option[EntryGroup] =
  Q(s"""
    select $allFields
    from EntryGroup
    where
      id = {id}
  """)(
    'id -> id
  )(
    parser.singleOpt
  )
}
