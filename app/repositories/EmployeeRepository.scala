package repositories

import models.{Card, Employee}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.commands.{FindAndModifyCommand, WriteResult}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
case class EmployeeRepository @Inject()(cc: ControllerComponents,
                                   config: Configuration,
                                   mongo: ReactiveMongoApi)
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val employeeCollection: Future[JSONCollection] = {
    mongo.database.map(_.collection[JSONCollection]("Employees"))
  }

  def findEmployeeByID(cardID: Card): Future[Option[Employee]] = {
    employeeCollection.flatMap(_.find(
      Json.obj("employeeID" -> cardID.cardID),
      None
    ).one[Employee])
  }

  def registerEmployee(newEmployee:Employee): Future[WriteResult] = {
    employeeCollection.flatMap(_.insert.one(newEmployee))
  }

  def findAndUpdate(collection: JSONCollection, selection: JsObject,
                    modifier: JsObject): Future[FindAndModifyCommand.Result[collection.pack.type]] = {
    collection.findAndUpdate(
      selector = selection,
      update = modifier,
      fetchNewObject = true,
      upsert = false,
      sort = None,
      fields = None,
      bypassDocumentValidation = false,
      writeConcern = WriteConcern.Default,
      maxTime = None,
      collation = None,
      arrayFilters = Seq.empty
    )
  }
  //TODO refactor ""
  def topUpBalance(card: Card, topUpAmount: Int): Future[Option[Employee]] = {
    employeeCollection.flatMap {
      result =>
        val selector:    JsObject = Json.obj("employee" -> card.cardID)
        val topUp: JsObject = Json.obj("£increase" -> Json.obj("balance" -> topUpAmount))
        findAndUpdate(result, selector, topUp).map(_.result[Employee])
    }
  }

  def topUpTransactions() = ???
}
