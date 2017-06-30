package io.armee

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}


object Master extends App {

  if (args.size != 1) {
    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")
    println ("Syntax: start_master.sh <master port>")
    println ("master port = Port used by the master. Should be started before the worker nodes")
    println("")
    println("Example: start_master.sh 1337 (afterwards use start_worker.sh locally or remote server(s)")
    System.exit(1)
  }
  else {
    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("")
    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")
  }

  val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(args(0).toInt)))
  system.actorOf(Props(new LoadController( args.lift(0).map(_.toInt))), "loadcontroller")

}
