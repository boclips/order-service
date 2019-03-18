package com.boclips.terry

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {
    @GetMapping("/")
    fun index() = "<h1>Do as I say, and do not do as I do</h1>"

    @PostMapping("/slack-verification")
    fun slackVerification(@RequestBody request: SlackVerificationRequest?) =
            SlackVerificationResponse(challenge = request!!.challenge)
}