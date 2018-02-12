package quizleague.domain.notification

import quizleague.domain._
import io.circe.generic.JsonCodec
import org.threeten.bp.LocalDateTime


object NotificationTypeNames{
  
  val result = "result"
  val maintain = "maintain"
  
}


case class Notification(
  id:String,
  typeName:String,
  timestamp:LocalDateTime,
  payload:Payload,
  retired:Boolean = false
  
) extends Entity

@JsonCodec
sealed trait Payload

case class ResultPayload(fixtureId:String) extends Payload
case class MaintainMessagePayload(message:String) extends Payload