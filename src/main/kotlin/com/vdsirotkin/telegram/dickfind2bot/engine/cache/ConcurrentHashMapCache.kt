package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashMapCache : GamesCache {

    private val gamesCache: MutableMap<String, Game> = ConcurrentHashMap()

    override fun getGame(gameId: String): Game? {
        return gamesCache[gameId]
    }

    override fun saveGame(gameId: String, game: Game) {
        gamesCache[gameId] = game
    }

}