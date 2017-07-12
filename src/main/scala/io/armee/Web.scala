package io.armee

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.jquery.jQuery
import util._
import dom.ext._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

//To test changes in thos class: sbt clean + fastOptJS (or ~fastOptJS to automatically compile) to create the
// scala.js jar's in target/<scala version/*.jar. Use curl or the provided webapp (See readme.md) to test the api's / webapp

object Web extends js.JSApp {
  var interval: js.UndefOr[js.timers.SetIntervalHandle] = js.undefined
  var activeSoldiers,counter = 0

  //index.html
  def main(): Unit = {
    jQuery("#main-button").click(() => mainButtonClick())
    jQuery("#cluster-button").click(() => clusterButtonClick())
    jQuery("#war-button").click(() => warButtonClick())

    mainButtonClick()
  }

  def mainButtonClick(): Unit = {
    interval foreach js.timers.clearInterval
    interval = js.undefined
    jQuery("#appl").html("<p>Welcome to Armee. This web application can be used to create some serious load on your Big Data Hub. </p>" +
      "<p>Click cluster status to see the hosts , master, shell and executor workers running across the cluster. </p>" +
    "<p>The warroom can be used to acive the load on the workers and monitor the load. ")
    jQuery("#chart_div").html("")
  }

  @js.native
  trait MyStruct extends js.Object {
    val host: String = js.native
    val port: Integer = js.native
    val typeAgent: String = js.native
    val state: String = js.native
  }

  //Parse json to html, used for clusterstatus requests
  def parseJson(jsn: String): String = {
    val agents = js.JSON.parse(jsn).agents.asInstanceOf[js.Array[MyStruct]]
    val agentsHtml = agents.map(x => "<tr><td>" + x.host.toString + "</td><td>" + x.port.toString + "</td><td>" + x.typeAgent.toString + "</td><td>" + x.state.toString + "</td></tr>").mkString("")
    "<table border='1'>" + "<tr><th>Host</th><th>Port</th><th>Role</th><th>State</th></tr>" + agentsHtml + "</table>"
  }


  //clusterstatus tab clicked in webapp
  def clusterButtonClick(): Unit = {
    interval foreach js.timers.clearInterval
    interval = js.undefined
    jQuery("#chart_div").html("")
    jQuery("#appl").html("<p>Requesting cluster status....</p>")

    val url = "http://localhost:1335/clusterstatus" //TODO change masterserver and api port in config
    val f = Ajax.post(url)


    f.onComplete {
      case Success(xhr) => {
        print("received: " + xhr.responseText.toString)
        jQuery("#appl").html("<p>Status received OK:" + parseJson(xhr.responseText.toString) + "</p>")
      }
      case Failure(e) => jQuery("#appl").html("<p>Failed: " + e.toString + "</p>") //df
    }

  }

  //The plus or minus button is clicked in webapp (meaning more soldiers/load , or less)
  def increaseSoldiersButtonClick(): Unit = {
    val url = "http://localhost:1335/numsoldiers/" + (activeSoldiers + 1).toString //TODO change to config file values
    val f = Ajax.post(url)
  }
  def decreaseSoldiersButtonClick(): Unit = {
    if (activeSoldiers > 0) {
      val url = "http://localhost:1335/numsoldiers/" + (activeSoldiers - 1).toString  //TODO change to config file values
      val f = Ajax.post(url)
    }
  }

  //The warroom button is clicked in the webapp
  def warButtonClick(): Unit = {
    jQuery("#appl").html("<p>Preparing the master war room for battle testing....</p>")
    jQuery("#appl").html(
      """<span id="memoryGaugeContainer"></span> <span id="cpuGaugeContainer"></span><button height="42" width="42" id="max-button" type="button"><img height="42" width="42" src="./resources/plus.png"></button><button height="42" width="42" id="min-button" type="button" ><img height="42" width="42" src="./resources/minus.png"></button>""".stripMargin)

    jQuery("#max-button").click(() => increaseSoldiersButtonClick())
    jQuery("#min-button").click(() => decreaseSoldiersButtonClick())

    js.Dynamic.global.drawChart() //config line chart
    js.Dynamic.global.startDraw() //and start draw

    js.Dynamic.global.initialize() //gauge init

    counter = 0 //reset timer of the areachart each time the tab in the webapp is clicked

   interval = js.timers.setInterval(1000) {

      val url = "http://localhost:1335/soldiersmetrics" //TODO change masterserver and api port in config
      val f=Ajax.post(url)

      f.onComplete{
        case Success(xhr) => {
          //parse json request of the load monitor api
          val msgPerSecondJson = js.JSON.parse(xhr.responseText.toString)
          val msgPerSecond = msgPerSecondJson.soldiers.msgPerSecond
          val failedPerSecond =  msgPerSecondJson.soldiers.failureperSecond
          val totalSoldiers = msgPerSecondJson.soldiers.totalSoldiers
          activeSoldiers = totalSoldiers.asInstanceOf[Int]

          //Update the gauges
          js.Dynamic.global.updateGauge("cpu",msgPerSecond) //hmm changing the names of the gauges breaks it?
          js.Dynamic.global.updateGauge("memory",failedPerSecond)

          //Update the areachart
          counter = counter + 1
          js.Dynamic.global.data.addRow(js.Array(counter.toString,
            msgPerSecond.asInstanceOf[Int],failedPerSecond.asInstanceOf[Int],totalSoldiers.asInstanceOf[Int]))
          js.Dynamic.global.startDraw()

        }
        case Failure(e) => jQuery("#appl").html("<p>Failed: " + e.toString + "</p>") //df
      }
    }
  }
}
