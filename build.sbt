organization := "com.lordmancer2"
name := "hero-stream"
version := "1.0.0"
scalaVersion := "2.12.2"

scalacOptions ++= List(
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= {
  val akkaV = "2.5.3"
  val akkaHttpV = "10.0.8"
  Seq("com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}
        