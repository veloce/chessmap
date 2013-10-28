package models

import com.google.common.cache.LoadingCache
import com.snowplowanalytics.maxmind.geoip.{ IpGeo, IpLocation }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.Play.current

import chessmap.Cache

object LichessStream extends Cache {

  val (enumerator, channel) = Concurrent.broadcast[String]

  val dbFile = current.configuration.getString("maxmind.db_file")
    .getOrElse("/opt/maxmind/GeoLiteCity.dat")
  val ipgeo = IpGeo(dbFile = dbFile, memCache = false, lruCache = 0)
  val ipCache: LoadingCache[String, Option[IpLocation]] = cache(1000, ipgeo.getLocation)

  val lineParser: Enumeratee[String, Option[Move]] = Enumeratee.map[String] { line ⇒
    line.split("\\s") match {
      case Array(id, move, ip) ⇒ Some(Move(id, move, ip))
      case _                   ⇒ None
    }
  }

  val toIpLocation: Enumeratee[Option[Move], Option[IpLocation]] = Enumeratee.map[Option[Move]] { op ⇒
    op.flatMap(move ⇒ ipCache.get(move.ip))
  }

  val toLocation: Enumeratee[Option[IpLocation], Location] =
    Enumeratee.mapInput[Option[IpLocation]] {
      case Input.El(Some(iploc)) ⇒ {
        val loc = Location(iploc.countryName, iploc.city, iploc.latitude,
          iploc.longitude)
        Input.El(loc)
      }
      case _ ⇒ Input.Empty
    }

  val asJson: Enumeratee[Location, JsValue] = Enumeratee.map[Location] { loc ⇒
    Json.obj(
      "countryName" -> loc.countryName,
      "city" -> Json.toJson[Option[String]](loc.city),
      "latitude" -> loc.latitude,
      "longitude" -> loc.longitude
    )
  }

}

case class Location(
  countryName: String,
  city: Option[String],
  latitude: Float,
  longitude: Float)

case class Move(
  gameId: String,
  move: String,
  ip: String)
