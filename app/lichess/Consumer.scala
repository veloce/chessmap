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
  context.setReceiveTimeout(3 seconds)

  def receive = {

    case Start ⇒ {
      val future = WS.url(url).get(consumer _)
      // TODO handle the case when connection is closed for some reason
    }

    case Handle(line) ⇒ {
      println(line)
      Consumer.channel.push(line)
      // todo geoip logic and all that stuff
    }

    // lichess stream inactive ? try to restart it
    case ReceiveTimeout => {
      throw new RuntimeException("Received time out")
    }

  }

  private def consumer(headers: ResponseHeaders): Iteratee[Array[Byte], Unit] =
    Iteratee foreach { bytes ⇒
      self ! Handle(new String(bytes, "UTF-8"))
    }

}

class Supervisor extends Actor {

  val consumer = context.actorOf(Props(new Consumer("http://localhost:9000/stubdata")))

  override def preStart = {
    self ! Start
  }

  def receive = {
    case Start => consumer ! Start
  }

}

case object Start
case class Handle(line: String)
