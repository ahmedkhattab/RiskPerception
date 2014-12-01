name := """RiskPerception"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
     "org.webjars" %% "webjars-play" % "2.3.0-2",
  // Downgrade to JQuery 1.8.3 so that integration tests with HtmlUnit work.
  "org.webjars" % "bootstrap" % "3.0.0" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "1.8.3",
  "org.webjars" % "jquery-ui" % "1.11.1",
  "org.webjars" % "jquery-file-upload" % "9.8.0",
  "org.webjars" % "highcharts" % "4.0.4"
)

resolvers ++= Seq(
  "webjars" at "http://webjars.github.com/m2"
)