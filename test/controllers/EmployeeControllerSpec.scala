package controllers

  import models.{Card, Employee, EmployeeSession}
  import org.mockito.Matchers.any
  import org.mockito.Mockito.{mock, when}
  import org.scalatest.OptionValues._
  import org.scalatest.concurrent.ScalaFutures
  import org.scalatest.{MustMatchers, WordSpec}
  import org.scalatestplus.mockito.MockitoSugar
  import play.api.{Application, inject}
  import play.api.inject.guice.GuiceApplicationBuilder
  import play.api.libs.json.{JsResultException, JsValue, Json}
  import play.api.mvc.{AnyContentAsEmpty, Result}
  import play.api.test.FakeRequest
  import play.api.test.Helpers.{GET, POST, contentAsString, route, status, _}
  import reactivemongo.api.commands.UpdateWriteResult
  import reactivemongo.bson.BSONDocument
  import reactivemongo.core.errors.DatabaseException
  import repositories.{EmployeeRepository, EmployeeSessionRepository}

  import java.time.LocalDateTime
  import scala.concurrent.Future

class EmployeeControllerSpec extends WordSpec with MustMatchers
  with MockitoSugar with ScalaFutures {

  val mockEmployeeRespository: EmployeeRepository = mock[EmployeeRepository]
  val mockEmployeeSessionRespository: EmployeeSessionRepository = mock[EmployeeSessionRepository]

  private lazy val builder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(
      inject.bind[EmployeeRepository].toInstance(mockEmployeeRespository),
      inject.bind[EmployeeSessionRepository].toInstance(mockEmployeeSessionRespository)
    )

  private val cardID = Card("QWerty1234567890")

  "insertCard" must {
    "return an OK response and delete current session if one already exists" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      when(mockEmployeeSessionRespository.findEmployeeSessionByID(any()))
        .thenReturn(Future.successful(Some(EmployeeSession("QWerty1234567890", LocalDateTime.now))))

      when(mockEmployeeSessionRespository.deleteEmployeeSessionByID(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.insertCard(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Goodbye testName."

      app.stop
    }

    "return an OK response and create a new session if a session does not exist" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      when(mockEmployeeSessionRespository.findEmployeeSessionByID(any()))
        .thenReturn(Future.successful(None))

      when(mockEmployeeSessionRespository.createEmployeeSessionByID(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.insertCard(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Welcome testName."

      app.stop
    }

    "return a BAD_REQUEST response and the correct message if the Employee does not exist" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.insertCard(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Your cardID is not registered on the system. Please proceed to registering your card."

      app.stop
    }

    "return a BAD_REQUEST response if the data is invalid" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.insertCard(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Employee model."

      app.stop
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.insertCard(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."

      app.stop
    }
  }

  "findEmployeebyID" must {
    "return an OK response and the Employee details" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 200, 1234))))
      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.findEmployeeByID(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) must contain
      """{"_id":cardID,"name":testName,"email":"testEmail","mobileNumber":"testMobileNo","balance":200,"securityPin":1234}""".stripMargin

      app.stop
    }

    "return a NOT_FOUND response with correct message when the Employee could not be found" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(None))
      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.findEmployeeByID(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "An Employee could not be found with that cardID."

      app.stop
    }

    "return a BAD_REQUEST response if data is invalid" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.findEmployeeByID(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Employee model."

      app.stop
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.findEmployeeByID(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."

      app.stop
    }
  }

  "registerEmployee" must {
    "return an OK response with success message if data is valid" in {
      when(mockEmployeeRespository.registerEmployee(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val EmployeeJson: JsValue = Json.toJson(Employee(cardID, "testName", "testEmail", "testMobileNo", 200, 1234))

      val app: Application = builder.build()

      val request: FakeRequest[JsValue] =
        FakeRequest(POST, routes.EmployeeController.registerEmployee.url).withBody(EmployeeJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Employee registered successfully."

      app.stop
    }

    "Return a BAD_REQUEST response with correct error message when the data is invalid" in {
      val EmployeeJson: JsValue = Json.toJson("Invalid Json")

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.EmployeeController.registerEmployee.url).withBody(EmployeeJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Employee model."

      app.stop
    }

    "Return a BAD_REQUEST response with correct error message when duplicate data is given" in {
      when(mockEmployeeRespository.registerEmployee(any()))
        .thenReturn(Future.failed(new DatabaseException {
          override def originalDocument: Option[BSONDocument] = None

          override def code: Option[Int] = None

          override def message: String = "Duplicate key - unable to parse Json to the Employee model."
        }))

      val EmployeeJson: JsValue = Json.toJson(Employee(cardID, "testName", "testEmail", "testMobileNo", 200, 1234))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.EmployeeController.registerEmployee.url).withBody(EmployeeJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Duplicate key - unable to parse Json to the Employee model."

      app.stop
    }

    "Return a BAD_REQUEST response with correct error message when something else fails" in {
      when(mockEmployeeRespository.registerEmployee(any()))
        .thenReturn(Future.failed(new Exception))

      val EmployeeJson: JsValue = Json.toJson(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.EmployeeController.registerEmployee.url).withBody(EmployeeJson)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."

      app.stop
    }
  }

  "checkBalance" must {
    "return a NOT_FOUND response with correct message when Employee could not be found" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.checkBalance
      (Card("testId1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "An Employee could not be found with that cardID."

      app.stop
    }

    "return an OK response with correct balance when correct cardID is input" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController
        .checkBalance(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "60"

      app.stop
    }

    "return a BAD_REQUEST response if data is invalid" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.checkBalance(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Incorrect data - unable to parse Json data to the Employee model."

      app.stop
    }

    "return a BAD_REQUEST response if something else has failed" in {
      when(mockEmployeeRespository.findEmployeeByID(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.EmployeeController.checkBalance(Card("QWerty1234567890")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "An issue has occurred resulting in the following exception: java.lang.Exception."

      app.stop
    }
  }

  "topUpBalance" must {

    "return an OK response with success message if data is valid" in {
      when(mockEmployeeRespository.topUpBalance(any, any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      when(mockEmployeeRespository.findEmployeeByID(any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.EmployeeController.topUpBalance(Card("QWerty1234567890"), 60).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Top up successful, £60 has been added to your account balance."

      app.stop
    }

    "return a BAD_REQUEST response with correct error message if given a negative amount" in {

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.EmployeeController.topUpBalance(Card("QWerty1234567890"), -20).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "You must enter a positive amount to top up your balance."

      app.stop
    }

    "return a NOT_FOUND response with correct error message when the Employee could not be found" in {

      when(mockEmployeeRespository.findEmployeeByID(Card("QWerty1234567890")))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.EmployeeController.topUpBalance(Card("QWerty1234567890"), 60).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "An Employee could not be found with that cardID."

      app.stop
    }
  }

  "accountTransaction" must {
    "return an OK response with success message if data is valid" in {
      when(mockEmployeeRespository.accountTransaction(any, any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      when(mockEmployeeRespository.findEmployeeByID(any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))


      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.EmployeeController.accountTransaction(Card("QWerty1234567890"), 60).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Your transaction was successful."

      app.stop
    }

    "return a BAD_REQUEST response with correct error message if account transaction cost is higher than total balance" in {
      when(mockEmployeeRespository.findEmployeeByID(any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      when(mockEmployeeRespository.accountTransaction(any, any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.EmployeeController.accountTransaction(Card("QWerty1234567890"), 100).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Insufficient balance to complete this transaction."

      app.stop
    }

    "return a NOT_FOUND response with correct error message when Employee could not be found" in {
      when(mockEmployeeRespository.findEmployeeByID(any))
        .thenReturn(Future.successful(Some(Employee(cardID, "testName", "testEmail", "testMobileNo", 60, 1111))))

      when(mockEmployeeRespository.findEmployeeByID(Card("QWerty1234567890")))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.EmployeeController.accountTransaction(Card("QWerty1234567890"), 60).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "An Employee could not be found with that cardID."

      app.stop
    }
  }
}
