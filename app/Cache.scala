package chessmap

import com.google.common.cache.{ CacheLoader, LoadingCache, CacheBuilder, Cache => GuavaCache }

trait Cache {

  def cache[K, V](size: Long, f: K ⇒ V): LoadingCache[K, V] =
    CacheBuilder.newBuilder()
      .maximumSize(size)
      .asInstanceOf[CacheBuilder[K, V]]
      .build[K, V](f)

  def cache[K, V](size: Long): GuavaCache[K, V] =
    CacheBuilder.newBuilder()
      .maximumSize(size)
      .asInstanceOf[CacheBuilder[K, V]]
      .build[K, V]

  private implicit def functionToGoogleCacheLoader[T, R](f: T ⇒ R): CacheLoader[T, R] =
    new CacheLoader[T, R] {
      def load(p1: T) = f(p1)
    }
}
