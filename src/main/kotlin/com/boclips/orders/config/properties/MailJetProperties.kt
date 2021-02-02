package com.boclips.orders.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "mailjet")
data class MailJetProperties(
    var apiKey: String = "",
    var apiSecretKey: String = ""
)
