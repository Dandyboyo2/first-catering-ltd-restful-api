package models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Reads, OWrites, __}

case class Employee(employeeID: Card,
                    name: String,
                    email: String,
                    mobileNo: String,
                    balance: Int,
                    securityPin: Int)

object Employee {

  implicit val reads: Reads[Employee] =
      (__.read[Card] and
      (__ \ "name").read[String] and
      (__ \ "email").read[String] and
      (__ \ "mobileNo").read[String] and
      (__ \ "balance").read[Int] and
      (__ \ "securityPin").read[Int]
    )(Employee.apply _)


  implicit val writes: OWrites[Employee] =
      (__.write[Card] and
      (__ \ "name").write[String] and
      (__ \ "email").write[String] and
      (__ \ "mobileNo").write[String] and
      (__ \ "balance").write[Int] and
      (__ \ "securityPin").write[Int]
    ) (unlift(Employee.unapply))
}
