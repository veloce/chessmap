package chessmap

import play.api.libs.ws._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import akka.actor.{ Actor, Props }

import models.LichessStream

class Connection(url: String) extends Actor {

  override def preStart = {
    WS.url(url).withRequestTimeout(-1).get { headers ⇒
      Iteratee.foreach { bytes ⇒
        LichessStream.channel.push(new String(bytes, "UTF-8"))
      }
    }.onComplete { _ ⇒
      self ! ConnectionClosed
    }
  }

  def receive = {

    case ConnectionClosed ⇒ {
      Thread.sleep(3000)
      throw new Exception("Connection closed")
    }

  }

}

case object ConnectionClosed
