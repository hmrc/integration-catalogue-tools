import sbt._

object LibDependencies {

  val compile: Seq[ModuleID] = Seq(
    "io.swagger.parser.v3"       % "swagger-parser-v3"       % "2.0.23",
    "org.apache.commons"         % "commons-csv"             % "1.8",
    "org.apache.httpcomponents"  % "httpclient"              % "4.3.1",
    "org.apache.httpcomponents"  % "httpmime"                % "4.3.1",
    "net.liftweb"               %% "lift-json"               % "3.4.3"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"            %% "scalatest"                % "3.2.2",
    "org.mockito"              %% "mockito-scala-scalatest"  % "1.16.29",
    "com.vladsch.flexmark"      % "flexmark-all"             % "0.36.8"
  ).map(_ % Test)
}
