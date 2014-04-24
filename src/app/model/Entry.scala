package model

import anorm.{NotAssigned, Pk}
import org.joda.time.DateTime

case class Entry(
  id: Pk[Long],
  creatorId: Long,
  stateId: Long,
  created: DateTime,
  content: String
) {
  def this(creatorId: Long, stateId: Long, content: String) =
    this(NotAssigned, creatorId, stateId, DateTime.now(), content)
}
