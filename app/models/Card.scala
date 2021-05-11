package models

import play.api.libs.json.{OWrites, Reads, __}
import play.api.mvc.PathBindable

case class Card(cardID: String)

object Card {

  implicit val reads: Reads[Card] = (__ \ "cardID").read[String].map(Card(_))
  implicit val writes: OWrites[Card] = (__ \ "cardID").write[String].contramap(_.cardID)

  implicit val pathBindable: PathBindable[Card] = {
    new PathBindable[Card] {
      override def bind(key: String, value: String): Either[String, Card] =
        if (value.matches("^[a-zA-Z0-9]{16}$")) Right(Card(value)) else Left("Invalid CardID.")

      override def unbind(key: String, value: Card): String = value.cardID
    }
  }
}
