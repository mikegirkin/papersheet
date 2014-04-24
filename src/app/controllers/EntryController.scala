package controllers

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import common._
import dao._

abstract class EntryController extends Controller with AbstractSecurity with DaoProvider {

  def list() = SecuredAjax { account => request =>
    Ok(
      toJson(
        entryDao.listForUser(account.id.get)
      )
    )
  }

}

object EntryController extends EntryController with SecureSocialSecurity with PsqlDaoProvider
