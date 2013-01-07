import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Paste"
    val appVersion      = "1.2"

    val appDependencies = Seq(
        "org.squeryl" %% "squeryl" % "0.9.5-6",
        "mysql" % "mysql-connector-java" % "5.1.20",
        jdbc
    )

    val pbkdf2Project = RootProject(uri("git://github.com/oxman/pbkdf2-scala.git"))

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
    ).dependsOn(pbkdf2Project)

}
