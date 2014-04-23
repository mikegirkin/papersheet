package dao

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.ParameterValue

trait SqlHelpers {
  def Q[Result](query: String)
               (params: (Symbol, ParameterValue[_])* )
               (parser: ResultSetParser[Result]): Result = DB.withConnection { implicit cn =>
    genericQuery(query)(params)
      .as(parser)
  }

  def I[TKey](query: String)
             (params: (Symbol, ParameterValue[_])*): TKey = DB.withConnection { implicit cn =>
    genericQuery(query)(params)
    .executeInsert() match {
      case Some(key) => key.asInstanceOf[TKey]
      case None => throw new Exception("Database doesn't work properly")
    }
  }

  def U(query: String)
       (params: (Symbol, ParameterValue[_])*): Unit = DB.withConnection { implicit cn =>
    genericQuery(query)(params)
  }

  def genericQuery(query: String)
                  (params: Seq[(Symbol, ParameterValue[_])]) = DB.withConnection { implicit cn =>
    SQL(query)
      .on(params:_*)
  }
}