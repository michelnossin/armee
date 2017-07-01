package io.armee

import akka.actor.{ActorSystem, Props}
import akka.actor.ActorRef
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.armee.messages.LoadSchedulerMessages.SendSoldiers

import scala.io.StdIn
import com.typesafe.config._
import io.armee.config.YamlConfig
import io.armee.messages.LoadControllerMessages.SoldiersMetrics

object Shell extends App{

  def showMenu(gw: ActorRef): Unit ={
    println("1 Send Soldiers to the battlefield to change the load")
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
  val shellGateway = system.actorOf(Props(new ShellGateWay(e.shellPort, e.masterPort)), "shellgateway_" + e.shellPort)

  showMenu(shellGateway)
}
