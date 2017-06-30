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
  var deltaTotalRequests,deltaTotalFailures = 0

  //Start send events to executors when starting
  override def preStart(): Unit = {
    val address = Address("akka.tcp", "armee", "127.0.0.1", port)

    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
      val eventGenerators = context.actorSelection(address.toString + "/user/eventgenerator*")
      eventGenerators ! MonitorRequests()
    }
    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
      val diffRequests = (deltaTotalRequests)/5
      val diffFailures = (deltaTotalFailures)/5
      println("Msg per sec (avg last 5 sec):" + diffRequests + " , failed: " + diffFailures)
      deltaTotalRequests = 0
      deltaTotalFailures = 0
    }
  }

  def receive = {
    case MonitorRequestsReply(num,lost,queueSize) =>

      //log.info("Actor: " + sender + " got " + num + " requests.")
      //println("Actor: " + sender + " got " + num + " requests. Lost: " + lost + " . Queuesize: " + queueSize)
      deltaTotalRequests = deltaTotalRequests + num
      deltaTotalFailures = deltaTotalFailures + lost
    case _: MemberEvent => // ignore
  }
}


