package com.boclips.terry.config

import com.boclips.terry.infrastructure.Clock
import com.boclips.terry.infrastructure.MultiReadHttpServletRequest
import com.boclips.terry.infrastructure.incoming.RawSlackRequest
import com.boclips.terry.infrastructure.incoming.SignatureMismatch
import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.incoming.StaleTimestamp
import com.boclips.terry.infrastructure.incoming.Verified
import com.boclips.terry.infrastructure.outgoing.slack.HTTPSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
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
        botToken = slackProperties.slackBotToken
    )
}

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class SlackSignatureValidatorFilter(val slackSignature: SlackSignature, val clock: Clock) : HttpFilter() {

    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val wrapper = MultiReadHttpServletRequest(request)
        val rawSlackRequest = RawSlackRequest.fromRequest(wrapper, clock.read())

        rawSlackRequest?.let {
            when (slackSignature.verify(rawSlackRequest)) {
                SignatureMismatch, StaleTimestamp ->
                    response.run {
                        status = 401
                        flushBuffer()
                    }

                Verified ->
                    chain.doFilter(wrapper, response)

            }
        } ?: chain.doFilter(wrapper, response)
    }
}
