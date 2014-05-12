package controllers

import play.api._
import play.api.mvc._
import securesocial.core.SecureSocial

object Application extends Controller with SecureSocial {

  def index = SecuredAction {
    Ok(views.html.main())
  }

  def login = Action {
    Ok(views.html.authentication.login())
  }

  def postLogin = Action { request =>
    ???
  }

}