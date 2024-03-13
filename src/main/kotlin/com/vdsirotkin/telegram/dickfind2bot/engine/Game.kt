package com.vdsirotkin.telegram.dickfind2bot.engine

import java.io.Serializable

sealed interface InvitedPlayer {
    data class ChatId(val chatId: Long) : InvitedPlayer
    data class Username(val username: String) : InvitedPlayer
}

data class Game(
        val firstPlayer: User,
        val secondPlayer: User? = null,
        val rounds: List<Round> = emptyList(),
        val invitedPlayer: InvitedPlayer? = null,
): Serializable


data class User(
    val chatId: Long,
    val firstName: String,
    val score: Int = 0
) : Serializable

data class Round(
        val entitiesMap: Array<Array<Entity>>,
        val firstUserCoordinates: Pair<Int, Int>? = null,
        val secondUserCoordinates: Pair<Int, Int>? = null,
        val order: Int = 1,
        val locked: Boolean = false
): Serializable

enum class Entity(val value: String) {
    DICK("\uD83C\uDF46"),
    NOTHING("💨"),
    GOLDEN_DICK("\uD83C\uDF4C"),
    BOMB("\uD83D\uDCA3"),
    UNKNOWN("\uD83D\uDCE6");

    override fun toString(): String {
        return value;
    }


}
