package io.armee

import com.sun.prism.image.Coords

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.jquery.jQuery

import util._
import dom.ext._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//import scalatags.Text.all._
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

//sbt clean + fastOptJS to create the jar's in target/<scala version/*.jar
//Open index.html to view

object Web extends js.JSApp {

  var teller = 100
  var mainPage = None

  //index.html
  def main(): Unit = {
    jQuery("#main-button").click(() => mainButtonClick())
    jQuery("#cluster-button").click(() => clusterButtonClick())
    jQuery("#war-button").click(() => warButtonClick())

    mainButtonClick()
  }

  def mainButtonClick(): Unit = {
    jQuery("#appl").html("<p>Welcome to Armee. This web application can be used to control and monitor the load on you Big Data Hub. </p>" +
      "<p>Click cluster status to see the hosts , master, shell and executor workers running across the cluster. </p>")
  }

  @js.native
  trait MyStruct extends js.Object {
    val host: String = js.native
    val port: Integer = js.native
    val typeAgent: String = js.native
    val state: String = js.native
  }

  def parseJson(jsn: String): String = {
    val agents = js.JSON.parse(jsn).agents.asInstanceOf[js.Array[MyStruct]]
    val agentsHtml = agents.map(x => "<tr><td>" + x.host.toString + "</td><td>" + x.port.toString + "</td><td>" + x.typeAgent.toString + "</td><td>" + x.state.toString + "</td></tr>").mkString("")
    "<table border='1'>" + "<tr><th>Host</th><th>Port</th><th>Role</th><th>State</th></tr>" + agentsHtml + "</table>"
  }


  def clusterButtonClick(): Unit = {
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

  def warButtonClick(): Unit = {
    jQuery("#appl").html("<p>Preparing the master war room for battle testing....</p>")

    jQuery("#appl").html("""<span id="memoryGaugeContainer"></span> <span id="cpuGaugeContainer"></span> <span id="networkGaugeContainer"></span> <span id="testGaugeContainer"></span>""")

    //var matrix = js.Array(  js.Dynamic.literal(date = "24-Apr-07", close = 100),js.Dynamic.literal(date = "25-Apr-07", close = 200))
    //val svg = d3.select("body").append("svg")
    //val sel = svg.selectAll("#appl").data(matrix).enter()

    js.Dynamic.global.startDraw() //line chart init
    js.Dynamic.global.initialize() //gauge init

    var counter = 0

    js.timers.setInterval(1000) {

      val url = "http://localhost:1335/soldiersmetrics" //TODO change masterserver and api port in config
      val f=Ajax.post(url)

      f.onComplete{
        case Success(xhr) => {
          val msgPerSecondJson = js.JSON.parse(xhr.responseText.toString)
          val msgPerSecond = msgPerSecondJson.soldiers.msgPerSecond
          //val failedPerSecond =  msgPerSecondJson.soldiers.failuresperSecond
          js.Dynamic.global.updateGauge("cpu",msgPerSecond)
          //js.Dynamic.global.updateGauge("memory",failedPerSecond)

          //js.Dynamic.global.data.addRow(js.Array(counter.toString,msgPerSecond,200))
          counter = counter + 1
          js.Dynamic.global.data.addRow(js.Array(counter.toString,
            msgPerSecond.asInstanceOf[Int],0)) //failedPerSecond.asInstanceOf[Int]
          js.Dynamic.global.startDraw()

        }
        case Failure(e) => jQuery("#appl").html("<p>Failed: " + e.toString + "</p>") //df
      }
    }
  }
}

//// The following code was contained in the callback function.
//x.domain(data.map(function(d) { return d.letter; }));
//y.domain([0, d3.max(data, function(d) { return d.frequency; })]);

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




        <script>

    var svg = d3.select("svg"),
        margin = {top: 20, right: 20, bottom: 30, left: 50},
        width = +svg.attr("width") - margin.left - margin.right,
        height = +svg.attr("height") - margin.top - margin.bottom,
        g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var parseTime = d3.timeParse("%d-%b-%y");

    var x = d3.scaleTime()
        .rangeRound([0, width]);

    var y = d3.scaleLinear()
        .rangeRound([height, 0]);

    var area = d3.area()
        .x(function(d) { return x(d.date); })
        .y1(function(d) { return y(d.close); });

    d3.tsv("data.tsv", function(d) {
      d.date = parseTime(d.date);
      d.close = +d.close;
      return d;
    }, function(error, data) {
      if (error) throw error;

      x.domain(d3.extent(data, function(d) { return d.date; }));
      y.domain([0, d3.max(data, function(d) { return d.close; })]);
      area.y0(y(0));

      g.append("path")
          .datum(data)
          .attr("fill", "steelblue")
          .attr("d", area);

      g.append("g")
          .attr("transform", "translate(0," + height + ")")
          .call(d3.axisBottom(x));

      g.append("g")
          .call(d3.axisLeft(y))
        .append("text")
          .attr("fill", "#000")
          .attr("transform", "rotate(-90)")
          .attr("y", 6)
          .attr("dy", "0.71em")
          .attr("text-anchor", "end")
          .text("Price ($)");
    });

    </script>


*/