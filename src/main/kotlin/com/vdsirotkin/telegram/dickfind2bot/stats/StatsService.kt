package com.vdsirotkin.telegram.dickfind2bot.stats

import org.springframework.data.domain.Pageable
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
    fun incrementGoldenDicks(id: UserAndChatId) {
        val entity = statsRepository.findById(id).orElse(StatsEntity(id))
        entity.copy(foundGoldenDicks = entity.foundGoldenDicks + 1).also {
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
    fun saveDrawGameResults(firstUser: UserAndChatId, secondUser: UserAndChatId) {
        val players = statsRepository.findAllById(listOf(firstUser, secondUser));
        players.forEach {
            it.copy(draws = it.draws + 1).also {
                statsRepository.save(it)
            }
        };
    }

    fun getStats(userAndChatId: UserAndChatId, name: String): String {
        val user = statsRepository.findById(userAndChatId).orElse(null) ?:
            return "Про тебя нет никакой инфы. Отьебись."

        val totalFound = user.foundDicks + user.foundGoldenDicks + user.foundNothing;
        val totalDuels = user.wins + user.loses + user.draws;

        return """
             Стата пользователя $name
            
             Найдено членов: ${user.foundDicks} (${getPercent(totalFound, user.foundDicks)}%)
             Найдено ЗОЛОТЫХ членов: ${user.foundGoldenDicks} (${getPercent(totalFound, user.foundGoldenDicks)}%)
             Найдено пустых коробок: ${user.foundNothing} (${getPercent(totalFound, user.foundNothing)}%)
             
             Дуэли:
             Победы: ${user.wins} (${getPercent(totalDuels, user.wins)}%)
             Пососы: ${user.loses} (${getPercent(totalDuels, user.loses)}%)
             Ничьи: ${user.draws} (${getPercent(totalDuels, user.draws)}%)
        """.trimIndent()
    }

    private fun getPercent(total: Int, desired: Int): String {
        if (desired == 0) return "0";
        val percent = (desired.toDouble() * 100 / total.toDouble());
        return String.format("%.2f", percent);
    }

    fun getTopStats(id: Long): List<TopStatsDto> {
        return statsRepository.getTop(id, Pageable.ofSize(15)).map { TopStatsDto(it.id.userId, it.wins) }
    }
}
