import java.util.Date
import java.text.SimpleDateFormat

version := "0." + new SimpleDateFormat("yyMMddHHmm").format(new Date())

lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "schedular"

version := "2.4.0"

val akkaVersion = "2.4.0"

val playVersion = "2.4.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


libraryDependencies ++= Seq(
  ws,
  specs2 % Test,
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.4.0-akka-2.3.x",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.quartz-scheduler" % "quartz" % "2.2.2", 
  "com.ning" % "async-http-client" % "1.9.31", 
  "com.typesafe.play" %% "play" % playVersion,
  "com.typesafe.play" %% "play-json" % playVersion,
  "com.typesafe.play" %% "play-ws" % playVersion,
  "com.typesafe.play" %% "play-mailer" % "2.4.1",
  "org.apache.commons" % "commons-email" % "1.4",
  "net.debasishg" %% "redisclient" % "3.1",
  "joda-time" % "joda-time" % "2.9.1",
  "net.elogistics" %% "api-tools" % "2.0.2",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"
)


fork in run := true