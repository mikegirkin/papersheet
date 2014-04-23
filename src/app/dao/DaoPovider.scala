package dao

trait DaoProvider {
  val entryDao: EntryDao
  val entryStateDao: EntryStateDao
  val accountDao: IAccountDao
}

trait PsqlDao extends DaoProvider {
  object PsqlEntryDaoImpl extends PsqlEntryDao
  object PsqlEntryStateDaoImpl extends PsqlEntryStateDao

  val entryDao = PsqlEntryDaoImpl
  val entryStateDao = PsqlEntryStateDaoImpl
  val accountDao = AccountDao
}
