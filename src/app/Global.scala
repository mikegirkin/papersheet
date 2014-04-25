import anorm.{NotAssigned, Id}
import play.api._
import securesocial.core._

import dao._
import model._

object Global extends GlobalSettings {

  object PsqlDao extends PsqlDaoProvider

  override def onStart(app: Application) = {
    if(PsqlDao.entryStateDao.getById(1).isEmpty) insertInitialData
  }

  private def insertInitialData = {

    AccountDao.create(
      AccountDao.newUserpassAccount("mike", "mike_girkin@mail.ru", "Mike", "Girkin", "1")
    )

    val acc = AccountDao.findByIdentityId(IdentityId("mike", "userpass")).get
    val accId = acc.id.get

    val entryStates = Seq(
      EntryState(Id(1), "Active"),
      EntryState(Id(2), "Closed")
    ).map {
      s => PsqlDao.entryStateDao.insert(s)
    }

    val entryGroups = Seq(
      EntryGroup(NotAssigned, accId, "Group 1"),
      EntryGroup(NotAssigned, accId, "Group 2")
    ).map {
      g => PsqlDao.entryGroupDao.insert(g)
    }

    val entries = Seq(
      new Entry(acc.id.get, 1, "Entry 1"),
      new Entry(acc.id.get, 1, "Entry 2"),
      new Entry(acc.id.get, 1, "Entry 3"),
      new Entry(acc.id.get, 2, "Closed entry 1"),
      new Entry(acc.id.get, 2, "Closed entry 2")
    ).map {
      e => PsqlDao.entryDao.insert(e)
    }

  }
}