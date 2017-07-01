package io.armee.messages

object ShellGateWayMessages {
  case class SoldiersMetricsReply(msgPerSecond : Int,failuresperSecond: Int)
}
