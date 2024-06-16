package com.vdsirotkin.telegram.dickfind2bot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot")
data class BotConfig(
    val token: String,
    val goldenDickChance: Double,
    val bombChance: Double
)
