package com.vdsirotkin.telegram.dickfind2bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.*
import com.vdsirotkin.telegram.dickfind2bot.engine.Game
import com.vdsirotkin.telegram.dickfind2bot.engine.GameEngine
import com.vdsirotkin.telegram.dickfind2bot.engine.Round
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DickfindBot(
    private val botConfig: BotConfig,
    private val gameEngine: GameEngine
) : TelegramBot(botConfig.token) {

    @PostConstruct
    fun init() {
        setUpdatesListener {
            it.forEach {
                try {
                    onUpdate(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }
    }

    private fun onUpdate(update: Update) {
        if (!isGroup(update)) {
            return
        }
        when {
            update.message()?.text()?.startsWith("/duel") == true -> startDuel(update.message())
            update.callbackQuery()?.data()?.startsWith("join") == true -> joinGame(update.callbackQuery())
            update.callbackQuery()?.data()?.startsWith("turn") == true -> handleTurn(update.callbackQuery())
        }
    }

    private fun startDuel(message: Message) {
        val response = execute(SendMessage(message.chat().id(), "@${
            message.from().username()
        } хочет поискать писюны. Кто тож?").replyMarkup(InlineKeyboardMarkup().addRow(InlineKeyboardButton("Присоединиться").callbackData("join"))))
        gameEngine.startNewEmptyGame(response.message().messageId().toLong(), message.from().id())
    }

    private fun joinGame(callbackQuery: CallbackQuery) {
        val game = gameEngine.user2join(callbackQuery.message().messageId().toLong(), callbackQuery.from().id())
        if (game.secondPlayer != null) {
            sendNewRound(callbackQuery, game)
        }
    }

    private fun sendNewRound(callbackQuery: CallbackQuery, game: Game) {
        execute(EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), """
                Дуэль
                
                @${execute(GetChatMember(callbackQuery.message().chat().id(), game.firstPlayer.chatId)).chatMember().user().username()} - ${game.firstPlayer.score}/3
                @${execute(GetChatMember(callbackQuery.message().chat().id(), game.secondPlayer!!.chatId)).chatMember().user().username()} - ${game.secondPlayer?.score}/3
            """.trimIndent()).replyMarkup(InlineKeyboardMarkup().apply {
            repeat(3) {
                addRow(InlineKeyboardButton("\uD83D\uDCE6").callbackData("turn_${it}_0"), InlineKeyboardButton("\uD83D\uDCE6").callbackData("turn_${it}_1"), InlineKeyboardButton("\uD83D\uDCE6").callbackData("turn_${it}_2"))
            }
        }))
    }

    private fun handleTurn(callbackQuery: CallbackQuery) {
        val split = callbackQuery.data().split("_")
        val messageId = callbackQuery.message().messageId().toLong()
        val usersTurnResult = gameEngine.usersTurn(messageId, callbackQuery.from().id(), split[1].toInt() to split[2].toInt())
        val resultItem = usersTurnResult
        execute(AnswerCallbackQuery(callbackQuery.id()).text("Ты нашел ${usersTurnResult}"))
        val roundFinished = gameEngine.finishRound(messageId)
        if (roundFinished) {
            val game = gameEngine.getGame(messageId)
            val currentRound = gameEngine.getCurrentRound(game, messageId)
            if (game.firstPlayer.score >= 3 || game.secondPlayer?.score!! >= 3) {
                handleFinishGame(callbackQuery, game, currentRound, messageId)
                return
            }
            execute(EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), """
                Дуэль
                
                @${execute(GetChatMember(callbackQuery.message().chat().id(), game.firstPlayer.chatId)).chatMember().user().username()} - ${game.firstPlayer.score}/3
                @${execute(GetChatMember(callbackQuery.message().chat().id(), game.secondPlayer!!.chatId)).chatMember().user().username()} - ${game.secondPlayer?.score}/3
                
                @${
                execute(GetChatMember(callbackQuery.message().chat().id(), game.firstPlayer.chatId)).chatMember().user().username()
            } нашол ${currentRound.entitiesMap[currentRound.user1Coordinates!!.first][currentRound.user1Coordinates!!.second]}
                @${
                execute(GetChatMember(callbackQuery.message().chat().id(), game.secondPlayer!!.chatId)).chatMember().user().username()
            } нашол ${currentRound.entitiesMap[currentRound.user2Coordinates!!.first][currentRound.user2Coordinates!!.second]}
            """.trimIndent()).replyMarkup(InlineKeyboardMarkup().apply {
                currentRound.entitiesMap.forEach {
                    addRow(*it.map { InlineKeyboardButton(it.value).callbackData("placeholder") }.toTypedArray())
                }
            }))
            GlobalScope.launch {
                delay(2000)
                gameEngine.newRound(messageId)
                sendNewRound(callbackQuery, gameEngine.getGame(messageId))
            }
        }
    }

    private fun handleFinishGame(callbackQuery: CallbackQuery, game: Game, currentRound: Round, messageId: Long) {
        val winner = if (game.firstPlayer.score >= 3) {
            execute(GetChatMember(callbackQuery.message().chat().id(), game.firstPlayer.chatId)).chatMember().user().username()
        } else {
            execute(GetChatMember(callbackQuery.message().chat().id(), game.secondPlayer!!.chatId)).chatMember().user().username()
        }
        execute(EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), """
                Дуэль
                
                @${execute(GetChatMember(callbackQuery.message().chat().id(), game.firstPlayer.chatId)).chatMember().user().username()} - ${game.firstPlayer.score}/3
                @${execute(GetChatMember(callbackQuery.message().chat().id(), game.secondPlayer!!.chatId)).chatMember().user().username()} - ${game.secondPlayer?.score}/3
                
                Победитель - @${winner}
            """.trimIndent()).replyMarkup(InlineKeyboardMarkup().apply {
            currentRound.entitiesMap.forEach {
                addRow(*it.map { InlineKeyboardButton(it.value).callbackData("placeholder") }.toTypedArray())
            }
        }))
    }

    private fun isGroup(update: Update): Boolean {
        val chat = update.message()?.chat() ?: update.callbackQuery()?.message()?.chat()
        val type = chat?.type() ?: return false
        return when (type) {
            Chat.Type.group, Chat.Type.supergroup, Chat.Type.channel -> true
            else -> false
        }
    }

}