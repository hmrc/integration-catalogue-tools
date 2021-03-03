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
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.1"
libraryDependencies += "org.apache.httpcomponents" % "httpmime" % "4.3.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
libraryDependencies += "org.mockito" %% "mockito-scala-scalatest" % "1.16.29" % Test

enablePlugins(PackPlugin)
packMain := Map("integration-catalogue-tools" -> "uk.gov.hmrc.integrationcataloguetools.Main")
