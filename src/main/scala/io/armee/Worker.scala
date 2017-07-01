package io.armee

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

  for (x <- 0 until e.numExecutorsPerServer) {
    val p = e.workerPort + x
    println("Starting executor using port: " + p)
    val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(p)))
    Thread.sleep(2000)
    system.actorOf(Props(new LoadScheduler(p, Option(e.masterPort))), "loadscheduler_" + p)
  }

}
