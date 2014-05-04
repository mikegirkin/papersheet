package controllers

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import common._
import dao._
import model._
import JsonFormatHelpers._
import authentication.{SecureSocialSecurity, AbstractSecurity}

abstract class EntryGroupController extends Controller with AbstractSecurity with DaoProvider {

  case class EditGroupVM(
    name: String
  )

  val editGroupForm = Form(
    mapping(
      "name" -> nonEmptyText
    )(EditGroupVM.apply)(EditGroupVM.unapply)
  )

  def list() = SecuredAjax { account => request =>
    Ok(
      toJson(
        entryGroupDao.listForUser(account)
      )
    )
  }

  def create() = SecuredAjax { account => implicit request =>
    editGroupForm.bindFromRequest().fold(
      errorForm => BadRequest(toJson(errorForm.errors)),
      vm => {
        val group = entryGroupDao.insert(
          new EntryGroup(account.id.get, vm.name)
        )
        Ok(
          toJson(group)
        )
      }
    )
  }

}

object EntryGroupController extends EntryGroupController with SecureSocialSecurity with PsqlDaoProvider
