name := "sansible-examples"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  version := "1.0",
  scalacOptions += "-deprecation",
  version := "1.0-SNAPSHOT",
  organization := "com.citycontext",
  libraryDependencies ++= Seq(
    "com.citycontext" %% "ansible" % "1.0-SNAPSHOT",
    "com.typesafe" % "config" % "1.3.0"
  )
)

lazy val root =
  (project in file(".")).
    aggregate(gitbucket, elasticsearch)

val common = (project in file("common")).settings(commonSettings)

lazy val gitbucket = (project in file("gitbucket")).
  settings(commonSettings: _*).
  dependsOn(common)

lazy val elasticsearch = (project in file("elasticsearch")).
  settings(commonSettings: _*).
  settings(libraryDependencies ++= Seq(
    "com.myjeeva.digitalocean" % "digitalocean-api-client" % "2.4"
  )).
  dependsOn(common)

