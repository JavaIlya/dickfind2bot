package com.vdsirotkin.telegram.dickfind2bot.stats

import java.io.Serializable
import jakarta.persistence.*

@Entity
@Table(name = "statistics")
data class StatsEntity(
    @Id
    @EmbeddedId
    val id: UserAndChatId,
    val foundDicks: Int,
    val foundGoldenDicks: Int,
    val foundNothing: Int,
    val foundBombs: Int,
    val wins: Int,
    val loses: Int,
    val draws: Int,

    @Version
    val version: Long = -1
) {
    constructor(id: UserAndChatId) : this(id, 0, 0, 0, 0, 0, 0, 0)
}

fun StatsEntity.totalDuels() = wins + loses + draws

@Embeddable
data class UserAndChatId(
    val chatId: Long,
    val userId: Long
) : Serializable
