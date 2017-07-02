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

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import io.armee.messages.EventGeneratorMessages.MonitorRequests
import io.armee.messages.LoadControllerMessages.{AddScheduler, ControllerMonitorRequestReply, RemoveScheduler}
import io.armee.messages.LoadMonitorMessages.{ControllerMonitorRequest, MonitorRequestsReply}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, MICROSECONDS, SECONDS}

class LoadMonitor(port : Int,seedPort: Option[Int]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var deltaTotalRequests,deltaTotalFailures,totalRequestRate,totalFailureRate = 0

  //Tell master to add new scheduler to akka cluster so the master can communicate with this worker node
  val remoteActor = seedPort map {
    port =>
      val address = Address("akka.tcp", "armee", "127.0.0.1", port)
      cluster.joinSeedNodes(immutable.Seq(address))

      context.actorSelection(address.toString + "/user/loadcontroller")
  }
  remoteActor.foreach(_ ! AddScheduler) //change to addmonitor

  //Start send events to executors when starting
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

    val address = Address("akka.tcp", "armee", "127.0.0.1", port)

    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
      val eventGenerators = context.actorSelection(address.toString + "/user/eventgenerator*")
      eventGenerators ! MonitorRequests()
    }
    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
      totalRequestRate = (deltaTotalRequests)/5
      totalFailureRate = (deltaTotalFailures)/5
      //println("Msg per sec (avg last 5 sec):" + totalRequestRate + " , failed: " + totalFailureRate)
      deltaTotalRequests = 0
      deltaTotalFailures = 0
    }
  }

  override def postStop(): Unit = {
    remoteActor.foreach(_ ! RemoveScheduler)  //change to removemonitor
    cluster.unsubscribe(self)
  }

  def receive = {
    case MonitorRequestsReply(num,lost,queueSize) =>

      //log.info("Actor: " + sender + " got " + num + " requests.")
      if (queueSize > 0) println("Actor: " + sender + " got " + num + " requests. Lost: " + lost + " . Queuesize: " + queueSize)
      deltaTotalRequests = deltaTotalRequests + num
      deltaTotalFailures = deltaTotalFailures + lost
    case ControllerMonitorRequest() =>
      sender ! ControllerMonitorRequestReply(totalRequestRate,totalFailureRate)
    case _: MemberEvent => // ignore
  }
}


