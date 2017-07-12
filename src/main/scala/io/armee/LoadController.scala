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

import io.armee.messages.LoadControllerMessages._
import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent._
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import io.armee.config.YamlConfig
import io.armee.messages.LoadMonitorMessages.ControllerMonitorRequest
import io.armee.messages.LoadSchedulerMessages.SendSoldiers
import io.armee.messages.ShellGateWayMessages.{ClusterStatusReply, SoldiersMetricsReply}

import scala.util.matching.Regex
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LoadController(seedPort: Option[Int],seedHost: String,yamlConfig : YamlConfig) extends Actor with ActorLogging {

  var sumTotalRequests,sumTotalFailures,msgPerSecond,failuresperSecond,numWorkersPerExecutor,activeExecutors = 0 //for monitoring
  val configuredMasterPort : Int = yamlConfig.masterPort
  val configuredShellPort : Int = yamlConfig.shellPort

  val cluster = Cluster(context.system)
  var router = Router(BroadcastRoutingLogic(), Vector[ActorRefRoutee]())

  //Add this controller to the controller group which acts ass the seed group for this akka cluster
  val remoteActor = seedPort map {
    port =>
      val address = Address("akka.tcp", "armee", seedHost, port)
      cluster.joinSeedNodes(immutable.Seq(address))

      context.actorSelection(address.toString + "/user/loadcontroller")
  }

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

    seedPort.foreach { _ =>
      //Each 5 seconds ask the monitor state to all executor monitors
      context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(1, SECONDS)) {
        router.route(ControllerMonitorRequest(), self)
      }
      //And save the last state of the metrics received by the executor monitors
      context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(1, SECONDS)) {
        //println("Msg/s (average 5 secs): " + sumTotalRequests + ",failures: " + sumTotalFailures)
        msgPerSecond = sumTotalRequests
        failuresperSecond = sumTotalFailures
        sumTotalFailures = 0
        sumTotalRequests = 0
      }
    }
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      activeExecutors = activeExecutors + 1
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info(
        "Member is Removed: {} after {}",
        member.address, previousStatus)
      activeExecutors = activeExecutors - 1
    case AddScheduler =>
      log.info("Adding LoadScheduler to LoadController: {}", sender().path)
      router = router.addRoutee(sender())
    case RemoveScheduler =>
      log.info("Removing LoadScheduler from LoadController: {}", sender().path)
      router = router.removeRoutee(sender())
    case BroadcastedMessage =>
      log.info("Received a broadcasted Message")
    case ControllerMonitorRequestReply(totalRequestRate,totalFailureRate) =>
      sumTotalRequests = sumTotalRequests + totalRequestRate
      sumTotalFailures = sumTotalFailures + totalFailureRate
    case SendSoldiers(num) => {
      println("Master is sending soldiers to executors, total of " + num)
      numWorkersPerExecutor = num
      router.route(SendSoldiers(num), self)
    } //Send to all executors
    case SoldiersMetrics() => {
      sender ! SoldiersMetricsReply(msgPerSecond,failuresperSecond,numWorkersPerExecutor)
    }
    case ClusterStatus() => {
      println("Cluster status requested at Controller")
      val members = cluster.state.members
      val pattern = """([a-z:\/.]*)@([0-9.]*):([0-9]*)$""".r

      val status = members.collect {

        case member  =>
          val address = member.address.toString
          val matched = pattern.findFirstMatchIn(address)


          matched match {
            //val agentType = "WORKER"

            case  Some(m) => {
              println("running " + m.group(3).toInt)
              val agentType = m.group(3).toInt match {
                case `configuredMasterPort` => "Master"
                case `configuredShellPort` => "Shell"
                case _ => "Worker"

              }
              AgentStatus(m.group(2).toString,m.group(3).toInt,agentType,member.status.toString)
            }
            //(m.group(2).toString, m.group(3).toInt , member.status.toString)
            //case None => AgentStatus("dfsf",1234,"HALO","UP")
        }
      }
      val cs = new NamedList[AgentStatus](name = "clusterstatus", items = status.toList)
      sender ! ClusterStatusReply(cs)


    }
    case _: MemberEvent => // ignore
  }
}
