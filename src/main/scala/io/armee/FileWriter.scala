package io.armee

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import java.io._

import io.armee.messages.FileWriterMessages.WriteFileMessage

class FileWriter(port : Int,fileName : String) extends Actor with ActorLogging {
  val cluster = Cluster(context.system)
  val outputFile = new PrintWriter(new File(fileName ))

  def receive = {
    case WriteFileMessage(js: String) =>
      outputFile.write(js)
    case _: MemberEvent => // ignore
  }
}


