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
    "org.scala-stm" %% "scala-stm" % "0.8",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.9",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.apache.kafka" % "kafka-clients" % "0.9.0.1"
  )
}
        