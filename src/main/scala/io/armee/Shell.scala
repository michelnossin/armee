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
import akka.actor.ActorRef
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.armee.messages.LoadSchedulerMessages.SendSoldiers

import scala.io.StdIn
import io.armee.config.YamlConfig
import io.armee.messages.LoadControllerMessages.{ClusterStatus, SoldiersMetrics}

object Shell extends App{

  def showMenu(gw: ActorRef): Unit ={
    println("0 Get cluster status")
    println("1 Send Soldiers to the battlefield to change the load")
    println("2 Monitor load created by soldiers")
    println("3 Exit shell")
    print("Armee shell>")
    StdIn.readLine() match {
      case "0" => {
        println("")
        gw ! ClusterStatus()
        Thread.sleep(200)
        println("")
        showMenu(gw)
      }
      case "1" => {
        print("How many soldiers: ")
        StdIn.readLine() match {
          case num => {
            gw ! SendSoldiers(num.toInt)
            println("Master, the command was given to send " + num.toInt + " soldiers to each executor!")
            println("")
            showMenu(gw)
          }
        }
      }
      case "2" => {
        gw ! SoldiersMetrics()
        println("")
        Thread.sleep(200)
        println("")
        showMenu(gw)
      }
      case "3" => System.exit(0)
    }
  }

    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("Starting shell.... ")
    println("")
    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")

  val yc = new YamlConfig
  val e = yc.readConfig()


  val config = ConfigFactory.load()
    .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(e.shellPort))

  val system = ActorSystem("armee", config)
  val shellGateway = system.actorOf(Props(new ShellGateWay(e.shellPort, e.masterPort,e.masterServer)), "shellgateway_" + e.shellPort)

  showMenu(shellGateway)
}
