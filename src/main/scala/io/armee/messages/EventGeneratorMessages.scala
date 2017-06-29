package io.armee.messages

object EventGeneratorMessages {
  sealed trait EventRequest
  case class JsonEventRequest() extends EventRequest
  case class XmlEventRequest() extends EventRequest

  case class EventRequestEnvelope(er: EventRequest)

  case class MonitorRequests()

}

