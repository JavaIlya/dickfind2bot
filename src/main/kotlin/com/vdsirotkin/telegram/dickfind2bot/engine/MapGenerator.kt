package com.vdsirotkin.telegram.dickfind2bot.engine

import org.springframework.stereotype.Component

@Component
class MapGenerator {

    fun generateNewMap(): Array<Array<Entity>> {
        return Array(3) {
            Array(3) { Entity.values().filter { it != Entity.UNKNOWN && it != Entity.GOLDEN_DICK }.random() }
        }
    }

}