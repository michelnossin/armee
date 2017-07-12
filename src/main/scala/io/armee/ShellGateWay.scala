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

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.LoadControllerMessages.{BroadcastedMessage, ClusterStatus, SoldiersMetrics}
import io.armee.messages.LoadSchedulerMessages.SendSoldiers
import io.armee.messages.ShellGateWayMessages.{ClusterStatusReply, SoldiersMetricsReply}

import scala.collection.immutable

class ShellGateWay (shellPort: Int, masterPort: Int,masterServer: String) extends Actor with ActorLogging{
  val cluster = Cluster(context.system)
  val address = Address("akka.tcp", "armee", masterServer, masterPort)
  cluster.joinSeedNodes(immutable.Seq(address))

  val master = context.actorSelection(address.toString + "/user/loadcontroller")

  def receive = {
    case SendSoldiers(num) => {
      //println("User commands soldiers to initiate battle testing. Per executor: " + num)
      master ! SendSoldiers(num)
    }
    case SoldiersMetricsReply(msgPerSecond,failuresperSecond,totalSoldiers) => {
      println("Msg/s : " + msgPerSecond + ",failures: " + failuresperSecond + ", total soldiers per executor: " + totalSoldiers)
    }
    case SoldiersMetrics() => master ! SoldiersMetrics()
    case ClusterStatus() => master ! ClusterStatus()
    case ClusterStatusReply(clusterStatus) => {
      for (agent <- clusterStatus.items) {
        println("Executor: " + agent.host + ":" + agent.port + " (" + agent.typeAgent+ ") , status: " + agent.state)
      }
    }
    case _: MemberEvent => // ignore
  }
}
