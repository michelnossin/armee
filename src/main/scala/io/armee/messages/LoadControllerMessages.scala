package io.armee.messages

object LoadControllerMessages {
  case object AddScheduler
  case object RemoveScheduler
  case object BroadcastedMessage
  case class ControllerMonitorRequestReply(totalRequestRate:Int,totalFailureRate:Int)
  case class SoldiersMetrics()
}