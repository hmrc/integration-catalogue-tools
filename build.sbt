val playWSClientVersion = "2.1.2"
// val playVersion = "2.8"
val akkaVersion = "2.6.12"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "uk.gov.hmrc",
      scalaVersion := "2.13.4",
      version := "0.3.0"
    )),
    name := "integration-catalogue-tools"
  )

libraryDependencies += "io.swagger.parser.v3" % "swagger-parser-v3" % "2.0.23"
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.8"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test

libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % playWSClientVersion
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % playWSClientVersion
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-xml" % playWSClientVersion

// libraryDependencies += "com.typesafe.play" %% "play.mvc" % playVersion

// libraryDependencies += ws

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)


enablePlugins(PackPlugin)
packMain := Map("integration-catalogue-tools" -> "Main")
