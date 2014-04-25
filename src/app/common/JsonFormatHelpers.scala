package common

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError
import play.api.data._
import anorm._
import org.joda.time._

object JsonFormatHelpers {

	implicit object PkFormat extends Format[Pk[Long]] {
    def reads(json: JsValue): JsResult[Pk[Long]] = JsSuccess (
        json.asOpt[Long].map(id => Id(id)).getOrElse(NotAssigned)
    )
    def writes(id: Pk[Long]): JsValue = id.map(JsNumber(_)).getOrElse(JsNull)
	}

  implicit object jodaDateTimeFormat extends Format[DateTime] {
    import org.joda.time.DateTime

    val df = org.joda.time.format.ISODateTimeFormat.dateTime()

    def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsNumber(d) => JsSuccess(new DateTime(d.toLong))
      case JsString(s) => parseDate(s) match {
        case Some(d) => JsSuccess(d)
        case None => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.jodadate.format", json))))
      }
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.date"))))
    }

    private def parseDate(input: String): Option[DateTime] =
      scala.util.control.Exception.allCatch[DateTime] opt (DateTime.parse(input, df))

    def writes(d: org.joda.time.DateTime): JsValue = JsString(d.toString())
  }

  implicit object FormErrorWrites extends Writes[FormError] {
    def writes(error: FormError) = Json.toJson(
      Map(
        "id" -> Json.toJson(error.key.replace('.', '_')),
        "message" -> Json.toJson(error.message)
      )
    )
  }
}