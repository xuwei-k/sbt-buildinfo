lazy val check = taskKey[Unit]("check")

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    name := "helloworld",
    version := "0.1",
    scalaVersion := "2.12.6",
    buildInfoKeys := Seq(name, version),
    buildInfoPackage := "hello",
    homepage := Some(url("http://example.com")),
    licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")),
    check := {
      val dir = (sourceManaged in Compile).value
      val f = dir / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      val expect = scala.io.Source.fromFile(file("expect.txt")).getLines.toList
      if (lines != expect) {
        sys.error("unexpected output: \n" + lines.mkString("\n"))
      }
      ()
    }
  )
