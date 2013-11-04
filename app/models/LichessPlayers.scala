package models

import chessmap.Cache
import com.google.common.cache.{ Cache ⇒ GuavaCache }

final class LichessPlayers(aiLocations: Set[Location]) extends Cache {

  // to each game ID, associate list of player Locations
  // there can be 0, 1 or 2 players per game ID,
  // but this constraint is not expressed by the cache type :(
  private val cache: GuavaCache[String, List[Location]] = cache(1000)

  def getOpponentLocation(gameId: String, myLocation: Location): Option[Location] =

    Option(cache getIfPresent gameId) getOrElse Nil match {

      // new game ID, store player location
      case Nil                              ⇒
        cache.put(gameId, List(myLocation)); None

      // only my location is known
      case List(loc) if loc == myLocation   ⇒ None

      // only opponent location is known. Store mine
      case List(loc)                        ⇒
        cache.put(gameId, List(loc, myLocation)); Some(loc)

      // both locations are known
      case List(l1, l2) if l1 == myLocation ⇒ Some(l2)

      // both locations are known
      case List(l1, l2) if l2 == myLocation ⇒ Some(l1)

      // this game has more than 2 locations ! Gasp.
      // well this can happen when playing against the AI,
      // as moves can be routed to different AI instances.
      // return the non-AI location.
      case List(l1, l2) if aiLocations(l1)  ⇒ Some(l2)
      case List(l1, l2) if aiLocations(l2)  ⇒ Some(l1)
    }
}
