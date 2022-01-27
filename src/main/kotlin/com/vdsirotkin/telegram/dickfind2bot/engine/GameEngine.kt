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
        if (game.firstPlayer.chatId == userId) {
            return game
        }
        val updatedGame = game.copy(secondPlayer = User(userId))
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
            game.firstPlayer.chatId == userChatId -> {
                round.takeIf { it.firstUserCoordinates == null }?.copy(firstUserCoordinates = coordinates)?.also { updatedRound ->
                    saveUpdatedRound(game, updatedRound, messageId)
                }
            }
            game.secondPlayer?.chatId == userChatId -> {
                round.takeIf { it.secondUserCoordinates == null }?.copy(secondUserCoordinates = coordinates)?.also { updatedRound ->
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
        if (currentRound.firstUserCoordinates != null && currentRound.secondUserCoordinates != null) {
            val updatedGame = determineWinner(game, currentRound)
            saveGame(messageId, updatedGame)
            return true
        }
        return false
    }

    private fun determineWinner(game: Game, currentRound: Round): Game {
        val (entitiesMap, firstUserCoordinates, secondUserCoordinates) = currentRound
        val firstUserChoice = entitiesMap[firstUserCoordinates!!.first][firstUserCoordinates.second]
        val secondUserChoice = entitiesMap[secondUserCoordinates!!.first][secondUserCoordinates.second]
        var resultGame = game
        if (firstUserChoice == Entity.GOLDEN_DICK) {
            game.firstPlayer.copy(score = game.firstPlayer.score + 9).also { resultGame = resultGame.copy(firstPlayer = it) }
        }
        if (secondUserChoice == Entity.GOLDEN_DICK) {
            game.secondPlayer?.copy(score = game.secondPlayer.score + 9).also { resultGame = resultGame.copy(secondPlayer = it) }
        }
        if (firstUserChoice == Entity.DICK && secondUserChoice == Entity.NOTHING) {
            game.firstPlayer.copy(score = game.firstPlayer.score + 1).also { resultGame = resultGame.copy(firstPlayer = it) }
        } else if (firstUserChoice == Entity.NOTHING && secondUserChoice == Entity.DICK) {
            game.secondPlayer?.copy(score = game.secondPlayer.score + 1).also { resultGame = resultGame.copy(secondPlayer = it) }
        }
        return resultGame
    }

    private fun saveUpdatedRound(game: Game, updatedRound: Round, messageId: Long) {
        game.rounds.sortedBy { it.order }.dropLast(1).toMutableList().apply { add(updatedRound) }.let { game.copy(rounds = it) }.also { saveGame(messageId, it) }
    }

    private fun validateRoundIsOver(messageId: Long, game: Game) {
        val lastRound = game.rounds.maxByOrNull { it.order } ?: return
        if (lastRound.firstUserCoordinates == null || lastRound.secondUserCoordinates == null) {
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