package model

import anorm._
import play.api.libs.json.Json

case class EntryGroup(
  id: Pk[Long],
  ownerId: Long,
  name: String
)

object EntryGroup{
  val formatter = Json.format[EntryGroup]
}