package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import io.lettuce.core.api.sync.RedisCommands

class RedisCache (private val redisTemplate: RedisCommands<String, String>, private val objectMapper: ObjectMapper) : GamesCache {

    override fun getGame(gameId: String): Game? {
        return redisTemplate.get(gameId)?.let {
            objectMapper.readValue(it)
        }
    }

    override fun saveGame(gameId: String, game: Game) {
        redisTemplate.set(gameId, objectMapper.writeValueAsString(game))
    }
}
