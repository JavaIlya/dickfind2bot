package com.vdsirotkin.telegram.dickfind2bot.config

import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@Profile("heroku")
class RedisConfig(
) {


    @Bean
    fun redisTemplate(redisFactory: RedisConnectionFactory): RedisTemplate<String, Game> {
        val template = RedisTemplate<String, Game>();
        template.setConnectionFactory(redisFactory);
        return template;
    }
}