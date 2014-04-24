package common

import play.api.mvc._
import play.api.libs.json._

import model.Account

trait AbstractSecurity {
  import play.api.mvc.Results._

  def Secured(action: Account => Request[AnyContent] => Result): Action[AnyContent]
  def SecuredAjax(action: Account => Request[AnyContent] => Result): Action[AnyContent]

  def SecuredJson[T <: Result](action: Account => JsValue => JsResult[T]): Action[AnyContent] = SecuredAjax { account => request =>
    request.body.asJson.map { json =>
      action(account)(json).recoverTotal {
        e => BadRequest("Bad json: " + JsError.toFlatJson(e))
      }
    }.getOrElse {
      BadRequest("Expected json")
    }
  }

  def require(permissionChecker: => Boolean)(action: => Result): Result = {
    if(permissionChecker) action
    else Forbidden("Permission required")
  }

}
