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

  // calling run on the iteratee is a way to push an Input.EOF to it in order
  // to close it
  override def preStart = {
    WS.url(url).get { headers =>
      Iteratee.fold() {
        case (a, b) =>
          self ! Handle(new String(b, "UTF-8"))
        }
    }.map { _ =>
      println("lbusdfadslf")

    }
  }

  def receive = {

    case Handle(line) ⇒ {
      println("handled", line)
      Consumer.channel.push(line)
      // todo geoip logic and all that stuff
    }

    // lichess stream inactive ? try to restart it
    case ReceiveTimeout => {
      throw new Exception("Received time out")
    }

  }

}

case object Start
case class Handle(line: String)
