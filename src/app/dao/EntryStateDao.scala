package dao

import anorm._
import anorm.SqlParser._

import model.EntryState

trait EntryStateDao {
  def insert(state: EntryState) : EntryState
  def getById(id: Long): Option[EntryState]
}

class PsqlEntryStateDao extends EntryStateDao with SqlHelpers {

  val parser =
    get[Pk[Long]]("id") ~
    str("name") map { case id ~ name => EntryState(id, name) }

  def insert(state: EntryState): EntryState = {
    I[Long]("""
      insert into EntryState
        (id, name)
      values
        ({id}, {name})
    """.stripMargin)(
      'id -> state.id,
      'name -> state.name
    )
    state
  }

  def getById(id: Long): Option[EntryState] =
    Q("""
      select id, name
      from EntryState
      where id = {id}
    """.stripMargin)(
      'id -> id
    )(
      parser.singleOpt
    )

}
