scalacOptions in doc ++=
  "-author" ::
    "-groups" ::
    "-implicits" ::
    "-no-link-warnings" ::
    Nil

/** See http://scala-sbt.org/0.13/docs/Howto-Scaladoc.html for details. */
autoAPIMappings := true

/** See http://stackoverflow.com/a/35673212/700420 for details. */
apiMappings ++= {
  def mappingsFor(organization: String, names: List[String], location: String, revision: (String) => String = identity): Seq[(File, URL)] =
    for {
      entry: Attributed[File] <- (fullClasspath in Compile).value
      module: ModuleID <- entry.get(moduleID.key)
      if module.organization == organization
      if names.exists(module.name.startsWith)
    } yield entry.data -> url(location.replace("${revision}", revision(module.revision)))

  /** Several of these may not be used in this project as this is a reusable set of mappings. */
  val mappings: Seq[(File, URL)] =
    mappingsFor("com.typesafe.play", List("play-json"), "http://playframework.com/documentation/${revision}/api/scala/index.html", _.replaceAll("[\\d]$", "x"))

  mappings.toMap
}

apiURL := {
  val host = "https://oss.sonatype.org"
  val path = s"${organization.value.replace('.', '/')}/${name.value}_${scalaBinaryVersion.value}/${version.value}/${name.value}_${scalaBinaryVersion.value}-${version.value}-javadoc.jar/!/index.html"

  val location =
    if (isSnapshot.value) url(s"$host/service/local/repositories/snapshots/archive/$path")
    else url(s"$host/service/local/repositories/releases/archive/$path")

  Some(location)
}
