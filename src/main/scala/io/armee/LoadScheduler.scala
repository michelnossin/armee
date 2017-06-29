package io.armee

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import io.armee.messages.EventGeneratorMessages.JsonEventRequest
import io.armee.messages.LoadControllerMessages.{AddScheduler, BroadcastedMessage, RemoveScheduler}
import io.armee.messages.LoadSchedulerMessages.JsonEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable
import scala.concurrent.duration.{FiniteDuration, SECONDS,MICROSECONDS}

class LoadScheduler(akkaPort: Int, numWorkers: Int,seedPort: Option[Int]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var router = Router(BroadcastRoutingLogic(), Vector[ActorRefRoutee]())

  val eventGenerators = for (worker <- 0 until numWorkers) yield {
    println("Starting executor " + worker + " on worker: " + self.path.name)
    context.system.actorOf(Props(new EventGenerator("json")), "eventgenerator_" + self.path.name + "_" + worker)
  }

  //Tell master to add new worker to the cluster
  val remoteActor = seedPort map {
    port =>
      val address = Address("akka.tcp", "armee", "127.0.0.1", port)
      cluster.joinSeedNodes(immutable.Seq(address))

      context.actorSelection(address.toString + "/user/loadcontroller")
  }

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

    context.system.scheduler.schedule(FiniteDuration(5, SECONDS), FiniteDuration(10000, MICROSECONDS)) {
      eventGenerators.foreach(gen => gen ! JsonEventRequest)
    }
  }

  remoteActor.foreach(_ ! AddScheduler)

  override def postStop(): Unit = {
    remoteActor.foreach(_ ! RemoveScheduler)
    cluster.unsubscribe(self)
  }

  def receive = {
    case BroadcastedMessage =>
      log.info("Received a broadcasted Message")
    case JsonEvent(event) =>
      log.info("Received event: " + event)
    case _: MemberEvent => // ignore
  }
}

