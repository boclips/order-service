package com.boclips.terry.config

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.slack.HTTPSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.HTTPVideoService
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("production")
class ProductionContext {
    @Bean
    fun slackPoster(): SlackPoster = HTTPSlackPoster(
        slackURI = "https://slack.com/api/chat.postMessage",
        botToken = System.getenv("SLACK_BOT_TOKEN")
    )

    @Bean
    fun videoService(): VideoService = HTTPVideoService(System.getenv("VIDEO_SERVICE_URI"))

    @Bean
    fun terry(): Terry = Terry()

    @Bean
    fun slackSignature(): SlackSignature = SlackSignature(
        "v0",
        System.getenv("SLACK_SIGNING_SECRET").toByteArray()
    )

    @Bean
    fun slackRequestValidator(): SlackRequestValidator = SlackRequestValidator(
        terry = terry(),
        slackSignature = slackSignature(),
        objectMapper = jacksonObjectMapper()
    )
}
