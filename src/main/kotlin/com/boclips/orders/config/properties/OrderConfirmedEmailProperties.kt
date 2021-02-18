package com.boclips.orders.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "order-confirmed-email")
data class  OrderConfirmedEmailProperties(
    var baseUrl: String = ""
)
