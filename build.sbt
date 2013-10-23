name := "chessmap"

version := "1.0-SNAPSHOT"

resolvers ++= Seq(
  "SnowPlow Repo" at "http://maven.snplow.com/releases/",
  "Twitter Maven Repo" at "http://maven.twttr.com/"
)

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.snowplowanalytics"  %% "scala-maxmind-geoip"  % "0.0.5"
)

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:_")

play.Project.playScalaSettings
