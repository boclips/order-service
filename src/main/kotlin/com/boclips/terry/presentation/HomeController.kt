package com.boclips.terry.presentation

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.*
import com.boclips.terry.infrastructure.outgoing.SlackPoster
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController(
        private val terry: Terry,
        private val slackPoster: SlackPoster
) {
    companion object : KLogging()

    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack")
    fun slack(@RequestBody request: SlackRequest): Action {
        val decision = terry.receiveSlack(request)
        logger.info { decision.log }
        when (val action = decision.action) {
            is ChatPost ->
                slackPoster.chatPostMessage(action.message)
        }
        return decision.action
    }
}
