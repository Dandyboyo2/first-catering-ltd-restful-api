package controllers

import play.api.mvc.{AbstractController, ControllerComponents}
import repositories.{EmployeeRepository,EmployeeSessionRepository }

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmployeeController @Inject()(cc: ControllerComponents,
                                   employeeRepository: EmployeeRepository,
                                   employeeSessionRepository: EmployeeSessionRepository)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

}
