import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Paste"
    val appVersion      = "1.1"

    val appDependencies = Seq(
        "com.typesafe" % "slick_2.10.0-RC1" % "0.11.2",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "mysql" % "mysql-connector-java" % "5.1.20",
        jdbc
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
    )

}
