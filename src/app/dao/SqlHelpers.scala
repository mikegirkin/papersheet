package dao

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.ParameterValue

trait SqlHelpers {
  protected def Q[Result](query: String)
               (params: (Symbol, ParameterValue[_])* )
               (parser: ResultSetParser[Result]): Result = DB.withConnection { implicit cn =>
    genericQuery(query)(params)
      .as(parser)
  }

  protected def I[TKey](query: String)
             (params: (Symbol, ParameterValue[_])*): TKey = DB.withConnection { implicit cn =>
    genericQuery(query)(params)
    .executeInsert() match {
      case Some(key) => key.asInstanceOf[TKey]
      case None => throw new Exception("Database doesn't work properly")
    }
  }

  protected def U(query: String)
       (params: (Symbol, ParameterValue[_])*): Unit = DB.withConnection { implicit cn =>
    genericQuery(query)(params).executeUpdate()
  }

  private def genericQuery(query: String)
                  (params: Seq[(Symbol, ParameterValue[_])]) = DB.withConnection { implicit cn =>
    SQL(query)
      .on(params:_*)
  }
}