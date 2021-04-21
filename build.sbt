import sbt._
import sbt.Keys._
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.versioning.SbtGitVersioning

lazy val root = (project in file("."))
  .enablePlugins(SbtGitVersioning, SbtArtifactory)
  .settings(
    scalaVersion := "2.12.12",
    name := "integration-catalogue-tools",
    majorVersion := 1
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
