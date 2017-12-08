
enablePlugins(PackPlugin)

organization := "edu.cmu.ml.rtw"

name := "scopa_pra"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:unchecked")

javaOptions in run ++= Seq("-Xmx10g")

//javaOptions ++= Seq("-agentpath:/home/mattg/clone/yjp-2015-build-15084/bin/linux-x86-64/libyjpagent.so=delay=10000")

crossScalaVersions := Seq("2.11.2", "2.10.3")

libraryDependencies ++= Seq(
  // Java utility libraries (collections, option parsing, such things)
  "com.google.guava" % "guava" % "17.0",
  "log4j" % "log4j" % "1.2.16",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-compress" % "1.9",
  // Scala utility libraries
  "org.json4s" %% "json4s-native" % "3.2.11",
  "edu.cmu.ml.rtw" %% "matt-util" % "2.3.2",
  // Matrix stuff, both for java and scala
  "net.sf.trove4j" % "trove4j" % "2.0.2",
  "org.scalanlp" %% "breeze" % "0.10",
  "org.scalanlp" %% "breeze-natives" % "0.10",
  // MALLET, for optimization
  "cc.mallet" % "mallet" % "2.0.7",
  // GraphChi
  "org.graphchi" %% "graphchi-java" % "0.2.2",
  // Testing dependencies
  "com.novocode" % "junit-interface" % "0.11",
  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

