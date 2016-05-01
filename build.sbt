organization := "com.github.michaelahlers"

name := "play-json-zipper"

description := "Tools for complex and powerful manipulations of Play JSON API structures."

licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")

/** See [[https://github.com/mandubian/play-json-zipper Pascal's original project]]. */
homepage := Some(url("http://github.com/michaelahlers/mandubian-play-json-zipper"))

startYear := Some(2013)

developers :=
  Developer("mandubian", "Pascal Voitot", "pascal.voitot.dev@gmail.com", url("http://mandubian.com")) ::
    Developer("michaelahlers", "Michael Ahlers", "michael@ahlers.co", url("http://github.com/michaelahlers")) ::
    Nil

scmInfo :=
  Some(ScmInfo(
    browseUrl = url("http://github.com/michaelahlers/mandubian-play-json-zipper"),
    connection = "scm:git:https://github.com:michaelahlers/mandubian-play-json-zipper.git",
    devConnection = Some("scm:git:git@github.com:michaelahlers/mandubian-play-json-zipper.git")
  ))

scalaVersion := "2.11.8"

/* TODO: Eventually enable fatal warnings. */
scalacOptions ++=
  "-feature" ::
    // "-Xlint" ::
    // "-Xfatal-warnings" ::
    Nil

scalacOptions ++=
  (CrossVersion.partialVersion(scalaVersion.value) match {

    case Some((2, minor)) if 11 <= minor =>
      "-target:jvm-1.8" ::
        Nil

    case _ =>
      Nil

  })

scalacOptions in Test ++=
  Nil

crossScalaVersions :=
  "2.10.6" ::
    "2.11.8" ::
    Nil

/**
 * - Play JSON is marked as provided so as to allow users to specify their preference.
 */
libraryDependencies ++=
  "com.typesafe.play" %% "play-json" % "2.3.10" % Provided ::
    Nil

libraryDependencies ++=
  "org.specs2" %% "specs2" % "2.3.13" % Test ::
    "junit" % "junit" % "4.12" % Test ::
    Nil

publishMavenStyle := true

/** Test artifacts are desired (as additional examples). */
publishArtifact in Test := true

publishTo := Some("releases" at "https://oss.sonatype.org/content/repositories/snapshots")

import ReleaseTransformations._

releaseProcess :=
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(
      action = { state: State =>
        /* FIXME: Reassigns this setting globally for the current session. */
        publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")

        Command.process(
          "publishSigned", {
            /* TODO: Stop being dumb and learn how to do this properly. */
            // val extracted = Project.extract(state)
            // extracted.append((publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")) :: Nil, state)
            state
          }
        )
      },
      enableCrossBuild = true
    ),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
    pushChanges
  )

val root =
  (project in file(".")).
    enablePlugins(BuildInfoPlugin).
    settings(
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "play.api.libs.json.zippers"
    )
