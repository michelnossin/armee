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

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.armee.config.YamlConfig
import akka.actor.ActorRef
import io.armee.messages.LoadControllerMessages.{AgentStatus, ClusterStatus, NamedList}
import akka.pattern.ask
import akka.util.Timeout
import io.armee.messages.LoadControllerMessages.OrderJsonSupport
import io.armee.messages.ShellGateWayMessages.ClusterStatusReply
import scala.language.postfixOps
import scala.concurrent.Await
import spray.json._
import DefaultJsonProtocol._
import scala.concurrent.duration._

//curl http://<master-node>:<masterport>/clusterstatus eg http://loalhost:1335/clusterstatus
//curl -X POST --data 'Akka Http is Cool' http://<master-node>:1335/v1/id/ALICE
class ApiServer(controller : ActorRef ) extends HttpApp with OrderJsonSupport {
  def route =
    pathPrefix("v1") {
      path("clusterstatus")  {
        get {
          implicit val timeout = Timeout(3 seconds)
          val clusterStatus = controller ? ClusterStatus()
          val result = Await.result(clusterStatus, timeout.duration).asInstanceOf[ClusterStatusReply]

          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, {
            val lijst = result.status.items.collect { case x => x.toJson(agentWriter).toString }
            """ { "agents" : [""" + lijst.mkString(",") + "] }"
          })) //~
            //post {
            //  entity(as[String]) { entity =>
            //    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<b>Thanks $id for posting your message <i>$entity</i></b>"))
            //  }
            //}
        }
      }
    }
}

object Master extends App {
  println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
  println("")
  println("DISCLAIMER: USE AT YOUR OWN RISK. Read the LICENSE file in dit directory.")
  println("")

  val yc = new YamlConfig
  val e = yc.readConfig()

  val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port",
    ConfigValueFactory.fromAnyRef(e.masterPort)))
  val controller = system.actorOf(Props(new LoadController(Option(e.masterPort),e.masterServer,e )), "loadcontroller")

  new ApiServer(controller).startServer(e.masterServer, e.apiPort, ServerSettings(ConfigFactory.load))

  //val resourceHandler = system.actorOf(Props(new ResourceHandler(e.workerPort,"/tmp/output.json")), "filewriter_" + localIpAddress + "_" + uid)


}
