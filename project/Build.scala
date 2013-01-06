import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Paste"
    val appVersion      = "1.1"

    val appDependencies = Seq(
        "com.typesafe" % "slick_2.10.0-RC1" % "0.11.2",
        "mysql" % "mysql-connector-java" % "5.1.20",
        jdbc
    )

    val pbkdf2Project = RootProject(uri("git://github.com/oxman/pbkdf2-scala.git"))

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
    ).dependsOn(pbkdf2Project)

}
