name := "scala-opengraph"

version := "0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.scalaz" % "scalaz-concurrent_2.11" % "7.1.3"
)

lazy val root = project in file(".")
