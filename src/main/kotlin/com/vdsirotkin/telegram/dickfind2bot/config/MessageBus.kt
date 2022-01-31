package com.vdsirotkin.telegram.dickfind2bot.config

import com.vdsirotkin.telegram.dickfind2bot.stats.Event
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class MessageBus(
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun publish(event: Event) {
        applicationEventPublisher.publishEvent(event)
    }

}