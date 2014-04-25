package model

import anorm._

case class EntryGroup(
  id: Pk[Long],
  ownerId: Long,
  name: String
)
