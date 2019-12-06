package com.boclips.orders.config

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.orders.infrastructure.Clock
import com.boclips.orders.infrastructure.FakeClock
import com.boclips.orders.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.orders.infrastructure.outgoing.slack.SlackPoster
import com.boclips.orders.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.orders.infrastructure.outgoing.videos.VideoService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestContext {

    @Bean
    fun fakeSlackPoster(): SlackPoster = FakeSlackPoster()

    @Bean
    @Primary
    fun fakeClock(): Clock = FakeClock()

    @Bean
    @Primary
    fun fakeVideoService(): VideoService = FakeVideoService()

    @Bean
    fun fakeKalturaClient(): KalturaClient = TestKalturaClient()

    @Bean
    fun eventBus(): EventBus = SynchronousFakeEventBus()
}
