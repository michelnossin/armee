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
  var numTotalRequests,oldTotalRequests = 0
  var numTotalFailures , oldTotalFailures = 0

  //Start send events to executors when starting
  override def preStart(): Unit = {
    val address = Address("akka.tcp", "armee", "127.0.0.1", port)

    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
      val eventGenerators = context.actorSelection(address.toString + "/user/eventgenerator*")
      eventGenerators ! MonitorRequests()
    }
    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
      val diffRequests = (numTotalRequests - oldTotalRequests)/5
      val diffFailures = (numTotalFailures - oldTotalFailures)/5
      println("Msg per sec (avg last 5 sec):" + diffRequests + " , failed: " + diffFailures)
      oldTotalRequests = numTotalRequests
      oldTotalFailures = numTotalFailures
      numTotalRequests = 0
      numTotalFailures = 0
    }
  }

  def receive = {
    case MonitorRequestsReply(num,lost,queueSize) =>

      //log.info("Actor: " + sender + " got " + num + " requests.")
      //println("Actor: " + sender + " got " + num + " requests. Lost: " + lost + " . Queuesize: " + queueSize)
      numTotalRequests = numTotalRequests + num
      numTotalFailures = numTotalFailures + lost
    case _: MemberEvent => // ignore
  }
}


