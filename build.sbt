name := "reporting-app"
organization in ThisBuild := "io.novakovalexey"
scalaVersion in ThisBuild := "2.12.7"

lazy val global = project
  .in(file("."))
  .aggregate(`gen-data`, `api-search`)
  .settings(publishArtifact := false)

val commonSettings = dockerBaseImage := "openjdk:8-jre-alpine"
val commonPlugins = Seq(JavaAppPackaging, AshScriptPlugin)

lazy val `gen-data` = project
  .settings(libraryDependencies ++= Seq(requests, argonaut, scalacheck, scalaTest, cats), commonSettings)
  .enablePlugins(commonPlugins: _*)

lazy val `api-search` = project
  .settings(
    libraryDependencies ++= Seq(
      scalaTest,
      pureConfig,
      akkaHttp,
      akka,
      akkaStreams,
      akkaHttpTestKit,
      circeCore,
      circeGeneric,
      circeParser,
      elastic4sCore,
      elastic4sHttp,
      scalaLogging,
      logbackClassic,
      slf4jApi,
      elastic4sTestKit,
      elastic4sEmbedded,
      log4jForEsTests,
      log4jCoreForEsTests
    ),
    commonSettings,
    Test / fork := true,
    Test / javaOptions += "-Xmx4G"
  )
  .enablePlugins(commonPlugins: _*)
