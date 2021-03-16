lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "uk.gov.hmrc",
      scalaVersion := "2.12.12",
      version := "0.3.0"
    )),
    name := "integration-catalogue-tools"
  )
  .settings(scoverageSettings)

libraryDependencies += "io.swagger.parser.v3" % "swagger-parser-v3" % "2.0.23"
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.8"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.1"
libraryDependencies += "org.apache.httpcomponents" % "httpmime" % "4.3.1"
libraryDependencies += "net.liftweb" %% "lift-json" % "3.4.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
libraryDependencies += "org.mockito" %% "mockito-scala-scalatest" % "1.16.29" % Test

enablePlugins(PackPlugin)
packMain := Map("integration-catalogue-tools" -> "uk.gov.hmrc.integrationcataloguetools.Main")


lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := ";.*\\.domain\\.models\\..*;uk\\.gov\\.hmrc\\.BuildInfo;.*\\.Routes;.*\\.RoutesPrefix;;Module;GraphiteStartUp;.*\\.Reverse[^.]*",
      ScoverageKeys.coverageMinimum := 70,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
  )
}
