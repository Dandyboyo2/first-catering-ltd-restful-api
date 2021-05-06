package mongoDateTimeFormat

import play.api.libs.json.{Json, Reads, Writes, __}
import java.time.{Instant, LocalDateTime, ZoneOffset}

trait MongoDateTimeFormat {

  implicit val localDateTimeRead: Reads[LocalDateTime] =
    (__ \ "$date").read[Long].map {
      millis => LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC
    )
  }

  implicit val localDateTimeWrite: Writes[LocalDateTime] =
    (
      dateTime: LocalDateTime) => Json.obj(
    "$date" -> dateTime.atZone(ZoneOffset.ofHours(1)).toInstant.toEpochMilli
      )
}
