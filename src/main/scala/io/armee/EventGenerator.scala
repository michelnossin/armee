package io.armee

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.LoadSchedulerMessages.JsonEvent
import io.armee.messages.EventGeneratorMessages.{EventRequest, EventRequestEnvelope, JsonEventRequest, MonitorRequests}
import io.armee.messages.FileWriterMessages.WriteFileMessage
import io.armee.messages.LoadMonitorMessages.MonitorRequestsReply

import scala.collection.immutable.Queue

class EventGenerator() extends Actor with ActorLogging{
  var eventRequestQueue = Queue.empty[EventRequest]
  var numRequests,numFailures = 0 //for stats only, keeps increasing

  def generateJson(replyTo : ActorRef): Unit = {
    val evr = eventRequestQueue.head
    //replyTo ! JsonEvent("{ 'generatedAt':" +  System.currentTimeMillis / 1000 + ", 'id' : " + numRequests + " , 'username' : 'Michel' , 'role' : 'Data Engineer'")
    replyTo ! WriteFileMessage("{ 'generatedAt':" +  System.currentTimeMillis / 1000 + ", 'id' : " + numRequests + " , 'username' : 'Michel' , 'role' : 'Data Engineer'}\n")
    eventRequestQueue = eventRequestQueue.drop(1)
  }

  def receive = {
    case MonitorRequests() => {
      //println("received monitor request")
      sender ! MonitorRequestsReply(numRequests,numFailures,eventRequestQueue.size)
      numRequests = 0
      numFailures = 0
    }  //Put in top to give it prio (otherwise it will be lost)
    case js @ EventRequestEnvelope(jr) =>
      //log.info("Receiving json event request")

      numRequests = numRequests + 1

      //if (eventRequestQueue.isEmpty) {
      if (eventRequestQueue.size < 1000) {
        jr match {
          case JsonEventRequest(replyTo) => {
            eventRequestQueue :+= jr
            generateJson(replyTo)
          }
          case _ => // ignore
        }
      }
      else numFailures = numFailures + 1
      //}

    case _: MemberEvent => // ignore
  }
}


