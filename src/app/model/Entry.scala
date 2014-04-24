package model

import anorm._
import org.joda.time.DateTime
import play.api.libs.json.Json

import common.JsonFormatHelpers._

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

object Entry {

  implicit val formatter = Json.format[Entry]
}
