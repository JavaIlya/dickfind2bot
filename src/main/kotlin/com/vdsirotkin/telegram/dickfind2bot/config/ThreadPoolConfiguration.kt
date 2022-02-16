package com.vdsirotkin.telegram.dickfind2bot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class ThreadPoolConfiguration {

    @Bean
    fun chatBotAfterRoundDelayExecutor(): ThreadPoolTaskScheduler {
        return ThreadPoolTaskScheduler().apply {
            poolSize = 10
            setThreadFactory(CustomizableThreadFactory("chat-bot-delay-"))
        }
    }

}