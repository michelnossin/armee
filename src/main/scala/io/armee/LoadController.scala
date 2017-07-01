package io.armee

import io.armee.messages.LoadControllerMessages._
import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import io.armee.messages.LoadMonitorMessages.ControllerMonitorRequest
import io.armee.messages.LoadSchedulerMessages.SendSoldiers

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LoadController(seedPort: Option[Int]) extends Actor with ActorLogging {

  var sumTotalRequests,sumTotalFailures,msgPerSecond,failuresperSecond = 0 //for monitoring

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
      //Each 5 seconds ask the monitor state to all executor monitors
      context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
        router.route(ControllerMonitorRequest(), self)
      }
      //And save the last state of the metrics received by the executor monitors
      context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(5, SECONDS)) {
        println("Msg/s (average 5 secs): " + sumTotalRequests + ",failures: " + sumTotalFailures)
        msgPerSecond = sumTotalRequests
        failuresperSecond = sumTotalFailures
        sumTotalFailures = 0
        sumTotalRequests = 0
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
    case ControllerMonitorRequestReply(totalRequestRate,totalFailureRate) =>
      sumTotalRequests = sumTotalRequests + totalRequestRate
      sumTotalFailures = sumTotalFailures + totalFailureRate
    case SendSoldiers(num) => {
      println("Master is sending soldiers to executors, total of " + num)
      router.route(SendSoldiers(num), self)
    } //Send to all executors
    //case SoldiersMetrics() => SoldiersMetricsReply(msgPerSecond,failuresperSecond)
    case _: MemberEvent => // ignore
  }
}
