package com.boclips.terry.config

import com.boclips.terry.FakeSlackPoster
import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.SlackPoster
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestContext {
    @Bean
    fun slackPoster(): SlackPoster = FakeSlackPoster()

    @Bean
    fun terry(): Terry = Terry()

    @Bean
    fun slackSignature(): SlackSignature = SlackSignature(
            "v0",
            SLACK_SECRET_KEY_FOR_TEST.toByteArray()
    )

    @Bean
    fun slackRequestValidator(): SlackRequestValidator = SlackRequestValidator(
            terry = terry(),
            slackSignature = slackSignature(),
            objectMapper = jacksonObjectMapper()
    )
}

const val SLACK_SECRET_KEY_FOR_TEST = "foobar"
