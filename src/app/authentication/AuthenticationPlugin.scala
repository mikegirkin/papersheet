package authentication

import play._
import securesocial.controllers.TemplatesPlugin
import securesocial.core.{SecuredRequest, Identity}
import play.api.mvc.{Request, RequestHeader}
import play.api.templates.{Html, Txt}
import play.api.data.Form
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo

class AuthenticationPlugin(application: Application) extends TemplatesPlugin {

  def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String]): Html = ???

  def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = ???

  def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = ???

  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = ???

  def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = ???

  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = ???

  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = ???

  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = ???

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = ???

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = ???

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = ???

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = ???

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = ???
}
