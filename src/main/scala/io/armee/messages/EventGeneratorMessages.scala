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

import akka.actor.ActorRef
import akka.dispatch.ControlMessage

object EventGeneratorMessages {
  sealed trait EventRequest
  sealed trait EventTarget

  case class JsonEventRequest() extends EventRequest
  //case class JsonEventRequest(replyTo : ActorRef) extends EventRequest
  case class XmlEventRequest() extends EventRequest

  case class JdbcEventTarget(url : String, user: String, password: String) extends EventTarget

  case class EventRequestEnvelope(er: EventRequest)

  case class MonitorRequests()  extends ControlMessage //gives this message prio

}
