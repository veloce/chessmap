import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current

import akka.actor.Props
import chessmap.Connection

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    // start connection with lichess
    val url = current.configuration.getString("lichess.stream_url")
      .getOrElse("http://en.lichess.org/stream")
    val consumer = Akka.system.actorOf(Props(new Connection(url)))
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
