package com.vdsirotkin.telegram.dickfind2bot.engine

data class Game(
    val user1: User,
    val user2: User? = null,
    val rounds: List<Round> = emptyList()
)

data class User(
    val chatId: Long,
    val score: Int = 0
)

data class Round(
    val entitiesMap: Array<Array<Entity>>,
    val user1Coordinates: Pair<Int, Int>? = null,
    val user2Coordinates: Pair<Int, Int>? = null,
    val order: Int = 1
)

enum class Entity {
    DICK, NOTHING, GOLDEN_DICK, UNKNOWN
}