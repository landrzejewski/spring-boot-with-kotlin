package pl.training.commons.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

fun interface CacheSupplier {

    fun supply(capacity: Int): Cache<String, Any>

}

@Aspect
@Component
class CacheAspect {

    private val caches = ConcurrentHashMap<String, Cache<String, Any>>()
    var cacheSupplier = CacheSupplier { LinkedHashMapCache(it) }
    private val log = Logger.getLogger(CacheAspect::class.java.name)

    @Around("@annotation(fromCache)")
    fun read(joinPoint: ProceedingJoinPoint, fromCache: FromCache): Any? {
        val cacheName = fromCache.value
        val cache = caches.getOrPut(cacheName) { cacheSupplier.supply(fromCache.capacity) }
        val key = generateKey(joinPoint)
        val value = cache.get(key)
        if (value != null) {
            log.info("Cache hit")
            return value
        }
        var result = joinPoint.proceed()
        cache.put(key, result)
        return result
    }

    private fun generateKey(joinPoint: ProceedingJoinPoint) = joinPoint.args
        .joinToString("") { it.toString() }

}