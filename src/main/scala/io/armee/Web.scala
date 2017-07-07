package io.armee

import akka.http.scaladsl.model.headers.{Allow, Origin}

import scala.scalajs.js
import org.scalajs.dom
import dom.document

import scala.scalajs.js.annotation.JSExportTopLevel
import org.scalajs.jquery.jQuery

import util._
import dom.ext._
import io.armee.messages.LoadControllerMessages.AgentStatus
import sun.management.resources.agent

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//import scalatags.Text.all._

//sbt clean + fastOptJS to create the jar's in target/<scala version/*.jar
//Open index.html to view

object Web extends js.JSApp {

  var mainPage = None

  //index.html
  def main(): Unit = {
    jQuery("#main-button").click(() => mainButtonClick())
    jQuery("#cluster-button").click(() => clusterButtonClick())

    mainButtonClick()
  }

  def mainButtonClick(): Unit = {
    jQuery("#appl").html("<p>Welcome to Armee. This web application can be used to control and monitor the load on you Big Data Hub. </p>" +
    "<p>Click cluster status to see the hosts , master, shell and executor workers running across the cluster. </p>")
  }

  @js.native
  trait MyStruct extends js.Object {
    val host : String = js.native
    val port : Integer = js.native
    val typeAgent : String = js.native
    val state : String = js.native
  }

  def parseJson(jsn : String): String = {
    val agents =js.JSON.parse(jsn).agents.asInstanceOf[js.Array[MyStruct]]

    //agents.map(_.host).toString
    //agents.map(x => "<tr><td>" + x.host.toString + "</td><td>" + x.port.toString + "</td><td>" + x.typeAgent.toString + "</td><td>" + x.state.toString + "</td></tr>").mkString("")
    val agentsHtml = agents.map(x => "<tr><td>" + x.host.toString + "</td><td>" + x.port.toString + "</td><td>" + x.typeAgent.toString + "</td><td>" + x.state.toString + "</td></tr>").mkString("")

    "<table border='1'>" +  "<tr><th>Host</th><th>Port</th><th>Role</th><th>State</th></tr>" + agentsHtml + "</table>"
    /*
    val agentsHtml = for (agent <- agents) yield  {
      val hostP = agent.host
      val portP = agent.port
      val typeP = agent.typeAgent
      val statusP = agent.state
      s"<tr><td>$hostP</td><td>$portP</td><td>$typeP</td><td>$statusP</td></tr>"
    }
    s"<table>" + agentsHtml.mkString("") + s"</table>"
    */
  }

  def clusterButtonClick(): Unit = {
    jQuery("#appl").html("<p>Loading cluster status:</p>")

    val url = "http://localhost:1335/clusterstatus"
    //val headersRequest : Map[String,String] = Map("Access-Control-Allow-Origin" -> "*")
    val f=Ajax.post(url)   //'Access-Control-Allow-Origin: *' , url) //headers = headersRequest


    f.onComplete{
      case Success(xhr) => {
        print ("received: " + xhr.responseText.toString)
        jQuery("#appl").html("<p>Status received OK:" + parseJson(xhr.responseText.toString)+ "</p>")
      }
        //val json=js.JSON.parse(xhr.responseText)
        //val body=json.agents.toString.mkString(" ")
        //jQuery("#appl").html("<p>OK:" + json + "</p>")
      case Failure(e) => jQuery("#appl").html("<p>Failed: " + e.toString + "</p>") //df
    }
  }
}
/*
object ScalaJSExample extends js.JSApp {

  def main(): Unit = {
    /**
      * Adapted from http://thecodingtutorials.blogspot.ch/2012/07/introduction-to-d3.html
      */
    val graphHeight = 450

    //The width of each bar.
    val barWidth = 80

    //The distance between each bar.
    val barSeparation = 10

    //The maximum value of the data.
    val maxData = 50

    //The actual horizontal distance from drawing one bar rectangle to drawing the next.
    val horizontalBarDistance = barWidth + barSeparation

    //The value to multiply each bar's value by to get its height.
    val barHeightMultiplier = graphHeight / maxData;

    //Color for start
    val c = d3.rgb("DarkSlateBlue")

    val rectXFun = (d: Int, i: Int) => i * horizontalBarDistance
    val rectYFun = (d: Int) => graphHeight - d * barHeightMultiplier
    val rectHeightFun = (d: Int) => d * barHeightMultiplier
    val rectColorFun = (d: Int, i: Int) => c.brighter(i * 0.5).toString

    val svg = d3.select("body").append("svg").attr("width", "100%").attr("height", "450px")
    val sel = svg.selectAll("rect").data(js.Array(8, 22, 31, 36, 48, 17, 25))
    sel.enter()
      .append("rect")
      .attr("x", rectXFun)
      .attr("y", rectYFun)
      .attr("width", barWidth)
      .attr("height", rectHeightFun)
      .style("fill", rectColorFun)


  }

}

*/

/*
object ScalaJSExample extends js.JSApp {

  def main(): Unit = {
    /**
      * Adapted from https://github.com/mbostock/d3/wiki/Selections#data
      */

    val matrix = js.Array(
      js.Array(11975,  5871, 8916, 2868),
      js.Array( 1951, 10048, 2060, 6171),
      js.Array( 8010, 16145, 8090, 8045),
      js.Array( 1013,   990,  940, 6907)
    )

    val tr = d3.select("body").append("table").selectAll("tr")
      .data(matrix)
      .enter().append("tr")
    println("hihi")
    val td = tr.selectAll("td")
      .data( (d:js.Array[Int]) => { println(d); d; } )
      .enter().append("td")
      .text( (d:Int) => d.toString)

  }

}

*/