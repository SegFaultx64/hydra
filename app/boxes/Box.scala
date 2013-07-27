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

	var seg1 = Random.nextInt(200)
	var seg2 = Random.nextInt(200)
	var seg3 = Random.nextInt(200)
	var lastPort = 65000

	def nextIp = {
		if (seg3 > 254) {
			seg3 = 0
			seg2 = seg2 + 1
			if (seg2 > 255) {
				seg2 = 0
				seg1 = seg1 + 1
			}
		}
		s"10.$seg1.$seg2.$seg3"
	}

	def nextPort = {
		lastPort = lastPort + 1
		"" + lastPort
	}

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