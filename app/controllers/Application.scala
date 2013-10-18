package controllers

import play.api._
import play.api.mvc._

import lichess.Consumer

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def stream = Action {
    Consumer.apply
    Ok("bluk")
  }

}
