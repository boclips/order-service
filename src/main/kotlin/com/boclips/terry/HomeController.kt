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

    @PostMapping("/slack-verification")
    fun slackVerification(@RequestBody request: SlackVerificationRequest?): SlackVerificationResponse {
        return request?.challenge?.let {
            val slackVerificationResponse = SlackVerificationResponse(challenge = it)
            logger.info { "Successfully parsed Slack verification request" }
            slackVerificationResponse
        } ?: throw IllegalArgumentException()
    }
}