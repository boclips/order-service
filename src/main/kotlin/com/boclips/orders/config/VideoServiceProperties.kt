package com.boclips.orders.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("video.service")
class VideoServiceProperties {
    lateinit var uri: String
}