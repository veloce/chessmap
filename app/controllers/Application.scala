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

import models.LichessStream
import models.LichessStream._
import chessmap.Lidata.{ Stub, StubActor, On, Off, Push }

object Application extends Controller {

  val (stubEnum, stubChannel) = Concurrent.broadcast[String]
  val stubActor = Akka.system.actorOf(Props(new StubActor(stubChannel)))

  def index = Action {
    Ok(views.html.index())
  }

  def infstub = Action {
    stubActor ! Push
    Ok.chunked(stubEnum)
  }

  def stub = Action {
    Ok.chunked(Stub.enumerator)
  }

  def stubon = Action {
    stubActor ! On
    NoContent
  }

  def stuboff = Action {
    stubActor ! Off
    NoContent
  }

  def stream = Action {
    val source = LichessStream.enumerator &> lineParser &>
    toIpLocation &> toLocation &> withOpponentLocation &> 
    asJson &> EventSource()

    Ok.chunked(source).as("text/event-stream")
  }
}
