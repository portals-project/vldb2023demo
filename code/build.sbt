ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "vldb2023demo",
    libraryDependencies += "org.portals-project" %% "portals-core" % "0.1.0-SNAPSHOT",
    libraryDependencies += "org.portals-project" %% "portals-distributed" % "0.1.0-SNAPSHOT",
    libraryDependencies += "org.portals-project" %% "portals-libraries" % "0.1.0-SNAPSHOT",
  )
