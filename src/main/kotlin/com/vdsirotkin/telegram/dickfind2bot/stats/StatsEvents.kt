package com.vdsirotkin.telegram.dickfind2bot.stats

interface Event

data class FoundDickEvent(
    val chatId: Long,
    val userId: Long
) : Event

data class FoundNothingEvent(
    val chatId: Long,
    val userId: Long
) : Event

data class GameFinishedEvent(
    val chatId: Long,
    val userWonId: Long,
    val userLostId: Long
) : Event