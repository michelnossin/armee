package io.armee

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

object Worker extends App {

  if (args.size != 3) {
    println ("Syntax: start_worker.sh <nr workers> <worker port> <master port>")
    println ("")
    println ("nr Workers = Number of executors to start in current Worker node. (0 - max nr of cores)")
    println ("worker port = Free port to be used by the worker")
    println ("master port = Port used by the master. Should be started before the worker nodes")
    System.exit(1)
  }
  else println("Welcome to Armee. Starting worker node ")

  val workerPort = args(1).toInt
  val numWorkers = args(0).toInt

  val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(args(1).toInt)))

  println("Starting worker using port: " + workerPort)
  system.actorOf(Props(new LoadScheduler(workerPort, numWorkers,args.lift(2).map(_.toInt))), "loadscheduler_" + workerPort)

}
