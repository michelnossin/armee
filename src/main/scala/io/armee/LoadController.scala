package io.armee

import io.armee.messages.LoadControllerMessages._
import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LoadController(seedPort: Option[Int]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  var router = Router(BroadcastRoutingLogic(), Vector[ActorRefRoutee]())

  //Add this controller to the controller group which acts ass the seed group for this akka cluster
  val remoteActor = seedPort map {
    port =>
      val address = Address("akka.tcp", "armee", "127.0.0.1", port)
      cluster.joinSeedNodes(immutable.Seq(address))

      context.actorSelection(address.toString + "/user/loadcontroller")
  }

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

    seedPort.foreach { _ =>
      context.system.scheduler.schedule(FiniteDuration(20, SECONDS), FiniteDuration(20, SECONDS)) {
        router.route(BroadcastedMessage, self)
      }
    }
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info(
        "Member is Removed: {} after {}",
        member.address, previousStatus)
    case AddScheduler =>
      log.info("Adding LoadScheduler to LoadController: {}", sender().path)
      router = router.addRoutee(sender())
    case RemoveScheduler =>
      log.info("Removing LoadScheduler from LoadController: {}", sender().path)
      router = router.removeRoutee(sender())
    case BroadcastedMessage =>
      log.info("Received a broadcasted Message")
    case _: MemberEvent => // ignore
  }
}
