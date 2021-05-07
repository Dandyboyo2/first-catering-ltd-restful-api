package repositories

import models.{Card, Employee}
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoApi

import javax.inject.Inject
import scala.concurrent.ExecutionContext

case class EmployeeSessionRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration,
                                          memberRepository: EmployeeRepository)(implicit ec: ExecutionContext) {


}
