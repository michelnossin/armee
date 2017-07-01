package io.armee.messages

/**
  * Created by michelnossin on 28-06-17.
  */
object LoadSchedulerMessages {
  case class JsonEvent(event: String)
  case class SendSoldiers(num: Int)
}
