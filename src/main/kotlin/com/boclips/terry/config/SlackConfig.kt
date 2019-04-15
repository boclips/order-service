package com.boclips.terry.config

import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.slack.HTTPSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackConfig {

    @Bean
    fun slackSignature(): SlackSignature = SlackSignature(
            "v0",
            System.getenv("SLACK_SIGNING_SECRET").toByteArray()
    )

    @Bean
    @ConditionalOnMissingBean(SlackPoster::class)
    fun slackPoster(): SlackPoster = HTTPSlackPoster(
            botToken = System.getenv("SLACK_BOT_TOKEN")
    )

}