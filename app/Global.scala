import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current

import akka.actor.Props

import controllers.LichessActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    // start connection with lichess
    val lichessActor = Akka.system.actorOf(Props[LichessActor], name = "myActor")
    lichessActor ! "start"
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
