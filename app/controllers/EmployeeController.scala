package controllers

import models.{Card, Employee, EmployeeSession}
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}
import reactivemongo.core.errors.DatabaseException
import repositories.{EmployeeRepository, EmployeeSessionRepository}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class EmployeeController @Inject()(cc: ControllerComponents,
                                   employeeRepository: EmployeeRepository,
                                   employeeSessionRepository: EmployeeSessionRepository)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

  //TODO Add securityPin functionality to controller & repo?

//  def validatePin(cardID: Card, suppliedPin: Int): Action[AnyContent] = Action.async {
//    case Some(validPin) => {
//      suppliedPin match {
//        
//      }
//    }
//      employeeRepository.findEmployeeByID(card).flatMap {
//        case Some(employee) => if (employee.securityPin equals(suppliedPin))
//      }
//  }

  def insertCard(cardID: Card): Action[AnyContent] = Action.async {

    implicit request =>
      employeeRepository.findEmployeeByID(cardID).flatMap {
        case Some(employee) =>
          employeeSessionRepository.findEmployeeSessionByID(cardID).flatMap {
            case Some(_) =>
              employeeSessionRepository.deleteEmployeeSessionByID(cardID).map(_ => Ok(s"Goodbye ${employee.name}."))
            case None =>
             employeeSessionRepository.createEmployeeSessionByID(EmployeeSession(cardID.cardID, LocalDateTime.now))
                .map(_ => Ok(s"Welcome ${employee.name}."))
          }
        case None => Future.successful(BadRequest("Your cardID is not registered on the system. Please proceed to registering your card."))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Employee model."))
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def registerEmployee: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      (for {
        employee <- Future.fromTry(Try {
          request.body.as[Employee]
        })
        _ <- employeeRepository.registerEmployee(employee)
      } yield Ok("Employee registered successfully.")).recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Employee model."))
        case _: DatabaseException =>
          Future.successful(BadRequest("Duplicate key - unable to parse Json to the Employee model."))
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def findEmployeeByID(cardID: Card): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      employeeRepository.findEmployeeByID(cardID).map {
        case None => NotFound("An Employee could not be found with that cardID.")
        case Some(employee) => Ok(Json.toJson(employee))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Employee model."))
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def checkBalance(cardID: Card): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      employeeRepository.findEmployeeByID(cardID).map {
        case Some(employee) => Ok(Json.toJson(s"Your balance is £${employee.balance}.00."))
        case None => NotFound("An Employee could not be found with that cardID.")
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Employee model."))
        case e =>
          Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
      }
  }

  def topUpBalance(cardID: Card, topUpAmount: Int): Action[AnyContent] = Action.async {
    employeeRepository.findEmployeeByID(cardID).flatMap {
      case Some(_) =>
        topUpAmount match {
          case x if x <= 0 => Future.successful(BadRequest("You must enter a positive amount to top up your balance."))
          case _ =>
            employeeRepository.findEmployeeByID(cardID).flatMap {
              case Some(_) => employeeRepository.topUpBalance(cardID, topUpAmount)
                .map { _ => Ok(s"Top up successful, £$topUpAmount has been added to your account balance.")}
            }
        }
      case None => Future.successful(NotFound("An Employee could not be found with that cardID."))
    } recoverWith {
      case _: JsResultException => Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Employee model."))
      case e => Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
    }
  }

  def accountTransaction(cardID: Card, costOfGoods: Int): Action[AnyContent] = Action.async {
    employeeRepository.findEmployeeByID(cardID).flatMap {
      case Some(employee) => {
        costOfGoods match {
          case x if x > employee.balance => Future.successful(BadRequest("Insufficient balance to complete this transaction."))
          case _ =>
            employeeRepository.findEmployeeByID(cardID).flatMap {
              case Some(_) =>
                employeeRepository.accountTransaction(cardID, costOfGoods).map {
                  case Some(_) => Ok("Your transaction was successful.")
                }
            }
        }
      }
      case None => Future.successful(NotFound("An Employee could not be found with that cardID."))
    }.recoverWith {
      case _: JsResultException => Future.successful(BadRequest("Incorrect data - unable to parse Json data to the Employee model."))
      case e => Future.successful(BadRequest(s"An issue has occurred resulting in the following exception: $e."))
    }
  }
}
