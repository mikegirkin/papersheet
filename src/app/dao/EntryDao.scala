package dao

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import org.joda.time.DateTime


import common.AnormExtension._
import model._

trait EntryDao {
  def insert(entry: Entry): Entry
  def getById(id: Long): Option[Entry]
  def listForUser(accountId: Long): Seq[Entry]
  def update(entry: Entry): Entry
}

class PsqlEntryDao extends EntryDao {

  val parser =
    get[Pk[Long]]("id") ~
    long("creatorId") ~
    long("stateId") ~
    get[DateTime]("created") ~
    str("content") map { case id ~ creatorId ~ stateId ~ created ~ content => Entry(id, creatorId, stateId, created, content)}

  def insert(entry: Entry): Entry = ???

  def getById(id: Long): Option[Entry] = ???

  def listForUser(accountId: Long): Seq[Entry] = ???

  def update(entry: Entry): Entry = ???
}
