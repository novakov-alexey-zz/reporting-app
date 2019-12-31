import sbt._

object Dependencies extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    object DependenciesVersion {
      val catsVersion                      = "1.4.0"
      val circeVersion                     = "0.9.3"
      val logbackClassicVersion            = "1.2.3"
      val pureConfigVersion                = "0.9.1"
      val requestsVersion                  = "0.1.4"
      val scalaTestVersion                 = "3.0.5"
      val slf4jVersion                     = "1.7.25"
      val typesafeConfigVersion            = "1.3.3"
      val scalacheckVersion                = "1.13.4"
      val argonautVersion                  = "6.2.2"
      val akkaVersion                      = "2.5.18"
      val akkaStreamsVersion               = "2.5.18"
      val akkaHttpVersion                  = "10.1.5"
      val scalaLoggingVersion              = "3.9.0"
      val elasticVersion                   = "6.3.7"
      val log4jVersion                     = "2.11.1"
    }

    import DependenciesVersion._
    
    val cats                     = "org.typelevel"             %%  "cats-core"                 % catsVersion
    val circeCore                = "io.circe"                  %% "circe-core"                 % circeVersion
    val circeGeneric             = "io.circe"                  %% "circe-generic"              % circeVersion
    val circeParser              = "io.circe"                  %% "circe-parser"               % circeVersion
    val logbackClassic           = "ch.qos.logback"            %   "logback-classic"           % logbackClassicVersion
    val pureConfig               = "com.github.pureconfig"     %%  "pureconfig"                % pureConfigVersion
    val scalaTest                = "org.scalatest"             %%  "scalatest"                 % scalaTestVersion % Test
    val slf4jApi                 = "org.slf4j"                 %   "slf4j-api"                 % slf4jVersion
    val typesafeConfig           = "com.typesafe"              %   "config"                    % typesafeConfigVersion
    val requests                 = "com.lihaoyi"               %%  "requests"                  % requestsVersion
    val scalacheck               = "org.scalacheck"            %% "scalacheck"                 % scalacheckVersion
    val argonaut                 = "io.argonaut"               %% "argonaut"                   % argonautVersion
    val akka                     = "com.typesafe.akka"         %% "akka-actor"                 % akkaVersion
    val akkaStreams              = "com.typesafe.akka"         %% "akka-stream"                % akkaStreamsVersion
    val akkaHttp                 = "com.typesafe.akka"         %% "akka-http"                  % akkaHttpVersion
    val akkaHttpTestKit          = "com.typesafe.akka"         %% "akka-http-testkit"          % akkaHttpVersion % Test
    val scalaLogging             = "com.typesafe.scala-logging" %% "scala-logging"             % scalaLoggingVersion
    val elastic4sCore            = "com.sksamuel.elastic4s"     %% "elastic4s-core"            % elasticVersion
    val elastic4sHttp            = "com.sksamuel.elastic4s"     %% "elastic4s-http"            % elasticVersion
    val elastic4sTestKit         = "com.sksamuel.elastic4s"     %% "elastic4s-testkit"         % elasticVersion % Test
    val elastic4sEmbedded        = "com.sksamuel.elastic4s"     %% "elastic4s-embedded"        % elasticVersion % Test
    val log4jForEsTests          = "org.apache.logging.log4j"   % "log4j-api"                  % log4jVersion % Test
    val log4jCoreForEsTests      = "org.apache.logging.log4j"   % "log4j-core"                 % log4jVersion % Test
  }
}
