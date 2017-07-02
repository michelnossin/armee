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
import java.io._

import io.armee.messages.FileWriterMessages.WriteFileMessage

class FileWriter(port : Int,fileName : String) extends Actor with ActorLogging {
  val cluster = Cluster(context.system)
  //val outputFile = new PrintWriter(new File(fileName ))

  def receive = {
    case WriteFileMessage(js: String) =>
      //outputFile.write(js)
    case _: MemberEvent => // ignore
  }
}


