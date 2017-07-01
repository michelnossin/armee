package io.armee

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

object Worker extends App {

  if (args.size != 3) {
    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("")
    println("Syntax: start_worker.sh <nr executors> <worker port> <master port> [<masterserver>]")
    println("")
    println("nr Executors = Number of executors to start in current Worker node. (1 - max nr of cores)")
    println("worker port = Free port to be used by the worker. Other executors will take the next free ports ")
    println("master port = Port used by the master. Should be started before the worker nodes")
    println("masterserver = Servername or ip of master if used on remote server (OPTIONAL)")
    println("")
    println("Example for localhost usage having 4 cores, ports 1337 and 1338 are not used:")
    println("start_master.sh 1337")
    println("start_worker.sh 4 1338 1337")
    println("")
    println("Example for cluster usage having 4 cores, ports 1337 and 1338 are not used:")
    println("start_master.sh 1337 (on server : lx1234)")
    println("start_worker.sh 4 1338 1337 lx1234 (on others server or servers)")
    println("")

    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")
    System.exit(1)
  }
  else {
    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("Starting worker node executors ")
    println("")
    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")
  }

  val workerPort = args(1).toInt
  val numExecutors = args(0).toInt
  val masterPort = args.lift(2).map(_.toInt)

  println("Using parameters: [worker_port: " + workerPort + " , executors: " + numExecutors + " , masterport: " + masterPort + "]")

  for (x <- 0 until numExecutors) {
    val p = workerPort + x
    println("Starting executor using port: " + p)
    val system = ActorSystem("armee", ConfigFactory.load().withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(p)))
    Thread.sleep(2000)
    system.actorOf(Props(new LoadScheduler(p, masterPort)), "loadscheduler_" + p)
  }

}
