package com.vdsirotkin.telegram.dickfind2bot.engine

import org.springframework.stereotype.Component

@Component
class GameEngine(
    private val mapGenerator: MapGenerator
) {

    private val gamesCache: MutableMap<Long, Game> = mutableMapOf()

    fun startNewEmptyGame(messageId: Long, userId: Long) {
        saveGame(messageId, Game(User(userId)))
    }

    fun user2join(messageId: Long, userId: Long): Game {
        val game = getGame(messageId)
        if (game.user1.chatId == userId) {
            return game
        }
        val updatedGame = game.copy(user2 = User(userId))
        saveGame(messageId, updatedGame)
        newRound(messageId)
        return updatedGame
    }

    fun newRound(messageId: Long) {
        val game = getGame(messageId)
        val roundMap = mapGenerator.generateNewMap()
        validateRoundIsOver(messageId, game)
        if (game.rounds.isEmpty()) {
            addRound(game, messageId, Round(roundMap))
        } else {
            val newOrder = game.rounds.maxOf { it.order }.plus(1)
            addRound(game, messageId, Round(roundMap, order = newOrder))
        }
    }

    fun usersTurn(messageId: Long, userChatId: Long, coordinates: Pair<Int, Int>): Entity {
        val game = getGame(messageId)
        val round = getCurrentRound(game, messageId)
        when {
            game.user1.chatId == userChatId -> {
                round.takeIf { it.user1Coordinates == null }?.copy(user1Coordinates = coordinates)?.also { updatedRound ->
                    saveUpdatedRound(game, updatedRound, messageId)
                }
            }
            game.user2?.chatId == userChatId -> {
                round.takeIf { it.user2Coordinates == null }?.copy(user2Coordinates = coordinates)?.also { updatedRound ->
                    saveUpdatedRound(game, updatedRound, messageId)
                }
            }
            else -> return Entity.UNKNOWN
        }
        return round.entitiesMap[coordinates.first][coordinates.second]
    }

    fun getCurrentRound(game: Game, messageId: Long) =
        game.rounds.maxByOrNull { it.order } ?: throw IllegalArgumentException("No round found for '$messageId'")

    fun finishRound(messageId: Long): Boolean {
        val game = getGame(messageId)
        val currentRound = getCurrentRound(game, messageId)
        if (currentRound.user1Coordinates != null && currentRound.user2Coordinates != null) {
            val updatedGame = determineWinner(game, currentRound)
            saveGame(messageId, updatedGame)
            return true
        }
        return false
    }

    private fun determineWinner(game: Game, currentRound: Round): Game {
        val (entitiesMap, user1Coordinates, user2Coordinates) = currentRound
        val user1Choice = entitiesMap[user1Coordinates!!.first][user1Coordinates.second]
        val user2Choice = entitiesMap[user2Coordinates!!.first][user2Coordinates.second]
        var resultGame = game
        if (user1Choice == Entity.GOLDEN_DICK) {
            game.user1.copy(score = game.user1.score + 9).also { resultGame = resultGame.copy(user1 = it) }
        }
        if (user2Choice == Entity.GOLDEN_DICK) {
            game.user2?.copy(score = game.user2.score + 9).also { resultGame = resultGame.copy(user2 = it) }
        }
        if (user1Choice == Entity.DICK && user2Choice == Entity.NOTHING) {
            game.user1.copy(score = game.user1.score + 1).also { resultGame = resultGame.copy(user1 = it) }
        } else if (user1Choice == Entity.NOTHING && user2Choice == Entity.DICK) {
            game.user2?.copy(score = game.user2.score + 1).also { resultGame = resultGame.copy(user2 = it) }
        }
        return resultGame
    }

    private fun saveUpdatedRound(game: Game, updatedRound: Round, messageId: Long) {
        game.rounds.sortedBy { it.order }.dropLast(1).toMutableList().apply { add(updatedRound) }.let { game.copy(rounds = it) }.also { saveGame(messageId, it) }
    }

    private fun validateRoundIsOver(messageId: Long, game: Game) {
        val lastRound = game.rounds.maxByOrNull { it.order } ?: return
        if (lastRound.user1Coordinates == null || lastRound.user2Coordinates == null) {
            throw IllegalArgumentException("Last round of '$messageId' game is not finished yet!")
        }
    }

    private fun addRound(game: Game, messageId: Long, round: Round) {
        game.rounds.toMutableList()
            .apply { add(round) }
            .let { game.copy(rounds = it) }
            .also { saveGame(messageId, it) }
    }

    fun getGame(messageId: Long): Game {
        return gamesCache[messageId] ?: throw IllegalArgumentException("Can't find game with message id '$messageId'")
    }

    private fun saveGame(messageId: Long, game: Game) {
        gamesCache[messageId] = game
    }
}