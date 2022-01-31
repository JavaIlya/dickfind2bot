package com.vdsirotkin.telegram.dickfind2bot.stats

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StatsRepository : JpaRepository<StatsEntity, UserAndChatId>