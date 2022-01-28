package com.vdsirotkin.telegram.dickfind2bot.engine.cache

import com.vdsirotkin.telegram.dickfind2bot.engine.Game

interface GamesCache {

    fun getGame(messageId: Long): Game?

    fun saveGame(messageId: Long, game: Game)

}