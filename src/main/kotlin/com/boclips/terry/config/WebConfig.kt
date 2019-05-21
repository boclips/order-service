package com.boclips.terry.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
class WebConfig {

    @Bean
    fun forwardedHeaderFilter() =
        FilterRegistrationBean<ForwardedHeaderFilter>().apply {
            filter = ForwardedHeaderFilter()
        }
}
