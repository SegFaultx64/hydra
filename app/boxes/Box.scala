package boxes

import java.io._

abstract class Box(val name: String, val path: File, val readme: String, val ip: String) {

	val url = name + ".hydra"
	
	var running: Boolean = false

	def setHostEntry(on: Boolean) = {
		if (on) {
			general.Config.sudoWrite(s"""echo "\n$ip $url" >> /etc/hosts""")
		} else {
			general.Config.sudoWrite(s"""sed -i -e "/$ip $url/d" /etc/hosts""")
		}
	}

	def start(out: OutputStream): Boolean
	def stop(out: OutputStream): Boolean
	def restart(out: OutputStream): Boolean
}

object BoxOrdering extends Ordering[Box] { def compare(o1: Box, o2: Box) = if (o2.running && !o1.running) {1} else if (!o2.running && o1.running) {-1} else {0}}

object Box {
	var boxes: List[Box] = List empty

	def loadBoxes(location: File) = {
		Vagrant.loadBoxes(location);
		Play.loadBoxes(location);
		boxes = Vagrant.boxes ::: boxes
		boxes = Play.boxes ::: boxes 
	}

	def getRunning = {
		Vagrant.getRunning;
		Play.getRunning;
	}
}