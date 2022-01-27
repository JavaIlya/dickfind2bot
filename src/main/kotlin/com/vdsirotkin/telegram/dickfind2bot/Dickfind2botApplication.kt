package com.vdsirotkin.telegram.dickfind2bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class Dickfind2botApplication

fun main(args: Array<String>) {
	runApplication<Dickfind2botApplication>(*args)
}
