package io.armee.messages

import akka.actor.ActorRef
import akka.dispatch.ControlMessage

object EventGeneratorMessages {
  sealed trait EventRequest

  case class JsonEventRequest(replyTo : ActorRef) extends EventRequest
  case class XmlEventRequest() extends EventRequest

  case class EventRequestEnvelope(er: EventRequest)

  case class MonitorRequests()  extends ControlMessage //gives this message prio

}
