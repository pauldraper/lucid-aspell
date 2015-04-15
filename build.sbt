name := "lucid-aspell"

organization := "com.lucidchart"

version := "1.0"

scalaVersion := "2.10.6"

crossScalaVersions := Seq("2.10.6", "2.11.5")

libraryDependencies ++= Seq(
)

resolvers ++= List(
	DefaultMavenRepository,
	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)
