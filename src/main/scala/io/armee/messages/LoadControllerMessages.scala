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
package io.armee.messages

import spray.json.{DefaultJsonProtocol, JsonFormat}
import spray.json._
import DefaultJsonProtocol._
import sun.management.resources.agent

object LoadControllerMessages {

  case object AddScheduler

  case object RemoveScheduler

  case object BroadcastedMessage

  case class ControllerMonitorRequestReply(totalRequestRate: Int, totalFailureRate: Int)

  case class SoldiersMetrics()

  case class ClusterStatus()

  case class AgentStatus(host: String, port: Int, agentType: String, state: String)
  case class NamedList[A](name: String, items: List[A])

  trait OrderJsonSupport extends DefaultJsonProtocol {
    implicit val agentWriter: JsonWriter[AgentStatus] = {
      new JsonWriter[AgentStatus] {
        def write(agentStatus: AgentStatus): JsValue =
          JsObject(List(
            "host" -> agentStatus.host.toJson,
            "port" -> agentStatus.port.toJson,
            "type" -> agentStatus.agentType.toJson,
            "state" -> agentStatus.state.toJson

          ))
      }
    }
  }
}