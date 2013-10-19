package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Enumerator
import libs.EventSource
import concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._

import lichess.Consumer

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def stream = Action {
    Consumer("http://en.lichess.org/stream")
    val lichessStream = Enumerator.repeatM[String](
      Future.successful(Consumer.moves.dequeue())
    )
    val eventsStream = lichessStream &> EventSource()
    Ok.chunked(eventsStream).as("text/event-stream")
  }

}
