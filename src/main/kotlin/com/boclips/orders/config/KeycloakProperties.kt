package com.boclips.orders.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("keycloak")
@Component
class KeycloakProperties {
    lateinit var realm: String
    lateinit var url: String
}
