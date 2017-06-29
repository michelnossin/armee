package io.armee

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import io.armee.messages.EventGeneratorMessages.MonitorRequests
import io.armee.messages.LoadMonitorMessages.MonitorRequestsReply
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, MICROSECONDS, SECONDS}

class LoadMonitor(port : Int) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  //Start send events to executors when starting
  override def preStart(): Unit = {
    val address = Address("akka.tcp", "armee", "127.0.0.1", port)
    val eventGenerators = context.actorSelection(address.toString + "/user/eventgenerator*")

    context.system.scheduler.schedule(FiniteDuration(5, SECONDS), FiniteDuration(5, SECONDS)) {
      eventGenerators ! MonitorRequests()
    }
  }

  def receive = {
    case MonitorRequestsReply(num,lost) =>

      //log.info("Actor: " + sender + " got " + num + " requests.")
      print("Actor: " + sender + " got " + num + " requests. Lost: " + lost)
    case _: MemberEvent => // ignore
  }
}


