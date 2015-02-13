import Dependencies._

lazy val commonSettings = Seq(
  organization := "org.agmip.tools",
  name := "data-inspector",
  version := "0.1.0",
  scalaVersion := "2.11.4",
  resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

lazy val root = (project in file(".")).
  settings(commonSettings:_*).
  settings(libraryDependencies ++= allDeps)
