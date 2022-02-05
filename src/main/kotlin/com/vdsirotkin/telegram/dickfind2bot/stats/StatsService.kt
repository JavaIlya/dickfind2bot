package com.vdsirotkin.telegram.dickfind2bot.stats

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class StatsService(
    private val statsRepository: StatsRepository
) {

    @Transactional
    fun incrementNothing(id: UserAndChatId) {
        val entity = statsRepository.findById(id).orElse(StatsEntity(id))
        entity.copy(foundNothing = entity.foundNothing + 1).also {
            statsRepository.save(it)
        }
    }

    @Transactional
    fun incrementDicks(id: UserAndChatId) {
        val entity = statsRepository.findById(id).orElse(StatsEntity(id))
        entity.copy(foundDicks = entity.foundDicks + 1).also {
            statsRepository.save(it)
        }
    }

    @Transactional
    fun saveGameResults(winnerId: UserAndChatId, loserId: UserAndChatId) {
        val winner = statsRepository.findById(winnerId).orElseThrow { throw IllegalArgumentException("Can't find stats for winner $winnerId") }
        winner.copy(wins = winner.wins + 1).also {
            statsRepository.save(it)
        }
        val loser = statsRepository.findById(loserId).orElseThrow { throw IllegalArgumentException("Can't find stats for loser $loserId") }
        loser.copy(loses = loser.loses + 1).also {
            statsRepository.save(it)
        }
    }

    @Transactional
    fun getStats(userAndChatId: UserAndChatId, name: String): String {
        val optionalUser = statsRepository.findById(userAndChatId);

        if (optionalUser.isEmpty) {
            return "Про тебя нет никакой инфы. Отъебись."
        }

        val user = optionalUser.get();

        return """
             Стата пользователя $name
            
             Найдено членов: ${user.foundDicks}
             Найдено ЗОЛОТЫХ членов: ${user.foundGoldenDicks}
             Найдено пустых коробок: ${user.foundNothing}
             
             Дуэли:
             Победы: ${user.wins}
             Пососы: ${user.loses}
        """.trimIndent()
    }

}