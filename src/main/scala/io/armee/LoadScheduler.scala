package io.armee

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import io.armee.messages.EventGeneratorMessages.{EventRequestEnvelope, JsonEventRequest}
import io.armee.messages.LoadControllerMessages.{AddScheduler, BroadcastedMessage, RemoveScheduler}
import io.armee.messages.LoadSchedulerMessages.JsonEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable
import scala.concurrent.duration.{FiniteDuration, MICROSECONDS, SECONDS}

class LoadScheduler(akkaPort: Int, numWorkers: Int,seedPort: Option[Int]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  //Define roundrobin router for the scheduler so it can reach the event generating executors
  val routees = Vector.fill(numWorkers) {
    val uid = java.util.UUID.randomUUID.toString
    val r = context.system.actorOf(Props(new EventGenerator()), "eventgenerator_" + self.path.name + "_" + uid)
    context watch r
    ActorRefRoutee(r)
  }
  var roundRobinRouter = Router(RoundRobinRoutingLogic(), routees)

  //Lets add 1 monitor for each scheduler
  val uid = java.util.UUID.randomUUID.toString
  val monitor = context.system.actorOf(Props(new LoadMonitor(akkaPort)), "loadmonitor_" + self.path.name + "_" + uid)

  //val address = Address("akka.tcp", "armee", "127.0.0.1", 1337)
  //val test = context.actorSelection(address.toString + "/user/loadcontroller")

  //Tell master to add new scheduler to akka cluster so the master can communicate with this worker node
  val remoteActor = seedPort map {
    port =>
      val address = Address("akka.tcp", "armee", "127.0.0.1", port)
      cluster.joinSeedNodes(immutable.Seq(address))

      context.actorSelection(address.toString + "/user/loadcontroller")
  }
  remoteActor.foreach(_ ! AddScheduler)

  //Start send events to executors when starting
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

    context.system.scheduler.schedule(FiniteDuration(5, SECONDS), FiniteDuration(1000, MICROSECONDS)) {
      roundRobinRouter.route(EventRequestEnvelope(JsonEventRequest()), self)
    }

  }

  override def postStop(): Unit = {
    remoteActor.foreach(_ ! RemoveScheduler)
    cluster.unsubscribe(self)
  }

  def receive = {
    case BroadcastedMessage =>
      log.info("Received a broadcasted Message")
    //case JsonEvent(event) =>
    //  log.info("Received event: " + event)
    case _: MemberEvent => // ignore
  }
}

