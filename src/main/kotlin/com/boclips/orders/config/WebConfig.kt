package com.boclips.orders.config

import com.boclips.web.EnableBoclipsApiErrors
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
@EnableBoclipsApiErrors
class WebConfig {

    @Bean
    fun forwardedHeaderFilter() =
        FilterRegistrationBean<ForwardedHeaderFilter>().apply {
            filter = ForwardedHeaderFilter()
        }
}
