package io.armee

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.EventGeneratorMessages.JsonEventRequest
import io.armee.messages.LoadSchedulerMessages.JsonEvent

class EventGenerator(eventType: String) extends Actor with ActorLogging{
  def receive = {
    case JsonEventRequest =>
      log.info("Generating Json event received")
      sender ! JsonEvent("{ 'generatedAt':" +  System.currentTimeMillis / 1000 + ", 'username' : 'Michel' , 'role' : 'Data Engineer'")
    case _: MemberEvent => // ignore
  }
}

