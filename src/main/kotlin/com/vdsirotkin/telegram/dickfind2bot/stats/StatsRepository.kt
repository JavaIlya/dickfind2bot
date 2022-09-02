package com.vdsirotkin.telegram.dickfind2bot.stats

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StatsRepository : JpaRepository<StatsEntity, UserAndChatId> {
    @Query("from StatsEntity where id.chatId = :id order by wins desc")
    fun getTop(id: Long, pageable: Pageable): List<StatsEntity>
}
