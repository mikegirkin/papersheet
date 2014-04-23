package model

import anorm.Pk
import org.joda.time.DateTime

case class Entry(
  id: Pk[Long],
  creatorId: Long,
  stateId: Long,
  created: DateTime,
  content: String
)
