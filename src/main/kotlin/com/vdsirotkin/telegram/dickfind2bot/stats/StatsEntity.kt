package com.vdsirotkin.telegram.dickfind2bot.stats

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "statistics")
data class StatsEntity(
    @Id
    @EmbeddedId
    val id: UserAndChatId,
    val foundDicks: Int,
    val foundGoldenDicks: Int,
    val foundNothing: Int,
    val wins: Int,
    val loses: Int,

    @Version
    val version: Long = -1
) {
    constructor(id: UserAndChatId) : this(id, 0, 0, 0, 0, 0)
}

@Embeddable
data class UserAndChatId(
    val chatId: Long,
    val userId: Long
) : Serializable
