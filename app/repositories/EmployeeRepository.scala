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
    mongo.database.map(_.collection[JSONCollection]("employees"))
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

  def findEmployeeByID(IDsearch: Card): Future[Option[Employee]] = {
    employeeCollection.flatMap(_.find(
      Json.obj("cardID" -> IDsearch.cardID),
      None
    ).one[Employee])
  }

  def registerEmployee(newEmployee: Employee): Future[WriteResult] = {
    employeeCollection.flatMap(_.insert.one(newEmployee))
  }

  def topUpBalance(cardID: Card, topUpAmount: Int): Future[Option[Employee]] = {
    employeeCollection.flatMap {
      result =>
        val selector:    JsObject = Json.obj("cardID" -> cardID.cardID)
        val topUp: JsObject = Json.obj("$inc" -> Json.obj("balance" -> topUpAmount))
        findAndUpdate(result, selector, topUp).map(_.result[Employee])
    }
  }

  def accountTransaction(cardID: Card, costOfGoods: Int): Future[Option[Employee]] = {
    employeeCollection.flatMap {
      result =>
        val selector: JsObject = Json.obj("cardID" -> cardID.cardID)
        val modifier: JsObject = Json.obj("$inc" -> Json.obj("balance" -> -costOfGoods))
        findAndUpdate(result, selector, modifier).map(_.result[Employee])
    }
  }
}
