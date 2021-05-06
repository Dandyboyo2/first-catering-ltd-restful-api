package models

import models.Card.pathBindable
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class CardSpec extends WordSpec with MustMatchers {

  val validCardExample = "QWerty1234567890"

  "Card model" must {

    "return 'Invalid Card ID.' if card ID does not match ID format" in {
      val invalidCardID = "623Wc"
      val testResult = "Invalid Card ID."

      pathBindable.bind("", invalidCardID) mustBe Left(testResult)
    }

    "Deserialise" in {
      val cardId = Card(cardID = validCardExample)
      val expectedJson = Json.obj("cardID" -> validCardExample)

      Json.toJson(cardId) mustEqual expectedJson
    }

    "Serialise" in {
      val expectedCardId = Card(cardID = validCardExample)
      val json = Json.obj("cardID" -> validCardExample)

      json.as[Card] mustEqual expectedCardId
    }

    "return a string" in {
      pathBindable.unbind("", Card("ValidIDTest12345")) mustEqual "ValidIDTest12345"
    }
  }
}
