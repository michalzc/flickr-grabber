
val akkaVersion = "2.4.14"
val akkaHttpVersion = "10.0.0"
val akkaGroup = "com.typesafe.akka"

val dependencies = Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  akkaGroup %% "akka-stream" % akkaVersion,
  akkaGroup %% "akka-slf4j" % akkaVersion,
  akkaGroup %% "akka-http" % akkaHttpVersion,
  akkaGroup %% "akka-http-xml" % akkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

)

lazy val `flickr-grabber` = project.in(file("."))
  .settings(
    name := "flickr-grabber",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.0",
    libraryDependencies ++= dependencies
  )
