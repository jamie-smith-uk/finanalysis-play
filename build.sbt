import com.typesafe.config.ConfigFactory

name := "finanalysis-play"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, CucumberPlugin)

scalaVersion := "2.11.7"

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick"           % "1.1.1",
  "com.typesafe"      % "config"                % "1.3.0",
  "org.apache.poi"    % "poi"                   % "3.14-beta1",
  "org.apache.poi"    % "poi-excelant"          % "3.14-beta1",
  "org.clapper"       % "grizzled-slf4j_2.10"   % "1.0.2",
  "com.typesafe.play" %% "anorm" 				        % "2.4.0",
  "org.webjars" 		  %% "webjars-play" 		    % "2.4.0-1",
  "org.webjars" 		  %  "bootstrap" 			      % "3.1.1-2",
  "org.webjars"       %  "flat-ui"              % "bcaf2de95e",
  "org.webjars" 		  %  "react" 				        % "0.13.3",
  "org.webjars" 		  %  "marked" 			        % "0.3.2",
  "info.cukes"        % "cucumber-core"         % "1.2.4" % "test",
  "info.cukes"        % "cucumber-scala_2.10"   % "1.2.4" % "test",
  "info.cukes"        % "cucumber-jvm"          % "1.2.4" % "test",
  "info.cukes"        % "cucumber-junit"        % "1.2.4" % "test",
  "org.scalatest"     %% "scalatest"            % "2.2.4" % "test",
  "org.scalactic"     %% "scalactic"            % "2.2.6",
  "org.reactivemongo" %% "play2-reactivemongo"  % "0.11.11-play24",
  "postgresql"        % "postgresql"            % "9.1-901.jdbc4",
  specs2 % Test
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

coverageEnabled in Test := true

CucumberPlugin.glue := "pms/test/"

fork in run := false