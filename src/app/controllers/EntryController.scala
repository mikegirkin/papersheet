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

  def list(groupId: Option[Long] = None, stateId: Option[Long] = None) = SecuredAjax { account => request =>
    Ok(
      toJson(
        entryDao.listForUser(account.id.get, EntryQueryParams(groupId, stateId))
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

  def update(id: Long) = SecuredAjax { account => implicit request =>
    editEntryForm.bindFromRequest().fold(
      errorForm => BadRequest(toJson(errorForm.errors)),
      vm => {
        entryDao.getById(id).map { entry =>
          if(entry.creatorId == account.id.get) {
            val updatedEntry = entryDao.update(
              entry.copy(content = vm.content, stateId = vm.stateId, groupId = vm.groupId)
            )
            Ok(toJson(updatedEntry))
          } else {
            Forbidden
          }
        }.getOrElse{
          NotFound
        }
      }
    )
  }
}

object EntryController extends EntryController with SecureSocialSecurity with PsqlDaoProvider
