package models

import com.google.common.cache.LoadingCache
import com.snowplowanalytics.maxmind.geoip.{ IpGeo, IpLocation }
import java.io.File
import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.Play.current

import chessmap.Cache

object LichessStream extends Cache {

  val (enumerator, channel) = Concurrent.broadcast[String]

  val cacheSize: Long = current.configuration.getLong("maxmind.ip_cache_size")
    .getOrElse(10000)
  val dbFile = new File("conf/GeoLiteCity.dat")
  val ipgeo = new IpGeo(dbFile = dbFile, memCache = false, lruCache = 0)
  val ipCache: LoadingCache[String, Option[IpLocation]] = cache(cacheSize, ipgeo.getLocation)

  val aiIps: List[String] = current.configuration.underlying.getStringList("lichess.ai_ips").toList
  val aiLocations = (aiIps map ipCache.get).flatten map Location.apply
  val players = new LichessPlayers(aiLocations.toSet)

  type OpponentLocation = Option[Location]
  type LocationPair = (Location, OpponentLocation)

  val lineParser: Enumeratee[String, Option[Move]] = Enumeratee.map[String] { line ⇒
    line.split("\\s") match {
      case Array(id, move, ip) ⇒ Some(Move(id, move, ip))
      case _                   ⇒ None
    }
  }

  val toIpLocation: Enumeratee[Option[Move], Option[MoveWith[IpLocation]]] = Enumeratee.map[Option[Move]] { op ⇒
    op.flatMap(move ⇒ ipCache.get(move.ip) map { ipLocation ⇒
      MoveWith(move, ipLocation)
    })
  }

  val toLocation: Enumeratee[Option[MoveWith[IpLocation]], MoveWith[Location]] =
    Enumeratee.mapInput {
      case Input.El(Some(MoveWith(move, iploc))) ⇒ {
        Input.El(MoveWith(move, Location(iploc)))
      }
      case _ ⇒ Input.Empty
    }

  val withOpponentLocation: Enumeratee[MoveWith[Location], LocationPair] =
    Enumeratee.map {
      case MoveWith(move, myLocation) => 
        myLocation -> players.getOpponentLocation(move.gameId, myLocation)
    }

  val asJson: Enumeratee[LocationPair, JsValue] = Enumeratee.map {
    case (myLocation, opponentLocation) ⇒ Json.obj(
      "countryName" -> myLocation.countryName,
      "region" -> myLocation.region,
      "city" -> Json.toJson(myLocation.city),
      "latitude" -> myLocation.latitude,
      "longitude" -> myLocation.longitude,
      "oLatitude" -> opponentLocation.map(_.latitude),
      "oLongitude" -> opponentLocation.map(_.longitude)
    )
  }

}

case class Location(
  countryName: String,
  region: Option[String],
  city: Option[String],
  latitude: Float,
  longitude: Float)

object Location {

  def apply(ipLoc: IpLocation): Location =
    Location(ipLoc.countryName, ipLoc.region, ipLoc.city, ipLoc.latitude, ipLoc.longitude)
}


case class Move(
  gameId: String,
  move: String,
  ip: String)

case class MoveWith[A](move: Move, value: A)
