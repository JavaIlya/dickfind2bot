package com.vdsirotkin.telegram.dickfind2bot.engine

import com.vdsirotkin.telegram.dickfind2bot.BotConfig
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class MapGenerator(
        val botConfig: BotConfig
) {


    fun generateNewMap(): Array<Array<Entity>> {
        val map = Array(3) {
            Array(3) { Entity.values().filter { it != Entity.UNKNOWN && it != Entity.GOLDEN_DICK }.random() }
        }
        val shouldHaveGoldenDick = shouldHaveGoldenDick()
        val hasAtLeastOneDick = hasAtLeastOneDick(map);
        val hasAtLeastOneNothing = hasAtLeastOneNothing(map);

        if (shouldHaveGoldenDick) {
            populateGoldDickInMap(map);
        }

        if (hasAtLeastOneDick && hasAtLeastOneNothing) {
            return map;
        } else if (hasAtLeastOneNothing) {
            populateSimpleDickInMap(map)
        } else {
            populateNothingInMap(map);
        }

        return map;
    }


    private fun shouldHaveGoldenDick(): Boolean {
        return Random.Default.nextDouble() <= botConfig.goldenDickChance;
    }

    private fun hasAtLeastOneNothing(map: Array<Array<Entity>>): Boolean {
        return map.any { it ->
            it.any { it == Entity.NOTHING }
        };
    }

    private fun hasAtLeastOneDick(map: Array<Array<Entity>>): Boolean {
        return map.any { it ->
            it.any { it == Entity.DICK }
        };
    }

    private fun populateGoldDickInMap(map: Array<Array<Entity>>) {
        val row: Int = (0..2).random();
        val cell: Int = (0..2).random();

        map[row][cell] = Entity.GOLDEN_DICK;
    }

    private fun populateSimpleDickInMap(map: Array<Array<Entity>>) {
        val row: Int = (0..2).random();
        val cell: Int = (0..2).random();

        if (map[row][cell] == Entity.GOLDEN_DICK) {
            return populateSimpleDickInMap(map);
        }

        map[row][cell] = Entity.DICK;
    }

    private fun populateNothingInMap(map: Array<Array<Entity>>) {
        val row: Int = (0..2).random();
        val cell: Int = (0..2).random();

        if (map[row][cell] == Entity.GOLDEN_DICK) {
            return populateNothingInMap(map);
        }

        map[row][cell] = Entity.NOTHING;
    }

}