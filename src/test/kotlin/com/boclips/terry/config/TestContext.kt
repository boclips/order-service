package com.boclips.terry.config

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.Clock
import com.boclips.terry.infrastructure.FakeClock
import com.boclips.terry.infrastructure.orders.FakeLegacyOrdersRepository
import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import com.boclips.terry.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestContext(val mongoProperties: MongoProperties) {

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
    fun ordersRepository(): OrdersRepository =
        FakeOrdersRepository()

    @Bean
    fun legacyOrdersRepository(): LegacyOrdersRepository =
        FakeLegacyOrdersRepository()

    @Bean
    fun eventBus(): EventBus = SynchronousFakeEventBus()
}

