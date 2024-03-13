package com.vdsirotkin.telegram.dickfind2bot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vdsirotkin.telegram.dickfind2bot.engine.InvitedPlayer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InvitedPlayerTest {

    val objectMapper = jacksonObjectMapper()

    @Test
    fun `test serialization and deserialization`() {
        objectMapper.writeValueAsString(InvitedPlayer.ChatId(123L, "dalbaeb"))
            .also { println(it) }
        val result = objectMapper.readValue<InvitedPlayer>("""{"@class":"com.vdsirotkin.telegram.dickfind2bot.engine.InvitedPlayer${"$"}ChatId","chatId":123,"firstName":"dalbaeb"}""")

        Assertions.assertTrue(result is InvitedPlayer.ChatId)
    }
}
