import sbt._
import sbt.Keys._
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.versioning.SbtGitVersioning

lazy val appName = "integration-catalogue-tools"

lazy val scala_212 = "2.12.15"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

inThisBuild(
  List(
    scalaVersion := scala_212,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val root = Project(appName, file("."))
  .enablePlugins(SbtGitVersioning, SbtArtifactory, PackPlugin)
  .settings(
    scalaVersion := scala_212,
    name := appName,
    majorVersion := 1
  )
  .settings(scoverageSettings)
  .settings(libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test)
  .settings(packMain := Map("integration-catalogue-tools" -> "uk.gov.hmrc.integrationcataloguetools.Main"))

lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := ";.*\\.domain\\.models\\..*;uk\\.gov\\.hmrc\\.BuildInfo;.*\\.Routes;.*\\.RoutesPrefix;;Module;GraphiteStartUp;.*\\.Reverse[^.]*",
      ScoverageKeys.coverageMinimumStmtTotal := 75,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true,
      Test / parallelExecution := false
  )
}
