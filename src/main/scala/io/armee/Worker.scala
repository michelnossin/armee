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

import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.armee.config.YamlConfig

object Worker extends App {
  println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
  println("Starting worker node executors ")
  println("")
  println("DISCLAIMER: USE AT YOUR OWN RISK")
  println("")

  val yc = new YamlConfig
  val e = yc.readConfig()

  println("Using parameters: " + e)

  val localhost = InetAddress.getLocalHost
  val localIpAddress = localhost.getHostAddress

  for (x <- 0 until e.numExecutorsPerServer) {
    val p = e.workerPort + x
    println("Starting executor using port: " + p)
    val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(p)))
    Thread.sleep(2000)
    system.actorOf(Props(new LoadScheduler(localIpAddress,p, Option(e.masterPort),e.masterServer)), "loadscheduler_" + localIpAddress + "_" + p)
  }

}
