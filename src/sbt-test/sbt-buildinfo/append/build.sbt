lazy val check = taskKey[Unit]("check")

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    name := "helloworld",
    organization := "com.eed3si9n",
    version := "0.1",
    scalaVersion := "2.10.2",
    buildInfoKeys ++= Seq[BuildInfoKey](name, organization, version, scalaVersion,
      libraryDependencies, libraryDependencies in Test),
    buildInfoKeys += BuildInfoKey(resolvers),
    buildInfoPackage := "hello",
    homepage := Some(url("http://example.com")),
    licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")),
    resolvers ++= Seq("Sonatype Public" at "https://oss.sonatype.org/content/groups/public"),
    check := {
      val f = (sourceManaged in Compile).value / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      val expect = scala.io.Source.fromFile(file("expect.txt")).getLines.toList
      if (lines != expect) {
        sys.error("unexpected output: \n" + lines.mkString("\n"))
      }
      ()
    }
  )

