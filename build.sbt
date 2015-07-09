import com.typesafe.sbt.SbtProguard._
import com.typesafe.sbt.SbtProguard.ProguardKeys.proguard

name := "fab-nodes"

organization := "alal"

version := "0.1"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

scalaVersion := "2.11.6"

licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/alal/fab-nodes"))


proguardSettings
ProguardKeys.options in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings")
ProguardKeys.options in Proguard += ProguardOptions.keepMain("alal.fabnodes.Main")
javaOptions in (Proguard, proguard) += "-Xmx2G"
javaOptions in (Proguard, proguard) += "-Xss1G"
