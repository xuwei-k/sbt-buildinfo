package sbtbuildinfo

private[sbtbuildinfo] case class ScalaCaseObjectRenderer(options: Seq[BuildInfoOption], pkg: String, obj: String) extends ScalaRenderer {

  override def fileType = BuildInfoType.Source
  override def extension = "scala"
  val traitNames = options.collect{case BuildInfoOption.Traits(ts @ _*) => ts}.flatten
  val objTraits = if (traitNames.isEmpty) "" else " extends " ++ traitNames.mkString(" with ")

  // It is safe to add `import scala.Predef` even though we need to keep `-Ywarn-unused-import` in mind
  // because we always generate code that has a reference to `String`. If the "base" generated code were to be
  // changed and no longer contain a reference to `String`, we would need to remove `import scala.Predef` and
  // fully qualify every reference. Note it is NOT safe to use `import scala._` because of the possibility of
  // the project using `-Ywarn-unused-import` because we do not always generated references that are part of
  // `scala` such as `scala.Option`.
  def header = List(
    s"package $pkg",
    "",
    "import scala.Predef._",
    "",
    s"/** This object was generated by sbt-buildinfo. */",
    s"case object $obj$objTraits {"
  )

  def footer = List("}")

  override def renderKeys(buildInfoResults: Seq[BuildInfoResult]) =
    header ++
    buildInfoResults.flatMap(line) ++ Seq(toStringLines(buildInfoResults)) ++
    toMapLine(buildInfoResults) ++ toJsonLine ++
    footer

  private def line(result: BuildInfoResult): Seq[String] = {
    import result._
    val typeDecl = getType(result.typeExpr) map { ": " + _ } getOrElse ""

    List(
      s"  /** The value is ${quote(value)}. */",
      s"  val $identifier$typeDecl = ${quote(value)}"
    )
  }

  def toStringLines(results: Seq[BuildInfoResult]): String = {
    val idents = results.map(_.identifier)
    val fmt = idents.map("%s: %%s" format _).mkString(", ")
    val vars = idents.mkString(", ")
    s"""  override val toString: String = {
         |    "$fmt".format(
         |      $vars
         |    )
         |  }""".stripMargin
  }

  def toMapLine(results: Seq[BuildInfoResult]): Seq[String] =
    if (options.contains(BuildInfoOption.ToMap) || options.contains(BuildInfoOption.ToJson))
      results
        .map(result => "    \"%s\" -> %s".format(result.identifier, result.identifier))
        .mkString("  val toMap: Map[String, Any] = Map[String, Any](\n", ",\n", ")")
        .split("\n")
        .toList ::: List("")
    else Nil

  def toJsonLine: Seq[String] =
    if (options contains BuildInfoOption.ToJson)
      List(
         """  val toJson: String = toMap.map{ i =>
           |    def escape(c: Char): String = c match {
           |      case '\\' => "\\\\"
           |      case '"' => "\\\""
           |      case '\b' => "\\b"
           |      case '\f' => "\\f"
           |      case '\n' => "\\n"
           |      case '\r' => "\\r"
           |      case '\t' => "\\t"
           |      case c =>
           |        if (Character.isISOControl(c)) {
           |          "\\u%04x".format(c.toInt)
           |        } else {
           |          String.valueOf(c)
           |        }
           |    }
           |    def quote(x:Any) : String = "\"" + x.toString.map(escape) + "\""
           |    val key : String = quote(i._1)
           |    val value : String = i._2 match {
           |       case elem : Seq[_] => elem.map(quote).mkString("[", ",", "]")
           |       case elem : Option[_] => elem.map(quote).getOrElse("null")
           |       case elem => quote(elem)
           |    }
           |    s"$key : $value"
           |    }.mkString("{", ", ", "}")""".stripMargin)
    else Nil

}
