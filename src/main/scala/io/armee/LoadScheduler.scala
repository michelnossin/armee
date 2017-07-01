package io.armee

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.routing._
import io.armee.messages.EventGeneratorMessages.{EventRequestEnvelope, JsonEventRequest}
import io.armee.messages.LoadControllerMessages.{AddScheduler, BroadcastedMessage, RemoveScheduler}
import io.armee.messages.LoadSchedulerMessages.{JsonEvent, SendSoldiers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable
import scala.concurrent.duration.{FiniteDuration, MICROSECONDS, NANOSECONDS, SECONDS}

class LoadScheduler(akkaPort: Int, seedPort: Option[Int]) extends Actor with ActorLogging {

  var numSoldiers = 0

  print("Executor starting up with port: " + akkaPort)
  val cluster = Cluster(context.system)

  var broadCastRouter = Router(BroadcastRoutingLogic())
  var roundRobinRouter = Router(RoundRobinRoutingLogic())

  //Lets add 1 monitor for each scheduler and one output filewriter

  val uid = java.util.UUID.randomUUID.toString
  val monitor = context.system.actorOf(Props(new LoadMonitor(akkaPort,seedPort)), "loadmonitor_" + self.path.name + "_" + uid)
  val fileWriter = context.system.actorOf(Props(new FileWriter(akkaPort,"/tmp/armee/" + self.path.name + ".json")), "filewriter_" + self.path.name + "_" + uid)

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

  }

  override def postStop(): Unit = {
    remoteActor.foreach(_ ! RemoveScheduler)
    cluster.unsubscribe(self)
  }

  def sendSoldiers (num: Int): Unit = {
    numSoldiers = num

    val routees = Vector.fill(numSoldiers) {
      val uid = java.util.UUID.randomUUID.toString
      val r = context.system.actorOf(Props(new EventGenerator()), "eventgenerator_" + self.path.name + "_" + uid)
      context watch r
      ActorRefRoutee(r)
    }
    broadCastRouter = Router(BroadcastRoutingLogic(), routees)
    roundRobinRouter = Router(RoundRobinRoutingLogic(), routees)

    context.system.scheduler.schedule(FiniteDuration(1, SECONDS), FiniteDuration(10, NANOSECONDS)) {
      //roundRobinRouter.route(EventRequestEnvelope(JsonEventRequest()), self)
      broadCastRouter.route(EventRequestEnvelope(JsonEventRequest(fileWriter)), self)
    }
  }

  def receive = {
    case SendSoldiers(num) => {
      println("Executor " + self.path.name + " received soldiers, total of : " + num)
      sendSoldiers(num)
    }
    case BroadcastedMessage =>
      log.info("Received a broadcasted Message")
    case _: MemberEvent => // ignore
  }
}

