package io.armee

import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.scalajs.js

//sbt clean + fastOptJS to create the jar's in target/<scala version/*.jar
//Open index.html to view

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
