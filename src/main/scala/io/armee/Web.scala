package io.armee

import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.scalajs.js

//sbt compile + fastOptJS to run
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
