
val dependencies = Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
)

lazy val `flickr-grabber` = project.in(file("."))
  .settings(
    name := "flickr-grabber",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.0",
    libraryDependencies ++= dependencies
  )
