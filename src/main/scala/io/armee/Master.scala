package io.armee

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}


object Master extends App {

  val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(args(0).toInt)))
  system.actorOf(Props(new LoadController(args(0).toInt, args.lift(1).map(_.toInt))), "loadcontroller")

}
