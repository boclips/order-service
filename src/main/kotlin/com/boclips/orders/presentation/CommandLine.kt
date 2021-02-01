package com.boclips.orders.presentation

import com.boclips.orders.application.orders.SyncVideos
import mu.KLogging
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.system.exitProcess

@Component
class CommandLine(
    val env: Environment,
    val syncVideos: SyncVideos
) {
    companion object : KLogging()

    @PostConstruct
    fun onBoot() {
        when (env.getProperty("mode")) {
            "synchronise-videos" -> {
                syncVideos()
                exitProcess(0)
            }
        }
    }
}
