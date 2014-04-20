package dao

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import org.joda.time.DateTime
import securesocial.core.providers.Token
import securesocial.core._

import common.AnormExtension._
import model._

trait IAccountDao {
  def newUserpassAccount(login: String, email: String, firstName: String, lastName: String, password: String) =
    Account(
      NotAssigned,
      IdentityId(login, "userpass"),
      firstName,
      lastName,
      firstName + " " + lastName,
      Some(email),
      AuthenticationMethod("userPassword"),
      passwordInfo = Some(Registry.hashers.currentHasher.hash(password))
    )

  def create(acc: Account): Account
  def update(acc: Account): Unit
  def list(): Seq[Account]
  def getById(id: Long): Option[Account]
  def findByIdentityId(identityId: IdentityId): Option[Account]
  def findByEmail(email: String, providerId: String): Option[Account]
  def findSingle(email: Option[String] = None,
                 providerId: Option[String] = None,
                 userId: Option[String] = None,
                 id: Option[Long] = None): Option[Account]

  def saveToken(tkn: Token): Unit
  def findToken(uuid: String): Option[Token]

}

object  AccountDao extends IAccountDao {

  val identityParser =
    long("id") ~
    str("userId") ~
    str("providerId") ~
    get[Option[String]]("email") ~
    str("firstName") ~
    str("lastName") ~
    str("fullname") ~
    get[Option[String]]("avatarUrl") ~
    str("authMethod") ~
    get[Option[String]]("token") ~
    get[Option[String]]("secret") ~
    get[Option[String]]("accessToken") ~
    get[Option[String]]("tokenType") ~
    get[Option[Int]]("expiresIn") ~
    get[Option[String]]("refreshToken") ~
    get[Option[String]]("hasher") ~
    get[Option[String]]("password") ~
    get[Option[String]]("salt") ~
    get[DateTime]("created") ~
    get[Option[DateTime]]("lastLogin") ~
    bool("isActive") map { case id ~ userId ~ providerId ~ email ~ firstName ~ lastName ~ fullName ~ avatarUrl ~
      authMethod ~ token ~ secret ~ accessToken ~ tokenType ~ expiresIn ~ refreshToken ~
      hasher ~ password ~ salt ~ created ~ lastLogin ~ isActive =>
      new Account(
        Id(id),
        IdentityId(userId, providerId),
        firstName,
        lastName,
        fullName,
        email,
        AuthenticationMethod(authMethod),
        avatarUrl,
        (token, secret) match {
          case (Some(tkn), Some(scrt)) => Some(OAuth1Info(tkn, scrt))
          case _ => None },
        accessToken match {
          case Some(at) => Some(new OAuth2Info(at, tokenType, expiresIn, refreshToken))
          case _ => None },
        (hasher, password) match {
          case (Some(hsr), Some(pwd)) => Some(PasswordInfo(hsr, pwd, salt))
          case _ => None
        }
      )
    }

  val tokenParser =
    int("id") ~
      str("uuid") ~
      str("email") ~
      get[DateTime]("creationTime") ~
      get[DateTime]("expirationTime") ~
      bool("isSignUp") map {
      case id ~ uuid ~ email ~ creationTime ~ expirationTime ~ isSignUp => Token(uuid, email, creationTime, expirationTime, isSignUp)
    }

  val allFields = """
    id,
    userId,
    providerId,
    email,
    firstName,
    lastName,
    fullName,
    avatarUrl,
    authMethod,
    token,
    secret,
    accessToken,
    tokenType,
    expiresIn,
    refreshToken,
    hasher,
    password,
    salt,
    created,
    lastLogin,
    isActive
  """

  def findSingle(
    email: Option[String] = None,
    providerId: Option[String] = None,
    userId: Option[String] = None,
    id: Option[Long] = None): Option[Account] = {

    val conditions = Seq(
      email.map { _ => "email = {email}" },
      providerId.map { _ => "providerId = {providerId}"},
      userId.map { _ => "userId = {userId}"},
      id.map { _ => "id = {id}" }
    ).flatten.mkString(" and ")

    val query =
      s"""
        select
          $allFields
        from Account
        where
          $conditions
      """

    val params = Seq(
      email.map { 'email -> _ },
      providerId.map { 'providerId -> _},
      userId.map { 'userId -> _ },
      id.map { 'id -> _ }
    ).flatMap( _.map(v => v._1 -> toParameterValue(v._2)))

    DB.withConnection { implicit cn =>
      SQL(
        query
      ).on(
        params:_*
      ).as(identityParser.singleOpt)
    }
  }

  def findByIdentityId(identityId: IdentityId): Option[Account] =
    findSingle(userId = Some(identityId.userId), providerId = Some(identityId.providerId))

  def findByEmail(email: String, providerId: String): Option[Account] =
    findSingle(email = Some(email), providerId = Some(providerId))

  def getById(id: Long): Option[Account] =
    findSingle(id = Some(id))

  def create(acc: Account) = DB.withConnection { implicit cn =>
    SQL(
      """
        insert into Account (
          userId, providerId, email, firstName, lastName, fullName,
          avatarUrl, authMethod, token, secret, accessToken, tokenType,
          expiresIn, refreshToken, hasher, password, salt, created,
          lastLogin, isActive)
        values (
          {userId}, {providerId}, {email}, {firstName}, {lastName}, {fullName},
          {avatarUrl}, {authMethod}, {token}, {secret}, {accessToken}, {tokenType},
          {expiresIn}, {refreshToken}, {hasher}, {password}, {salt}, {created},
          {lastLogin}, {isActive}
        )
      """
    ).on(
      'userId -> acc.identityId.userId,
      'providerId -> acc.identityId.providerId,
      'email -> acc.email,
      'firstName -> acc.firstName,
      'lastName -> acc.lastName,
      'fullName -> acc.fullName,
      'avatarUrl -> acc.avatarUrl,
      'authMethod -> acc.authMethod.method,
      'token -> acc.oAuth1Info.map(x => x.token),
      'secret -> acc.oAuth1Info.map(x => x.secret),
      'accessToken -> acc.oAuth2Info.map(x => x.accessToken),
      'tokenType -> acc.oAuth2Info.map(x => x.tokenType),
      'expiresIn -> acc.oAuth2Info.map(x => x.expiresIn),
      'refreshToken -> acc.oAuth2Info.map(x => x.refreshToken),
      'hasher -> acc.passwordInfo.map(x => x.hasher),
      'password -> acc.passwordInfo.map(x => x.password),
      'salt -> acc.passwordInfo.map(x => x.salt),
      'created -> DateTime.now,
      'lastLogin -> None,
      'isActive -> true
    ).executeInsert() match {
      case Some(key) => acc
      case _ => throw new Exception
    }
  }

  def update(acc: Account) = DB.withConnection { implicit cn =>
    SQL(
      """
        update Account set
          email = {email},
          firstName = {firstName},
          lastName = {lastName},
          fullName = {fullName},
          avatarUrl = {avatarUrl},
          authMethod = {authMethod},
          token = {token},
          secret = {secret},
          accessToken = {accessToken},
          tokenType = {tokenType},
          expiresIn = {expiresIn},
          refreshToken = {refreshToken},
          hasher = {hasher},
          password = {password},
          salt = {salt},
          created = {created},
          lastLogin = {lastLogin},
          isActive = {isActive}
        where
          providerId = {providerId} and
          userId = {userId}
      """
    ).on(
      'userId -> acc.identityId.userId,
      'providerId -> acc.identityId.providerId,
      'email -> acc.email,
      'firstName -> acc.firstName,
      'lastName -> acc.lastName,
      'fullName -> acc.fullName,
      'avatarUrl -> acc.avatarUrl,
      'authMethod -> acc.authMethod.method,
      'token -> acc.oAuth1Info.map(x => x.token),
      'secret -> acc.oAuth1Info.map(x => x.secret),
      'accessToken -> acc.oAuth2Info.map(x => x.accessToken),
      'tokenType -> acc.oAuth2Info.map(x => x.tokenType),
      'expiresIn -> acc.oAuth2Info.map(x => x.expiresIn),
      'refreshToken -> acc.oAuth2Info.map(x => x.refreshToken),
      'hasher -> acc.passwordInfo.map(x => x.hasher),
      'password -> acc.passwordInfo.map(x => x.password),
      'salt -> acc.passwordInfo.map(x => x.salt),
      'created -> DateTime.now,
      'lastLogin -> None,
      'isActive -> true
    ).executeUpdate()
  }

  def saveToken(tkn: Token) = DB.withConnection { implicit cn =>
    SQL(
      """
        insert into Token (
          uuid, email, creationTime, expirationTime, isSignUp
        ) values (
          {uuid}, {email}, {creationTime}, {expirationTime}, {isSignUp}
        )
      """
    ).on(
      'uuid -> tkn.uuid,
      'email -> tkn.email,
      'creationTime -> tkn.creationTime,
      'expirationTime -> tkn.expirationTime,
      'isSignUp -> tkn.isSignUp
    ).executeInsert()
  }

  def findToken(uuid: String) = DB.withConnection { implicit cn =>
    SQL(
      """
        select id, uuid, email, creationTime, expirationTime, isSignUp
        from Token
        where uuid = {uuid}
      """
    ).on(
      'uuid -> uuid
    ).as(
      tokenParser.singleOpt
    )
  }

  def list() = DB.withConnection { implicit cn =>
    SQL(
      s"""
        select
          $allFields
        from Account
      """
    ).as(identityParser.*)
  }

}
