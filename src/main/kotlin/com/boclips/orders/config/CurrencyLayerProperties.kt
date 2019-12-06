package com.boclips.orders.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("currencylayer")
class CurrencyLayerProperties {
    lateinit var accessKey: String
}