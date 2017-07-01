
name := "armee"

scalaVersion := "2.12.2"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scala-lang" % "scala-reflect" % "2.12.2",
  "com.typesafe.akka" %% "akka-actor" % "2.5.2",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.2"
)

libraryDependencies ++= Seq("org.yaml" % "snakeyaml" % "1.16")