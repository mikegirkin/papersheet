package common

import play.api._

import securesocial.core._
import dao._
import model._

class UserServiceImpl(application: Application) extends UserServicePlugin(application) {
  def deleteExpiredTokens(): Unit = {
    println("deleteExpiredTokens")
  }

  def deleteToken(uuid: String): Unit = {
    println("deleteToken " + uuid)
  }

  def find(id: IdentityId): Option[Identity] = {
    AccountDao.findByIdentityId(id)
  }
  def findByEmailAndProvider(email: String,providerId: String): Option[securesocial.core.Identity] = {
    AccountDao.findByEmail(email, providerId)
  }

  def save(user: Identity): Identity = {
    AccountDao.findByIdentityId(user.identityId).map { acc =>
      val accToUpdate = Account.newFromIdentity(user).copy(id = acc.id)
      AccountDao.update(accToUpdate)
      accToUpdate
    }.getOrElse {
      AccountDao.create(
        Account.newFromIdentity(user)
      )
    }

  }

  def findToken(token: String): Option[securesocial.core.providers.Token] = {
    AccountDao.findToken(token)
  }

  def save(token: securesocial.core.providers.Token): Unit = {
    AccountDao.saveToken(token)
  }
}


