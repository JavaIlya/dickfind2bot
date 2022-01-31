package com.vdsirotkin.telegram.dickfind2bot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import java.util.concurrent.Executors

@Configuration
class EventBusConfig {

    @Bean
    fun applicationEventMulticaster(): ApplicationEventMulticaster {
        return SimpleApplicationEventMulticaster().apply {
            setTaskExecutor(Executors.newFixedThreadPool(8))
        }
    }

}