package com.vdsirotkin.telegram.dickfind2bot.stats

interface Event

data class FoundDickEvent(
    val chatId: Long,
    val userId: Long
) : Event

data class FoundGoldenDickEvent(
        val chatId: Long,
        val userId: Long
): Event

data class FoundNothingEvent(
    val chatId: Long,
    val userId: Long
) : Event

data class FoundBombEvent(
    val chatId: Long,
    val userId: Long
) : Event

data class GameFinishedEvent(
    val chatId: Long,
    val userWonId: Long,
    val userLostId: Long
) : Event

data class GameFinishedDrawEvent(
    val chatId: Long,
    val firstUserId: Long,
    val secondUserId: Long
) : Event

data class GameFinishedBothLoseEvent(
    val chatId: Long,
    val firstUserId: Long,
    val secondUserId: Long
)
