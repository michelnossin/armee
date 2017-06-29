package io.armee.messages

/**
  * Created by michelnossin on 29-06-17.
  */
object LoadMonitorMessages {
  case class MonitorRequestsReply(num: Int,lost: Int,queueSize : Int)
}
