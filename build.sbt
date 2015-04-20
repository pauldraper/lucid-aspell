import com.github.joprice.Jni
import java.io.File

lazy val Native = config("native").extend(Compile)

lazy val aspell = (project in file("."))
  .configs(Native)
  .enablePlugins(BuildInfoPlugin)

Jni.settings

inConfig(Native)(Defaults.configSettings ++ Defaults.packageConfig)

addArtifact(artifact in (Native, packageBin), packageBin in Native)

artifactClassifier in (Native, packageBin) := Some("x86_64")

buildInfoKeys := Seq[BuildInfoKey](Jni.Keys.libraryName)

buildInfoPackage := "com.lucidchart.aspell"

crossScalaVersions := Seq("2.10.5", "2.11.5")

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  Option(System.getenv("SONATYPE_USERNAME")).getOrElse(""),
  Option(System.getenv("SONATYPE_PASSWORD")).getOrElse("")
)

dependencyClasspath in Test ++= (exportedProducts in Native).value

exportJars := true

Jni.Keys.includes ++= Seq("-Wl,--no-as-needed -Wl,-rpath,/usr/lib -Wl,-rpath,/usr/local/lib -L/usr/lib -L/usr/local/lib -laspell")

Jni.Keys.libraryName := s"lucidaspell-${version.value}"

Jni.Keys.jniClasses := Seq(
  "com.lucidchart.aspell.Aspell"
)

// can remove once https://github.com/joprice/sbt-jni/pull/1 is accepted
Jni.Keys.javah := Def.task {
  val log = streams.value.log
  val javahCommand =
    Seq(
      "javah",
      "-d", Jni.Keys.headersPath.value.getPath,
      "-classpath", (fullClasspath in Compile).value.map(_.data).mkString(File.pathSeparator)
    ) ++
      Jni.Keys.jniClasses.value
  log.info(javahCommand.mkString(" "))
  javahCommand ! log
}.tag(Tags.Compile, Tags.CPU)
  .value

libraryDependencies ++= Seq(
  "com.jsuereth" %% "scala-arm" % "1.4",
  "commons-io" % "commons-io" % "2.4",
  "org.specs2" %% "specs2-core" % "2.4.17" % Test
)

managedResourceDirectories in Native += Jni.Keys.binPath.value

name := "lucid-aspell"

organization := "com.lucidchart"

pomExtra := {
  <developers>
    <developer>
      <name>Lucid Software</name>
      <email>github@lucidchart.com</email>
      <organization>Lucid Software, Inc.</organization>
      <organizationUrl>https://www.golucid.co/</organizationUrl>
    </developer>
  </developers>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/lucidsoftware/lucid-aspell</connection>
      <developerConnection>scm:git:git@github.com:lucidsoftware/lucid-aspell</developerConnection>
      <url>https://github.com/lucidsoftware/lucid-aspell</url>
    </scm>
    <url>https://github.com/lucidsoftware/lucid-aspell</url>
}

resourceGenerators in Native +=
  Def.task {
    Jni.Keys.binPath.value.***.filter(_.isFile).get
  }
    .dependsOn(Jni.Keys.jniCompile)
    .taskValue

scalaVersion := "2.10.5"

version := "2.0-SNAPSHOT"

