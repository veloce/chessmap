package controllers

import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import libs.EventSource
import concurrent.Future

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka

import lichess.Consumer

class LichessActor extends Actor {

  def receive = {
    case "start" ⇒ {
      val future = Consumer("http://en.lichess.org/stream")
      // TODO
      // restart if connection if closed
      // try to reconnect
      // future.onComplete()
    }
    case _      ⇒
  }
}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def stream = Action {
    val eventsStream = Consumer.enumerator &> EventSource()
    Ok.chunked(eventsStream).as("text/event-stream")
  }

}
