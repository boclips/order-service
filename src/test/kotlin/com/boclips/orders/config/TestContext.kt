package com.boclips.orders.config

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.orders.domain.service.EmailSender
import com.boclips.orders.domain.service.currency.FixedFxRateService
import com.boclips.orders.domain.service.currency.FxRateService
import com.boclips.orders.infrastructure.Clock
import com.boclips.orders.infrastructure.FakeClock
import com.boclips.orders.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.orders.infrastructure.outgoing.slack.SlackPoster
import com.boclips.orders.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.orders.infrastructure.outgoing.videos.VideoService
import com.boclips.orders.infrastructure.users.ApiUsersClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.nhaarman.mockitokotlin2.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import java.util.Currency

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

    @Bean
    fun testFxRateService(): FxRateService {
        return FixedFxRateService(
            mapOf(
                Currency.getInstance("EUR") to BigDecimal("1.25"),
                Currency.getInstance("USD") to BigDecimal("1.5"),
                Currency.getInstance("GBP") to BigDecimal("1.0")
            )
        )
    }

    @Bean
    @Primary
    fun fakeUserService(usersClient: UsersClient): ApiUsersClient {
        return ApiUsersClient(usersClient)
    }

    @Bean
    fun fakeEmailSender() = mock<EmailSender>()
}
