package io.armee

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.armee.config.YamlConfig

object Master extends App {
  println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
  println("")
  println("DISCLAIMER: USE AT YOUR OWN RISK")
  println("")

  val yc = new YamlConfig
  val e = yc.readConfig()

  val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(e.masterPort)))
  system.actorOf(Props(new LoadController(Option(e.masterPort) )), "loadcontroller")

}
