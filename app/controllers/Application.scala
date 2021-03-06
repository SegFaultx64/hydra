package controllers

import play.api._
import play.api.mvc._
import boxes._
import play.api.libs.iteratee._

object Application extends Controller {
  
  def index = Action { implicit request =>
    if(Box.boxes.isEmpty) {
      Box.loadBoxes(new java.io.File(System.getProperty("user.home") + "/hydra/"))
    }

    Box.getRunning;
    Ok(views.html.index(Box.boxes.sorted(BoxOrdering)))
  }  

  def setPassword = Action(parse.urlFormEncoded) { implicit request =>
    // general.Config.password = pass
    general.Config.password = request.body("password")(0)
    if (general.Config.testPassword) {
      Redirect("/").flashing(
        "success" -> "Set password!"
      )
    } else {
      general.Config.password = ""
      Redirect("/")
    }
  }

  def addProject = Action(parse.urlFormEncoded) { implicit request =>
    val HomePath = "~(.*)".r
    val path = request.body("path")(0) match {
      case HomePath(p) => System.getProperty("user.home") + p
      case a           => a
    }
    
    val lastPart = path.split("/").last
    import sys.process._
    sys.process.Process(Seq( "ln", "-s", path, lastPart), new java.io.File(System.getProperty("user.home") + "/hydra/")).run()
    Redirect("/").flashing(
      "success" -> "Added project!"
    )
  }

  def start(box: String) = Action { implicit request =>
  	val enumerator = Enumerator.outputStream { os =>
		Vagrant.boxes.find(a => a.name == box) match {
			case Some(a) => a.start(os) 
			case None 	 => {
        boxes.Play.boxes.find(b => b.name == box) match {
          case Some(b) => b.start(os)
          case None    => os.close 
        } 
      }
		}
    }
  	Ok.stream(enumerator >>> Enumerator.eof).withHeaders(
      "Content-Type"->"text/html"
    )
  }

  def stop(box: String) = Action { implicit request =>
    val enumerator = Enumerator.outputStream { os =>
    Vagrant.boxes.find(a => a.name == box) match {
      case Some(a) => a.stop(os) 
      case None    => {
        boxes.Play.boxes.find(b => b.name == box) match {
          case Some(b) => b.stop(os)
          case None    => os.close 
        } 
      }
    }
    }
    Ok.stream(enumerator >>> Enumerator.eof).withHeaders(
      "Content-Type"->"text/html"
    )
  }

  def restart(box: String) = Action { implicit request =>
  	val enumerator = Enumerator.outputStream { os =>
		Vagrant.boxes.find(a => a.name == box) match {
			case Some(a) => a.restart(os) 
			case None 	 => {
        boxes.Play.boxes.find(b => b.name == box) match {
          case Some(b) => b.restart(os)
          case None    => os.close 
        } 
      }
		}
    }
  	Ok.stream(enumerator >>> Enumerator.eof).withHeaders(
      "Content-Type"->"text/html"
    )
  }

  def pause(box: String) = Action { implicit request =>
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