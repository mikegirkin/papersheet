package dao

trait DaoProvider {
  val entryDao: EntryDao
  val entryStateDao: EntryStateDao
  val accountDao: IAccountDao
  val entryGroupDao: EntryGroupDao
}

trait PsqlDaoProvider extends DaoProvider {
  object PsqlEntryDaoImpl extends PsqlEntryDao
  object PsqlEntryStateDaoImpl extends PsqlEntryStateDao
  object PsqlEntryGroupDaoImpl extends PsqlEntryGroupDao

  val entryDao = PsqlEntryDaoImpl
  val entryStateDao = PsqlEntryStateDaoImpl
  val accountDao = AccountDao
  val entryGroupDao = PsqlEntryGroupDaoImpl
}
