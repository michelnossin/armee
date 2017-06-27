package io.armee

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import io.armee.LoadControllerMessages.{AddScheduler, BroadcastedMessage, RemoveScheduler}

import scala.collection.immutable

class LoadScheduler(akkaPort: Int, seedPort: Option[Int]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var router = Router(BroadcastRoutingLogic(), Vector[ActorRefRoutee]())

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
  }

  remoteActor.foreach(_ ! AddScheduler)

  override def postStop(): Unit = {
    remoteActor.foreach(_ ! RemoveScheduler)
    cluster.unsubscribe(self)
  }

  def receive = {
    case BroadcastedMessage =>
      log.info("Received a broadcasted Message")
    case _: MemberEvent => // ignore
  }
}

