
name := "armee"

//scalaVersion := "2.12.1"
scalaVersion := "2.11.7"

version := "0.0.1-SNAPSHOT"

persistLauncher in Compile := true

persistLauncher in Test := false

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "com.typesafe.akka" %% "akka-actor" % "2.5.2",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.2"
)

libraryDependencies ++= Seq("org.yaml" % "snakeyaml" % "1.16")

libraryDependencies += "com.typesafe.akka" % "akka-http_2.11" % "10.0.5"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"

//lazy val root = project.enablePlugins(ScalaJSPlugin)

enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "org.singlespaced" %%% "scalajs-d3" % "0.3.1"
)

libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.1"

skip in packageJSDependencies := false
jsDependencies +=
  "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js"
