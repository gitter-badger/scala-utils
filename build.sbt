
name := "scala-utils"

organization := "org.tupol"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.6")

// ------------------------------
// DEPENDENCIES AND RESOLVERS

updateOptions := updateOptions.value.withCachedResolution(true)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" % "scala-logging" % "3.9.0" cross CrossVersion.binary,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalaz" % "scalaz-core" % "7.2.26" cross CrossVersion.binary,
  "org.scalacheck" % "scalacheck" % "1.14.0" % "test" cross CrossVersion.binary,
  "org.scalatest" % "scalatest" % "3.0.5" % "test" cross CrossVersion.binary,
  "com.typesafe" % "config" % "1.3.0"
)
// ------------------------------
// TESTING
parallelExecution in Test := false

fork in Test := true

publishArtifact in Test := true

// ------------------------------
// TEST COVERAGE

scoverage.ScoverageKeys.coverageExcludedFiles := ".*BuildInfo.*"


// ------------------------------
// PUBLISHING
isSnapshot := version.value.trim.endsWith("SNAPSHOT")

useGpg := true

// Nexus (see https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html)
publishTo := {
  val repo = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at repo + "content/repositories/snapshots")
  else
    Some("releases" at repo + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := true

publishMavenStyle := true

pomIncludeRepository := { x => false }


licenses := Seq("MIT-style" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/tupol/scala-utils"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/tupol/scala-utils.git"),
    "scm:git@github.com:tupol/scala-utils.git"
  )
)

developers := List(
  Developer(
    id    = "tupol",
    name  = "Oliver Tupran",
    email = "olivertupran@yahoo.com",
    url   = url("https://github.com/tupol")
  )
)

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,              // : ReleaseStep
  inquireVersions,                        // : ReleaseStep
  runClean,                               // : ReleaseStep
  runTest,                                // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  releaseStepCommand(s"""sonatypeOpen "${organization.value}" "${name.value} v${version.value}""""),
  releaseStepCommand("+publishSigned"),
  releaseStepCommand("+sonatypeRelease"),
  setNextVersion,                         // : ReleaseStep
  commitNextVersion,                      // : ReleaseStep
  pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
)

// ------------------------------
// BUILD-INFO
lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.tupol.utils.info"
  )

buildInfoKeys ++= Seq[BuildInfoKey](
  resolvers,
  libraryDependencies in Test,
  BuildInfoKey.map(name) { case (k, v) => "project" + k.capitalize -> v.capitalize },
  BuildInfoKey.action("buildTime") {
    System.currentTimeMillis
  } // re-computed each time at compile
)

buildInfoOptions += BuildInfoOption.BuildTime
buildInfoOptions += BuildInfoOption.ToMap
buildInfoOptions += BuildInfoOption.ToJson
