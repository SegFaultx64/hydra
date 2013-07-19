package controllers

import play.api._
import play.api.mvc._
import boxes._
import play.api.libs.iteratee._

object Application extends Controller {
  
  def index = Action {
  	if(Vagrant.boxes.isEmpty) {
  		Vagrant.loadBoxes(new java.io.File(System.getProperty("user.home") + "/workspace/"))
  	}

  	Vagrant.getRunning;
  	Ok(views.html.index(Vagrant.boxes))
  }

  def start(box: String) = Action {
  	val enumerator = Enumerator.outputStream { os =>
		Vagrant.boxes.find(a => a.name == box) match {
			case Some(a) => a.start(os) 
			case None 	 => os.close 
		}
    }
  	Ok.stream(enumerator >>> Enumerator.eof).withHeaders(
      "Content-Type"->"text/html"
    )
  }

  def stop(box: String) = Action {
  	val enumerator = Enumerator.outputStream { os =>
		Vagrant.boxes.find(a => a.name == box) match {
			case Some(a) => a.stop(os) 
			case None 	 => os.close 
		}
    }
  	Ok.stream(enumerator >>> Enumerator.eof).withHeaders(
      "Content-Type"->"text/html"
    )
  }

  def pause(box: String) = Action {
  	val enumerator = Enumerator.outputStream { os =>
		Vagrant.boxes.find(a => a.name == box) match {
			case Some(a) => a.pause(os) 
			case None 	 => os.close 
		}
    }
  	Ok.stream(enumerator >>> Enumerator.eof).withHeaders(
      "Content-Type"->"text/html"
    )
  }
  
}