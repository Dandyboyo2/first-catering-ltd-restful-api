package repositories

import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmployeeRepository @Inject()(cc: ControllerComponents,
                                   config: Configuration,
                                   mongo: ReactiveMongoApi)
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {

}
