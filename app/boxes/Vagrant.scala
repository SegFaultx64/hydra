package boxes
import java.io._

class Vagrant(val name: String, val path: File, val id: String, val readme: String, var running: Boolean = false) {
	override def toString(): String =  "Vagrant: " + name + " / Path: " + path 

	def start(out: OutputStream) = {		
		import sys.process._
		val pio = new ProcessIO(_ => (),
                        stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(a => {out.write(("\n" + a).getBytes); out.flush}),
                        _ => ())
		
		out.write((LogStuff.top1+"Starting"+LogStuff.top2+"Starting"+LogStuff.top3).getBytes)
		val ret = (sys.process.Process(Seq( "vagrant", "up" ), path) run pio).exitValue
		out.write(LogStuff.bottom.getBytes)
		out.close
	}

	def stop(out: OutputStream) = {		
				import sys.process._
		val pio = new ProcessIO(_ => (),
                        stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(a => {out.write(("\n" + a).getBytes); out.flush}),
                        _ => ())
		
		out.write((LogStuff.top1+"Stopping"+LogStuff.top2+"Stopping"+LogStuff.top3).getBytes)
		val ret = (sys.process.Process(Seq( "vagrant", "halt" ), path) run pio).exitValue
		out.write(LogStuff.bottom.getBytes)
		out.close
	}

	def restart(out: OutputStream) = {		
				import sys.process._
		val pio = new ProcessIO(_ => (),
                        stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(a => {out.write(("\n" + a).getBytes); out.flush}),
                        _ => ())
		
		out.write((LogStuff.top1+"Restarting"+LogStuff.top2+"Restarting"+LogStuff.top3).getBytes)
		val ret = (sys.process.Process(Seq( "vagrant", "reload" ), path) run pio).exitValue
		out.write(LogStuff.bottom.getBytes)
		out.close
	}

	def pause(out: OutputStream) = {		
				import sys.process._
		val pio = new ProcessIO(_ => (),
                        stdout => scala.io.Source.fromInputStream(stdout).getLines.foreach(a => {out.write(("\n" + a).getBytes); out.flush}),
                        _ => ())
		out.write((LogStuff.top1+"Pausing"+LogStuff.top2+"Pausing"+LogStuff.top3).getBytes)
		val ret = (sys.process.Process(Seq( "vagrant", "suspend" ), path) run pio).exitValue
		out.write(LogStuff.bottom.getBytes)
		out.close
	}
	
}

object Vagrant {
	var boxes: List[Vagrant] = List empty;

	private def recursiveListFiles(f: File): Array[File] = {
	  val these = f.listFiles
	  these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
	}

	def loadBoxes(location: File) = {
		boxes = List empty;
		val files = location.listFiles
		files.filter(a => a.isDirectory).map(a => if(new File(a.toString + "/.vagrant").listFiles != null) {(recursiveListFiles(new File(a.toString + "/.vagrant")).find(b => b.getName == "id"), a)} else {(None, a)})
		.foreach(a => {
			a._1 match {
				case Some(b) => {
					val id = scala.io.Source.fromFile(b).mkString
					val readmeFile = new File(a._2.toString + "/README.md")
					val readme = if(readmeFile.exists) {
						import eu.henkelmann.actuarius.ActuariusTransformer
						val transformer = new ActuariusTransformer()
						transformer(scala.io.Source.fromFile(readmeFile).mkString)
					} else {
						""
					}
					Vagrant.boxes = new Vagrant(a._2.getName, a._2, id, readme) :: boxes
				}
				case None => {} 
			}
		})
	}

	def getRunning = {
		import sys.process._
		val list = "vagrant list" !!;
		boxes.foreach(a => {
			val reg = a.id.r
			if (reg.findAllIn(list).length > 1) {
				a.running = true;
			} else {
				a.running = false;
			}
		})
	}
}


object VagrantOrdering extends Ordering[Vagrant] { def compare(o1: Vagrant, o2: Vagrant) = if (o2.running && !o1.running) {1} else if (!o2.running && o1.running) {-1} else {0}}


object LogStuff {
	val top1 ="""<html><head><head><title>"""
	val top2 = """</title>
				        <script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script>
			       		<script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script>
				        <script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script><script></script>
				        <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.no-icons.min.css" rel="stylesheet">
				        <link href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css" rel="stylesheet">
				        <link rel="stylesheet" media="screen" href="/assets/stylesheets/main.css">
				        <link rel="shortcut icon" type="image/png" href="/assets/images/favicon.png">
				        <script src="/assets/javascripts/jquery-1.9.0.min.js" type="text/javascript"></script><style type="text/css"></style><style type="text/css"></style>
				        <script src="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js"></script>
			    	</head>
			 		<body>
			 		<div class="container">
					<div class="page-header">
						<h1>"""
	val top3 ="""</h1></div><div class="row"><pre>"""
	val bottom ="""
	Done . . . Redirecting</pre></body><meta http-equiv="refresh" content="1; url=/"></html>"""
}