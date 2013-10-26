package models

import play.api.libs.iteratee._

object LichessStream {

  val (enumerator, channel) = Concurrent.broadcast[String]

}

