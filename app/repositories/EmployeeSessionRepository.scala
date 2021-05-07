package repositories

import akka.io.Tcp.Write
import models.{Card, Employee, EmployeeSession}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class EmployeeSessionRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration,
                                          memberRepository: EmployeeRepository)(implicit ec: ExecutionContext) {


  private val employeeSessionCollection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection]("EmployeeSession"))

  val inactivityTimeOut: Int = config.get[Int]("session.InactivityTimeOut")

  def createOrUpdateEmployeeSession(card: Card, session: EmployeeSession): Future[Option[WriteResult]] =
    employeeSessionCollection.flatMap {
      result =>
        val selector: JsObject = Json.obj("cardID" -> card.cardID)
        val sessionUpdater: JsObject = Json.obj(???)
    }

  def findAndUpdateSessionByID(card: Card): Future[Option[EmployeeSession]] = ???
}
