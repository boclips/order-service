package com.boclips.terry.config

import com.boclips.terry.FakeSlackPoster
import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.outgoing.SlackPoster
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestContext {
    @Bean
    fun slackPoster(): SlackPoster = FakeSlackPoster()

    @Bean
    fun terry(): Terry = Terry(slackPoster = slackPoster())
}
