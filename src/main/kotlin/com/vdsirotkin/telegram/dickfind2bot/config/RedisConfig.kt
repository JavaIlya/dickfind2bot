package com.vdsirotkin.telegram.dickfind2bot.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.sync.RedisCommands
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration


@Configuration
@Profile("heroku")
class RedisConfig {

    @ConstructorBinding
    @ConfigurationProperties("spring.redis")
    data class RedisConfigProperties(
            val host: String,
            val port: Int,
            val password: String
    )

    @Bean
    fun statefulRedisConnection(config: RedisConfigProperties): RedisCommands<String, String> {
        return RedisClient.create().connect(RedisURI(config.host, config.port, Duration.ofSeconds(10))).sync()
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }
}
