import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "devbox"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here   
    playOnStopped := List(() => {
      (new java.io.File("./PlayState")).delete
    }),
    playOnStarted := List((s: { def toString: String }) => {
      java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@').headOption.map { pid =>
        val fw2 = new java.io.FileWriter(("./PlayState"), false)
        fw2.write(pid)
        fw2.close()
      }
    })
  )



}
