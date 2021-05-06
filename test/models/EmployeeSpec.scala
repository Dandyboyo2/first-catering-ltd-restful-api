package models

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class EmployeeSpec extends WordSpec with MustMatchers {

  val employeeCardExample: Card = Card("QWerty1234567890")

  "Employee model" must {

    "Deserialize correctly" in {
      val json = Json.obj(
        "cardID" -> "QWerty1234567890",
        "name" -> "EmployeeTestName",
        "email" -> "EmployeeTestEmail@email.co.uk",
        "mobileNo" -> "0987654321",
        "securityPin" -> 1111,
        "balance" -> 60
      )

      val expectedEmployee = Employee(
        card = employeeCardExample,
        name = "EmployeeTestName",
        email = "EmployeeTestEmail@email.co.uk",
        mobileNo = "0987654321",
        securityPin = 1111,
        balance = 60
      )
      json.as[Employee] mustEqual expectedEmployee
    }

    "Serialize correctly" in {
      val employee = Employee(
        card = employeeCardExample,
        name = "EmployeeTestName",
        email = "EmployeeTestEmail@email.co.uk",
        mobileNo = "0987654321",
        securityPin = 1111,
        balance = 60
      )

      val expectedJson = Json.obj(
        "cardID" -> "QWerty1234567890",
        "name" -> "EmployeeTestName",
        "email" -> "EmployeeTestEmail@email.co.uk",
        "mobileNo" -> "0987654321",
        "securityPin" -> 1111,
        "balance" -> 60
      )
      Json.toJson(employee) mustBe expectedJson
    }
  }
}
