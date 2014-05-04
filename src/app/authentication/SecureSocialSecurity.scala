package authentication

import play.api.mvc._

import model._

trait SecureSocialSecurity extends AbstractSecurity with securesocial.core.SecureSocial {

  def Secured(action: Account => Request[AnyContent] => Result): Action[AnyContent] = SecuredAction { request =>
    action(request.user.asInstanceOf[Account])(request)
  }

  def SecuredAjax(action: Account => Request[AnyContent] => Result): Action[AnyContent] = UserAwareAction { request =>
    request.user.map {
      u => action(u.asInstanceOf[Account])(request)
    }.getOrElse {
      Forbidden
    }
  }

}
