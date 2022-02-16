package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.vdsirotkin.telegram.dickfind2bot.engine.Game

interface GamesCache {

    fun getGame(gameId: String): Game?

    fun saveGame(gameId: String, game: Game)

}