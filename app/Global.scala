import play.api._
import play.api.libs.concurrent.Akka
import play.api.Play.current

import akka.actor.Props

import chessmap.lichess.{ Consumer, Start }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    // start connection with lichess
    // val supervisor = Akka.system.actorOf(Props[Supervisor])
    val consumer = Akka.system.actorOf(Props(new Consumer("http://localhost:9000/stub")))
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
