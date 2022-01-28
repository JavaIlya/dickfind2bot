package com.vdsirotkin.telegram.dickfind2bot.engine

data class Game(
        val firstPlayer: User,
        val secondPlayer: User? = null,
        val rounds: List<Round> = emptyList()
)

data class User(
    val chatId: Long,
    val firstName: String,
    val score: Int = 0
)

data class Round(
        val entitiesMap: Array<Array<Entity>>,
        val firstUserCoordinates: Pair<Int, Int>? = null,
        val secondUserCoordinates: Pair<Int, Int>? = null,
        val order: Int = 1,
        val locked: Boolean = false
)

enum class Entity(val value: String) {
    DICK("\uD83C\uDF46"),
    NOTHING("💨"),
    GOLDEN_DICK("\uD83C\uDF4C"),
    UNKNOWN("\uD83D\uDCE6");

    override fun toString(): String {
        return value;
    }


}