package repositories

import models.{Card, Employee}
import play.api.Configuration
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmployeeSessionRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration,
                                          memberRepository: EmployeeRepository)(implicit ec: ExecutionContext) {


}
