package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
@Profile("heroku")
class RedisCache (private val redisTemplate: RedisTemplate<String, Game>) : GamesCache {

    override fun getGame(gameId: String): Game? {
        return redisTemplate.opsForValue().get(gameId)
    }

    override fun saveGame(gameId: String, game: Game) {
        redisTemplate.opsForValue().set(gameId, game)
    }
}