package io.armee

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}


object Master extends App {

  if (args.size != 1) {
    println ("Syntax: start_master.sh <master port>")
    println ("")
    println ("master port = Port used by the master. Should be started before the worker nodes")
    System.exit(1)
  }
  else println("Welcome to Armee. Starting master node.")

  val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(args(0).toInt)))
  system.actorOf(Props(new LoadController( args.lift(0).map(_.toInt))), "loadcontroller")

}
