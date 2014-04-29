package model

import anorm._
import play.api.libs.json.Json

import common.JsonFormatHelpers._

case class EntryGroup(
  id: Pk[Long],
  ownerId: Long,
  name: String
) {
  def this(ownerId: Long, name: String) =
    this(NotAssigned, ownerId, name)
}

object EntryGroup {
  implicit val formatter = Json.format[EntryGroup]
}