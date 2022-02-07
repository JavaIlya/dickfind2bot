package com.vdsirotkin.telegram.dickfind2bot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "bot")
data class BotConfig(
    val token: String,
    val goldenDickChance: Double
)