package com.boclips.terry

import com.boclips.events.spring.EnableBoclipsEvents
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TerryApplication

fun main(args: Array<String>) {
    runApplication<TerryApplication>(*args)
}
