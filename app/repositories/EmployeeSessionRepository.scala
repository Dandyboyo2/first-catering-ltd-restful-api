package repositories

import models.{Card, EmployeeSession}
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.Inject

case class EmployeeSessionRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration,
                                               employeeRepository: EmployeeRepository)(implicit ec: ExecutionContext) {

  private val employeeSessionCollection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection]("EmployeeSession"))

  val inactivityTimeOut: Int = config.get[Int]("session.inactivityTimeOut")

  private val index: Index = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("session-index"),
    options = BSONDocument("expireAfterSeconds" -> inactivityTimeOut)
  )

  employeeSessionCollection.map(_.indexesManager.ensure(index))
  
  def findEmployeeSessionByID(card: Card): Future[Option[EmployeeSession]] =
    employeeSessionCollection.flatMap(_.find(Json.obj("cardID" -> card.cardID), None).one[EmployeeSession])

  def createEmployeeSessionByID(session: EmployeeSession): Future[WriteResult] = {
    employeeSessionCollection.flatMap(_.insert.one(session))
  }

  def deleteEmployeeSessionByID(card: Card): Future[WriteResult] =
    employeeSessionCollection.flatMap(_.delete.one(Json.obj("cardID" -> card.cardID)))
}
