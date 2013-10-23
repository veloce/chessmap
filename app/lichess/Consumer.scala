package chessmap.lichess

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

}

class Consumer extends Actor {

  def receive = {

    case Start(url) ⇒ {
      val future = WS.url(url).get(consumer _)
      // TODO
      // restart if connection if closed
      // try to reconnect
      // future.onComplete()
    }

    case Handle(line) ⇒ {
      println(line)
      Consumer.channel.push(line)
      // todo geoip logic and all that stuff
    }

    case _ ⇒
  }

  private def consumer(headers: ResponseHeaders): Iteratee[Array[Byte], Unit] =
    Iteratee foreach { bytes ⇒
      self ! Handle(new String(bytes, "UTF-8"))
    }

}

class Supervisor extends Actor {

  def receive = {
    case Start ⇒ {
      val consumer = Akka.system.actorOf(Props[Consumer])
      consumer ! Start("http://localhost:9000/stubdata")
    }
  }
}

case object Start
case class Start(url: String)
case class Handle(line: String)
