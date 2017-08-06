/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.armee

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.LoadSchedulerMessages.JsonEvent
import io.armee.messages.EventGeneratorMessages._
import io.armee.messages.WriterMessages.WriteMessage
import io.armee.messages.LoadMonitorMessages.MonitorRequestsReply
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import scala.collection.immutable.Queue
import scala.concurrent.Await

class EventGenerator(eventTarget : EventTarget) extends Actor with ActorLogging{
  var eventRequestQueue = Queue.empty[EventRequest]
  var numRequests,numFailures = 0 //for stats only, keeps increasing

  //For now let's create a db session for each generator
  val uid = java.util.UUID.randomUUID.toString
  val jdbcWriter = context.system.actorOf(Props(new JdbcWriter(eventTarget.asInstanceOf[JdbcEventTarget])), "jdbcwriter_" + self.path.name + "_" + uid)

  def generateJson(replyTo : ActorRef): Unit = {
    val evr = eventRequestQueue.head
    /*
    implicit val timeout = Timeout(5.seconds)

    val future = replyTo ? WriteMessage("{ 'generatedAt':" +  System.currentTimeMillis / 1000 + ", 'id' : " + numRequests + " , 'username' : 'Michel' , 'role' : 'Data Engineer'}\n")

    val result = Await.result(future, timeout.duration).asInstanceOf[String]
    future.onSuccess {
      case result => {
        eventRequestQueue = eventRequestQueue.drop(1)
      }
    }
    */
    replyTo ! WriteMessage("{ 'generatedAt':" +  System.currentTimeMillis / 1000 + ", 'id' : " + numRequests + " , 'username' : 'Michel' , 'role' : 'Data Engineer'}\n")
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
      //Make bigger , example 1000 later on
      if (eventRequestQueue.size < 100) {
        jr match {
          case JsonEventRequest() => {
            eventRequestQueue :+= jr
            generateJson(jdbcWriter)
          }
          case _ => // ignore
        }
      }
      else {
        numFailures = numFailures + 1
        generateJson(jdbcWriter) //Still we have to proces the request to make the queue smaller
      }
      //}

    case _: MemberEvent => // ignore
  }
}


