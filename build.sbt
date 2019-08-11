lazy val SparkVersion = "2.4.0"
lazy val FramelessVersion = "0.8.0"

def makeColorConsole() = {
  val ansi = System.getProperty("sbt.log.noformat", "false") != "true"
  if (ansi) System.setProperty("scala.color", "true")
}

resolvers += Resolver.sonatypeRepo("releases")

lazy val root = project.in(file(".")).
  settings(
    name := "frameless-trial01",
    organization := "com.nothing",
    scalaVersion := "2.12.8",
    version := "0.1",
    scalacOptions ++= Seq(
      "-encoding", "utf8", // Option and arguments on same line
      "-Xfatal-warnings",  // New lines for each options
      "-deprecation",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:postfixOps"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "frameless-dataset" % FramelessVersion,
      "org.typelevel" %% "frameless-cats" % FramelessVersion,
      "org.apache.spark" %% "spark-core" % SparkVersion,
      "org.apache.spark" %% "spark-sql"  % SparkVersion
    ),
    initialize ~= { _ => makeColorConsole() },
    initialCommands in console :=
      """
        |import org.apache.spark.{SparkConf, SparkContext}
        |import org.apache.spark.sql.SparkSession
        |import frameless.functions.aggregate._
        |import frameless.syntax._
        |
        |val conf = new SparkConf().setMaster("local[*]").setAppName("frameless-repl").set("spark.ui.enabled", "false")
        |implicit val spark = SparkSession.builder().config(conf).appName("frameless-trial01").getOrCreate()
        |
        |import spark.implicits._
        |
        |spark.sparkContext.setLogLevel("WARN")
        |
        |import frameless.TypedDataset
      """.stripMargin,
    cleanupCommands in console :=
      """
        |spark.stop()
      """.stripMargin
  )
