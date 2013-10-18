package lichess

import play.api.libs.iteratee._
import play.api.libs.ws._
import scala.concurrent.{ Future, Promise }

import play.api.libs.concurrent.Execution.Implicits._

object Consumer {

  def apply {
    WS.url("http://en.lichess.org/stream").get(consumer _)
  }

  private def consumer(headers: ResponseHeaders): Iteratee[Array[Byte], Unit] =
    Iteratee foreach { bytes ⇒
      sideEffect(new String(bytes, "UTF-8"))
    }

  private def sideEffect(line: String) {
    // send a message to an actor here
    println(line)
  }
}
