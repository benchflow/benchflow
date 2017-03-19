name := """data-manager"""

version := "dev"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

// scalaz-bintray resolver needed for specs2 library
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers ++= Seq(
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  Resolver.bintrayRepo("websudos", "oss-releases")
)

val phantomVersion = "1.22.0"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
  ws, // Play's web services module
  specs2 % Test,
  "org.specs2" %% "specs2-matcher-extra" % "3.6" % Test,
  "org.easytesting" % "fest-assert" % "1.4" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.7" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.7" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.4.7",
  "org.webjars" % "bootstrap" % "2.3.2",
  "org.webjars" % "flot" % "0.8.0",
  "com.google.api-client" % "google-api-client" % "1.22.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0",
  "com.google.apis" % "google-api-services-drive" % "v3-rev46-1.22.0",
  "com.websudos" %% "phantom-dsl" % phantomVersion,
  "com.websudos" %% "phantom-reactivestreams" % phantomVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.5",
  "io.minio" % "minio" % "2.0.4"
)

routesGenerator := InjectedRoutesGenerator

fork in run := true

