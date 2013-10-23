package controllers

import play.api.Play.current
import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{ Iteratee, Enumerator, Concurrent }
import libs.EventSource
import concurrent.Future

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka

import chessmap.lichess.Consumer
import chessmap.{ StubActor, On, Off }

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def stubdata = Action {
    val (enumerator, channel) = Concurrent.broadcast[String]
    val stubActor = Akka.system.actorOf(Props(new StubActor(channel)))
    stubActor ! On
    Ok.chunked(enumerator)
  }

  def stream = Action {
    val eventsStream = Consumer.enumerator &> EventSource()
    Ok.chunked(eventsStream).as("text/event-stream")
  }

}
