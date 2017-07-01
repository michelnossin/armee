package io.armee

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.LoadControllerMessages.{BroadcastedMessage, SoldiersMetrics}
import io.armee.messages.LoadSchedulerMessages.SendSoldiers
import io.armee.messages.ShellGateWayMessages.SoldiersMetricsReply

import scala.collection.immutable

class ShellGateWay (shellPort: Int, masterPort: Int) extends Actor with ActorLogging{
  val cluster = Cluster(context.system)
  val address = Address("akka.tcp", "armee", "127.0.0.1", masterPort)
  cluster.joinSeedNodes(immutable.Seq(address))

  val master = context.actorSelection(address.toString + "/user/loadcontroller")

  def receive = {
    case SendSoldiers(num) => {
      //println("User commands soldiers to initiate battle testing. Per executor: " + num)
      master ! SendSoldiers(num)
    }
    case SoldiersMetricsReply(msgPerSecond,failuresperSecond) => {
      println("Msg/s (average 5 secs): " + msgPerSecond + ",failures: " + failuresperSecond)
    }
    case SoldiersMetrics() => master ! SoldiersMetrics()
    case _: MemberEvent => // ignore
  }
}
