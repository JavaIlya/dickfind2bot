package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ConcurrentHashMapCache : GamesCache {

    private val gamesCache: MutableMap<Long, Game> = ConcurrentHashMap()

    override fun getGame(messageId: Long): Game? {
        return gamesCache[messageId]
    }

    override fun saveGame(messageId: Long, game: Game) {
        gamesCache[messageId] = game
    }

}