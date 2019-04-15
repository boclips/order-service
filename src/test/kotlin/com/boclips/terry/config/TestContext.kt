package com.boclips.terry.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.Clock
import com.boclips.terry.infrastructure.FakeClock
import com.boclips.terry.infrastructure.incoming.SlackRequestValidator
import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestContext {

    @Bean
    @Primary
    fun fakeSlackPoster(): SlackPoster = FakeSlackPoster()

    @Bean
    @Primary
    fun fakeClock(): Clock = FakeClock()

    @Bean
    @Primary
    fun fakeVideoService(): VideoService = FakeVideoService()

    @Bean
    @Primary
    fun fakeKalturaClient(): KalturaClient =
        TestKalturaClient()
}
