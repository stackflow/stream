organization := "com.lordmancer2"
name := "hero-stream"
version := "1.0.0"
scalaVersion := "2.12.2"

scalacOptions ++= List(
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= {
  val akkaHttpV = "10.0.9"
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}
        