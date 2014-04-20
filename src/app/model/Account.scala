package model

import securesocial.core._
import anorm._

case class Account(
  id : Pk[Long],
  identityId: IdentityId,
  firstName: String,
  lastName: String,
  fullName: String,
  email: Option[String],
  authMethod: AuthenticationMethod,
  avatarUrl: Option[String] = None,
  oAuth1Info: Option[OAuth1Info] = None,
  oAuth2Info: Option[OAuth2Info] = None,
  passwordInfo: Option[PasswordInfo] = None
) extends Identity { }

case object Account {

  def newFromIdentity(identity: Identity) = Account(
    NotAssigned,
    identity.identityId,
    identity.firstName,
    identity.lastName,
    identity.fullName,
    identity.email,
    identity.authMethod,
    identity.avatarUrl,
    identity.oAuth1Info,
    identity.oAuth2Info,
    identity.passwordInfo
  )

  val Anonymous = Account(NotAssigned, IdentityId("Anonymous", "None"), 
    "Anonymous", "Anonymous", "Anonymous", None, AuthenticationMethod("none"))
}