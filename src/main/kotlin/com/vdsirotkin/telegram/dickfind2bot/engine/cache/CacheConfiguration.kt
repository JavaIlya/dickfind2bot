package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class CacheConfiguration {

    @Bean
    @Profile("heroku")
    fun gamesCache(redisTempate: RedisTemplate<String, Game>): GamesCache {
        logger.info { "Starting redis cache" }
        return RedisCache(redisTempate)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultCache(): GamesCache {
        return ConcurrentHashMapCache()
    }

    companion object : KLogging()

}