package chessmap.lichess

import scala.collection.mutable.Queue

import play.api.libs.iteratee._
import play.api.libs.ws._
import scala.concurrent.{ Future, Promise }

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

object Consumer {

  val (enumerator, channel) = Concurrent.broadcast[String]
  val consumerActor = Akka.system.actorOf(Props[Consumer])

  def apply(url: String): Future[Iteratee[Array[Byte], Unit]] = {
    WS.url(url).get(consumer _)
  }

  private def consumer(headers: ResponseHeaders): Iteratee[Array[Byte], Unit] =
    Iteratee foreach { bytes ⇒
      retrieve(new String(bytes, "UTF-8"))
    }

  private def retrieve(line: String) {
    consumerActor ! Handle(line)
  }
}

class Consumer extends Actor {

  def receive = {

    case "start" ⇒ {
      val future = Consumer("http://localhost:9000/stubdata")
      // TODO
      // restart if connection if closed
      // try to reconnect
      // future.onComplete()
    }

    case Handle(line) => {
      println(line)
      Consumer.channel.push(line)
      // todo geoip logic and all that stuff
    }

    case _      ⇒
  }
}

case class Handle(line: String)
