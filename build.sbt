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
libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test

enablePlugins(PackPlugin)
packMain := Map("integration-catalogue-tools" -> "uk.gov.hmrc.integrationcataloguetools.Main")

lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := ";.*\\.domain\\.models\\..*;uk\\.gov\\.hmrc\\.BuildInfo;.*\\.Routes;.*\\.RoutesPrefix;;Module;GraphiteStartUp;.*\\.Reverse[^.]*",
      ScoverageKeys.coverageMinimumStmtTotal := 75,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
  )
}
