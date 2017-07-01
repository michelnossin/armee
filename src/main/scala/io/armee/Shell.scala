package io.armee

import akka.actor.{ActorSystem, Props}
import akka.actor.ActorRef
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.armee.messages.LoadSchedulerMessages.{SendSoldiers}
import scala.io.StdIn
import com.typesafe.config._

object Shell extends App{

  def showMenu(gw: ActorRef): Unit ={
    println("1 Send Soldiers to start or change the load [0 - <any number>]")
    println("2 Monitor load created by soldiers")
    println("3 Exit shell")
    print("Armee shell>")
    StdIn.readLine() match {
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
      case "3" => System.exit(1)
    }
  }
  if (args.size > 2) {
    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("")
    println("Syntax: start_shell.sh [<shell port>] [<master port>] [<masterserver>]")
    println("")
    println("Shell port = Port to used by this shell. Default port 1336")
    println("master port = Port used by the master. Default port used is 1337. Master should be started first")
    println("masterserver = Servername or ip of master if used on remote server (OPTIONAL)")
    println("")
    println("start_shell.sh 1336 1337")
    println("")

    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")
    System.exit(1)
  }
  else {
    println("Welcome to Armee (C) 2017 Michel Nossin. For more information : Armee.io. ")
    println("Starting shell.... ")
    println("")
    println("DISCLAIMER: USE AT YOUR OWN RISK")
    println("")
  }

  /*
  val shellPort = toInt(args(0)) match {
    case Some(i) => i
    case None => 1336
  }

  val masterPort = toInt(args(1)) match {
    case Some(i) => i
    case None => 1337
  }
  */
  val masterPort = args(1).toInt
  val shellPort = args(0).toInt

  val config = ConfigFactory.load()
    .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(shellPort))

  val system = ActorSystem("armee", config)
  val shellGateway = system.actorOf(Props(new ShellGateWay(shellPort, masterPort)), "shellgateway_" + shellPort)

  showMenu(shellGateway)
  //shellGateway ! SendSoldiers(20000)
}
