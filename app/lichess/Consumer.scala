package lichess

import scala.collection.mutable.Queue

import play.api.libs.iteratee._
import play.api.libs.ws._
import scala.concurrent.{ Future, Promise }

import play.api.libs.concurrent.Execution.Implicits._

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

object Consumer {

  val (enumerator, channel) = Concurrent.broadcast[String]

  def apply(url: String) {
    WS.url(url).get(consumer _)
  }

  private def consumer(headers: ResponseHeaders): Iteratee[Array[Byte], Unit] =
    Iteratee foreach { bytes ⇒
      retrieve(new String(bytes, "UTF-8"))
    }

  private def retrieve(line: String) {
    channel.push(line)
  }
}
