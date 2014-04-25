package model

import anorm._
import play.api.libs.json.Json

import common.JsonFormatHelpers._

case class EntryGroup(
  id: Pk[Long],
  ownerId: Long,
  name: String
)

object EntryGroup {
  implicit val formatter = Json.format[EntryGroup]
}