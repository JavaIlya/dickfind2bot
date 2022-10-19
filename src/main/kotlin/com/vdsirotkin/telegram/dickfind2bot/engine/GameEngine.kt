package com.vdsirotkin.telegram.dickfind2bot.engine

import com.vdsirotkin.telegram.dickfind2bot.engine.cache.GamesCache
import org.springframework.stereotype.Component

@Component
class GameEngine(
    private val mapGenerator: MapGenerator,
    private val gamesCache: GamesCache
) {

    fun startNewEmptyGame(gameId: String, userId: Long, firstName: String) {
        gamesCache.saveGame(gameId, Game(User(userId, firstName)))
    }

    fun secondPlayerJoin(gameId: String, userId: Long, firstName: String): Game {
        val game = getGame(gameId)
        val updatedGame = game.copy(secondPlayer = User(userId, firstName))
        gamesCache.saveGame(gameId, updatedGame)
        newRound(gameId)
        return gamesCache.getGame(gameId)!!
    }

    fun newRound(gameId: String) {
        val game = getGame(gameId)
        val roundMap = mapGenerator.generateNewMap()
        validateRoundIsOver(gameId, game)
        if (game.rounds.isEmpty()) {
            addRound(game, gameId, Round(roundMap))
        } else {
            val newOrder = game.rounds.maxOf { it.order }.plus(1)
            addRound(game, gameId, Round(roundMap, order = newOrder))
        }
    }

    fun usersTurn(gameId: String, userChatId: Long, coordinates: Pair<Int, Int>): Entity {
        val game = getGame(gameId)
        val round = getCurrentRound(game, gameId)
        when {
            game.firstPlayer.chatId == userChatId -> {
                round.takeIf { it.firstUserCoordinates == null }?.copy(firstUserCoordinates = coordinates)?.also { updatedRound ->
                    saveUpdatedRound(game, updatedRound, gameId)
                }
            }
            game.secondPlayer?.chatId == userChatId -> {
                round.takeIf { it.secondUserCoordinates == null }?.copy(secondUserCoordinates = coordinates)?.also { updatedRound ->
                    saveUpdatedRound(game, updatedRound, gameId)
                }
            }
            else -> return Entity.UNKNOWN
        }
        return round.entitiesMap[coordinates.first][coordinates.second]
    }

    fun getCurrentRound(game: Game, gameId: String) =
        game.rounds.maxByOrNull { it.order } ?: throw IllegalArgumentException("No round found for '$gameId'")

    fun tryFinishRound(gameId: String): Boolean {
        val game = getGame(gameId)
        val currentRound = getCurrentRound(game, gameId)
        if (currentRound.locked) return false
        if (currentRound.firstUserCoordinates != null && currentRound.secondUserCoordinates != null) {
            determineAndSaveWinner(gameId, game, currentRound)
            return true
        }
        return false
    }

    private fun determineAndSaveWinner(gameId: String, game: Game, currentRound: Round) {
        val (entitiesMap, firstUserCoordinates, secondUserCoordinates) = currentRound
        val firstUserChoice = entitiesMap[firstUserCoordinates!!.first][firstUserCoordinates.second]
        val secondUserChoice = entitiesMap[secondUserCoordinates!!.first][secondUserCoordinates.second]
        if (currentRound.locked) return
        var resultGame = game
        if (firstUserChoice == Entity.GOLDEN_DICK) {
            game.firstPlayer.copy(score = game.firstPlayer.score + 9).also { resultGame = resultGame.copy(firstPlayer = it) }
        }
        if (secondUserChoice == Entity.GOLDEN_DICK) {
            game.secondPlayer?.copy(score = game.secondPlayer.score + 9).also { resultGame = resultGame.copy(secondPlayer = it) }
        }
        if (firstUserChoice == Entity.BOMB) {
            game.firstPlayer.copy(score = -1).also { resultGame = resultGame.copy(firstPlayer = it) }
        }
        if (secondUserChoice == Entity.BOMB) {
            game.secondPlayer?.copy(score = -1).also { resultGame = resultGame.copy(secondPlayer = it) }
        }
        if (firstUserChoice == Entity.DICK && secondUserChoice == Entity.NOTHING) {
            game.firstPlayer.copy(score = game.firstPlayer.score + 1).also { resultGame = resultGame.copy(firstPlayer = it) }
        } else if (firstUserChoice == Entity.NOTHING && secondUserChoice == Entity.DICK) {
            game.secondPlayer?.copy(score = game.secondPlayer.score + 1).also { resultGame = resultGame.copy(secondPlayer = it) }
        }
        val updatedRound = currentRound.copy(locked = true)
        saveUpdatedRound(resultGame, updatedRound, gameId)
    }

    private fun saveUpdatedRound(game: Game, updatedRound: Round, gameId: String) {
        game.rounds.sortedBy { it.order }.dropLast(1).toMutableList().apply { add(updatedRound) }.let { game.copy(rounds = it) }.also {
            gamesCache.saveGame(gameId, it)
        }
    }

    private fun validateRoundIsOver(gameId: String, game: Game) {
        val lastRound = game.rounds.maxByOrNull { it.order } ?: return
        if (lastRound.firstUserCoordinates == null || lastRound.secondUserCoordinates == null) {
            throw IllegalArgumentException("Last round of '$gameId' game is not finished yet!")
        }
    }

    private fun addRound(game: Game, gameId: String, round: Round) {
        game.rounds.toMutableList()
            .apply { add(round) }
            .let { game.copy(rounds = it) }
            .also {
                gamesCache.saveGame(gameId, it)
            }
    }

    fun getGame(gameId: String): Game {
        return gamesCache.getGame(gameId) ?: throw IllegalArgumentException("Can't find game with message id '$gameId'")
    }

}
