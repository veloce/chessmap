import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current

import akka.actor.Props

import chessmap.lichess.Consumer

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    // start connection with lichess
    val lichessActor = Akka.system.actorOf(Props[Consumer])
    lichessActor ! "start"
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
