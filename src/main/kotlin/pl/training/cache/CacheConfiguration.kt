package pl.training.cache

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.Config

import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory

@EnableCaching
@Configuration
class CacheConfiguration {

    /*@Bean
    fun cacheManager(): CacheManager {
        return TransactionAwareCacheManagerProxy(ConcurrentMapCacheManager("reports"))
    }*/


   /* @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
        config.usePrefix()
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build()
    }*/



   /* @Bean
    fun hazelcastInstanceClient(): HazelcastInstance {
        val config = ClientConfig()
        config.clusterName = "training"
        config.networkConfig.addAddress("localhost:5701")
        return HazelcastClient.newHazelcastClient(config)
    }*/


    @Bean
    fun hazelcastInstanceClient(): HazelcastInstance {
        val config = Config()
        config.networkConfig.setPortAutoIncrement(true)
        config.networkConfig.join.multicastConfig
            .setEnabled(true)
            .setMulticastPort(2000)
        return Hazelcast.newHazelcastInstance(config)
    }

    @Bean
    fun cacheManager(hazelcastInstanceClient: HazelcastInstance): CacheManager {
        return HazelcastCacheManager(hazelcastInstanceClient)
    }


}
