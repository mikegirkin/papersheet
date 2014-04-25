package controllers

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import common._
import dao._

abstract class EntryGroupController extends Controller with AbstractSecurity with DaoProvider {

  def list() = SecuredAjax { account => request =>
    Ok(
      toJson(
        entryGroupDao.listForUser(account)
      )
    )
  }

}

object EntryGroupController extends Controller with SecureSocialSecurity with PsqlDaoProvider
