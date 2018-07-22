lazy val check = taskKey[Unit]("check")

lazy val commonSettings = Seq(
  version := "0.1",
  organization := "com.example",
  homepage := Some(url("http://example.com")),
  scalaVersion := "2.12.6"
)

lazy val root = (project in file(".")).
  aggregate(app).
  settings(commonSettings: _*)

lazy val app = (project in file("app")).
  enablePlugins(BuildInfoPlugin).
  settings(commonSettings: _*).
  settings(
    name := "sbt-buildinfo-example-app",
    buildInfoKeys := Seq(name,
                         projectID in LocalProject("root"),
                         version,
                         BuildInfoKey.map(homepage) { case (n, opt) => n -> opt.get },
                         scalaVersion),
    buildInfoPackage := "hello",
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

