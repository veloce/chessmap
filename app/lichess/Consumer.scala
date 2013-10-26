package chessmap.lichess

import play.api.libs.iteratee._
import play.api.libs.ws._
import scala.concurrent.{ Future, Promise }

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

object Consumer {

  val (enumerator, channel) = Concurrent.broadcast[String]

}

class Consumer(url: String) extends Actor {

  override def preStart = {
    WS.url(url).get { headers ⇒
      Iteratee.foreach { bytes ⇒
        self ! Handle(new String(bytes, "UTF-8"))
      }
    }.onComplete { _ ⇒
      self ! ConnectionClosed
    }
  }

  def receive = {

    case Handle(line) ⇒ {
      println("handled", line)
      Consumer.channel.push(line)
      // todo geoip logic and all that stuff
    }

    case ConnectionClosed ⇒ {
      Thread.sleep(3000)
      throw new Exception("Connection closed")
    }

  }

}

case object ConnectionClosed
case class Handle(line: String)
