package io.armee

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.LoadSchedulerMessages.JsonEvent
import io.armee.messages.EventGeneratorMessages.{EventRequest, EventRequestEnvelope, JsonEventRequest, MonitorRequests}
import io.armee.messages.LoadMonitorMessages.MonitorRequestsReply

import scala.collection.immutable.Queue

class EventGenerator() extends Actor with ActorLogging{
  var eventRequestQueue = Queue.empty[EventRequest]
  var numRequests,numFailures = 0 //for stats only, keeps increasing

  def generateJson(): Unit = {
    val evr = eventRequestQueue.head
    sender ! JsonEvent("{ 'generatedAt':" +  System.currentTimeMillis / 1000 + ", 'id' : " + numRequests + " , 'username' : 'Michel' , 'role' : 'Data Engineer'")
    eventRequestQueue = eventRequestQueue.drop(1)
  }

  def receive = {
    case MonitorRequests() => sender ! MonitorRequestsReply(numRequests,numFailures,eventRequestQueue.size)  //Put in top to give it prio (otherwise it will be lost)
    case js @ EventRequestEnvelope(jr) =>
      //log.info("Receiving json event request")

      numRequests = numRequests + 1

      if (eventRequestQueue.isEmpty) {
        if (eventRequestQueue.size < 1000) {
          //val evr: EventRequest = jr
          jr match {
            case JsonEventRequest() => eventRequestQueue :+= jr
            case _ => // ignore
          }

          generateJson()
        }
        else numFailures = numFailures + 1
      }
      else generateJson()
    case _: MemberEvent => // ignore
  }
}


