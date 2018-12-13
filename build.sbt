val Http4sVersion = "0.20.0-M4"
val ScalaTestVersion = "3.0.5"
val LogbackVersion = "1.2.3"
val CirceVersion = "0.10.0"
val DoobieVersion = "0.6.0"

scalacOptions ++= Seq("-Ypartial-unification")

lazy val root = (project in file("."))
  .settings(
    organization := "io.github.carstendev",
    name := "pure-functional-webapp",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.5.0",
      "org.typelevel" %% "cats-free" % "1.5.0",

      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,

      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-prometheus-metrics" % Http4sVersion,


      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2" % DoobieVersion, // H2 driver 1.4.197 + type mappings.


      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",


      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),

    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oF")
  )

