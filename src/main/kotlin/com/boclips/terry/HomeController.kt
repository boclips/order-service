package com.boclips.terry

import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {
    companion object : KLogging()

    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack")
    fun slack(@RequestBody request: SlackRequest): SlackResponse =
            when (request) {
                is VerificationRequest ->
                    VerificationResponse(challenge = request.challenge)
                is EventNotification ->
                    EventNotificationResponse()
            }
}
