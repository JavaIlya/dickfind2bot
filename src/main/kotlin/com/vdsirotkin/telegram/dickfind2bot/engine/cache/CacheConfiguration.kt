package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.api.sync.RedisCommands
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class CacheConfiguration {

    @Bean
    @Profile("heroku")
    fun gamesCache(redisTempate: RedisCommands<String, String>, objectMapper: ObjectMapper): GamesCache {
        logger.info { "Starting redis cache" }
        return RedisCache(redisTempate, objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultCache(): GamesCache {
        return ConcurrentHashMapCache()
    }

    companion object : KLogging()

}
