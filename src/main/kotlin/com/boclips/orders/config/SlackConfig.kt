package com.boclips.orders.config

import com.boclips.orders.infrastructure.Clock
import com.boclips.orders.infrastructure.MultiReadHttpServletRequest
import com.boclips.orders.infrastructure.incoming.RawSlackRequest
import com.boclips.orders.infrastructure.incoming.SignatureMismatch
import com.boclips.orders.infrastructure.incoming.SlackSignature
import com.boclips.orders.infrastructure.incoming.StaleTimestamp
import com.boclips.orders.infrastructure.incoming.Verified
import com.boclips.orders.infrastructure.outgoing.slack.HTTPSlackPoster
import com.boclips.orders.infrastructure.outgoing.slack.SlackPoster
import mu.KLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class SlackConfig {

    @Bean
    fun slackSignature(slackProperties: SlackProperties): SlackSignature = SlackSignature(
        "v0",
        slackProperties.signingSecret.toByteArray()
    )

    @Bean
    @ConditionalOnMissingBean(SlackPoster::class)
    fun slackPoster(slackProperties: SlackProperties): SlackPoster = HTTPSlackPoster(
        botToken = slackProperties.botToken
    )
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SlackSignatureValidatorFilter(val slackSignature: SlackSignature, val clock: Clock) : HttpFilter() {
    companion object : KLogging()

    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val wrapper = MultiReadHttpServletRequest(request)
        val rawSlackRequest = RawSlackRequest.fromRequest(wrapper, clock.read())

        rawSlackRequest?.let {
            return when (slackSignature.verify(rawSlackRequest)) {
                SignatureMismatch ->
                    response.run {
                        logger.warn { "Signature mismatch: $rawSlackRequest" }
                        status = 401
                        flushBuffer()
                    }

                StaleTimestamp ->
                    response.run {
                        logger.warn { "Stale timestamp: $rawSlackRequest" }
                        status = 401
                        flushBuffer()
                    }

                Verified ->
                    chain.doFilter(wrapper, response)

            }
        } ?: chain.doFilter(wrapper, response)
    }
}
