package com.vdsirotkin.telegram.dickfind2bot.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
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
    fun redisConnectionFactory(redisConfigProperties: RedisConfigProperties): RedisConnectionFactory {
        val clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(2))
                .build()
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(redisConfigProperties.host, redisConfigProperties.port)
        redisStandaloneConfiguration.password = RedisPassword.of(redisConfigProperties.password)

        return LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig)
    }

    @Bean
    fun redisTemplate(redisFactory: RedisConnectionFactory): RedisTemplate<String, Game> {
        return RedisTemplate<String, Game>().apply {
            keySerializer = RedisSerializer.string()
            valueSerializer = GenericJackson2JsonRedisSerializer(ObjectMapper().registerKotlinModule().apply {
                activateDefaultTyping(this.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
            })
            setConnectionFactory(redisFactory)
        }
    }
}
