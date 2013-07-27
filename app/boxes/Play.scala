package boxes
import java.io._

class Play(val name: String, val path: File, val readme: String, val port: String, var running: Boolean = false) {
	val url = name + ".hydra"

	val config = s"""
<VirtualHost *:80>
  ServerAdmin none@none.none
  DocumentRoot "/usr/docs"
  ServerName $url

  ProxyRequests Off
  ProxyVia Off

  ProxyPass / http://127.0.0.1:$port/
  ProxyPassReverse / http://127.0.0.1:$port/
</VirtualHost>
	"""

	def foward() = {
		general.Config.sudoWrite(s"""echo "$config" >> /private/etc/apache2/other/$url.conf""")
		general.Config.sudoWrite(s"""apachectl restart""")
	}
	def unfoward() = {
		general.Config.sudoWrite(s"""rm /private/etc/apache2/other/$url.conf""")
		general.Config.sudoWrite(s"""apachectl restart""")
	}

	def setHostEntry(on: Boolean) = {
		if (on) {
			general.Config.sudoWrite(s"""echo "\n127.0.0.1 $url" >> /etc/hosts""")
		} else {
			general.Config.sudoWrite(s"""sed -i -e "/127.0.0.1 $url/d" /etc/hosts""")
		}
	}

	def start(out: OutputStream) = {		
		import sys.process._
		val pio = new ProcessIO(_ => (),
                        stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(a => {out.write(("\n" + a).getBytes); out.flush}),
                        _ => ())
		
		out.write((LogStuff.top1+"Starting"+LogStuff.top2+"Starting"+LogStuff.top3).getBytes)
		(sys.process.Process(Seq( "play", s"""run $port &""" ), path) run pio)
		out.write(LogStuff.bottom.getBytes)
		setHostEntry(true)
		foward
		out.close
	}

	def stop(out: OutputStream) = {		
		out.write((LogStuff.top1+"Stopping"+LogStuff.top2+"Stopping"+LogStuff.top3).getBytes)
		val playFile = new File(this.path + "/RUNNING_PID")
		if (playFile.exists) {
			val pid = scala.io.Source.fromFile(playFile).mkString
			general.Config.sudoWrite(s"""kill -9 $pid""")
			playFile.delete
		}
		out.write(LogStuff.bottom.getBytes)
		out.close
		unfoward
		setHostEntry(false)
	}

	def restart(out: OutputStream) = {		
		stop(out)
		start(out)
	}
}

object Play {
	var boxes: List[Play] = List empty;

	private def recursiveListFiles(f: File): Array[File] = {
	  val these = f.listFiles
	  these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
	}

		val buildScala = """.settings(
    // Added by Hydra
    playOnStopped := List(() => {
      (new java.io.File("./RUNNING_PID")).delete
    }),
    playOnStarted := List((s: { def toString: String }) => {
      java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@').headOption.map { pid =>
        val fw = new java.io.FileWriter(("./RUNNING_PID"), false)
        fw.write(pid)
        fw.close()
      }
    })
  )"""

	def loadBoxes(location: File) = {
		boxes = List empty;
		val files = location.listFiles
		files.filter(a => a.isDirectory).map(a => if(new File(a.toString + "/project").listFiles != null) {(recursiveListFiles(new File(a.toString + "/project")).find(b => b.getName == "Build.scala"), a)} else {(None, a)})
		.foreach(a => {
			a._1 match {
				case Some(b) => {
					val buildFile = new File(a._2.toString + "/project/Build.scala")
					val buildConfig = scala.io.Source.fromFile(buildFile).mkString
					val regex = "// Added by Hydra".r
					if (regex.findAllIn(buildConfig).length < 1) {
						val pieces = buildConfig.splitAt(buildConfig.lastIndexOf(')') + 1)
						val newBuildConfig = pieces._1 + buildScala + pieces._2
						val backup = new FileWriter((a._2.toString + "/project/Build.scala.old"), true)
						backup.write(buildConfig)
						val fw = new FileWriter((a._2.toString + "/project/Build.scala"), false)
						fw.write(newBuildConfig)
						fw.close()
					}
					Play.boxes = new Play(a._2.getName, a._2, "", "9050") :: boxes
				}
				case None => {} 
			}
		})
	}


	def getRunning = {
		boxes.foreach(a => {
			val playFile = new File(a.path + "/RUNNING_PID")
			if (playFile.exists) {
					import sys.process._
					val pid = scala.io.Source.fromFile(playFile).mkString
					if(("ps -A" #| ("grep " + pid + "") !!).split('\n').length > 1) {
						a.running = true
					} else {
						playFile.delete
						a.running = false
					}
				} else {
					a.running = false
				}
		})
	}
}