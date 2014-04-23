package model

import anorm.Pk

case class EntryState(
  id: Pk[Long],
  name: String
)

