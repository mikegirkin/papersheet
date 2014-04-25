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

abstract class EntryController extends Controller with AbstractSecurity with DaoProvider {

  case class EditEntryVM(
    content: String,
    stateId: Long,
    groupId: Long
  )

  val editEntryForm = Form(
    mapping(
      "content" -> nonEmptyText,
      "stateId" -> longNumber.verifying(id => entryStateDao.getById(id).isDefined),
      "groupId" -> longNumber.verifying(id => entryGroupDao.getById(id).isDefined)
    )(EditEntryVM.apply)(EditEntryVM.unapply)
  )

  def list() = SecuredAjax { account => request =>
    Ok(
      toJson(
        entryDao.listForUser(account.id.get)
      )
    )
  }

  def create() = SecuredAjax { account => implicit request =>
    editEntryForm.bindFromRequest().fold(
      errorForm => BadRequest(toJson(errorForm.errors)),
      vm => {
        val entry = entryDao.insert(
          new Entry(account.id.get, vm.stateId, vm.groupId, vm.content)
        )
        Ok(toJson(entry))
      }
    )
  }

}

object EntryController extends EntryController with SecureSocialSecurity with PsqlDaoProvider
