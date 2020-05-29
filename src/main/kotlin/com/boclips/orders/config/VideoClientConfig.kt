package com.boclips.orders.config

import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.helper.ServiceAccountCredentials
import com.boclips.videos.api.httpclient.helper.ServiceAccountTokenFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class VideoClientConfig {
    @Bean
    fun channelsClient(properties: VideoServiceClientProperties): ChannelsClient {
        return ChannelsClient.create(
            apiUrl = properties.baseUrl,
            tokenFactory = ServiceAccountTokenFactory(
                ServiceAccountCredentials(
                    authEndpoint = properties.baseUrl,
                    clientId = properties.clientId,
                    clientSecret = properties.clientSecret
                )
            )
        )
    }

    @Bean
    fun videosClient(properties: VideoServiceClientProperties): VideosClient {
        return VideosClient.create(
            apiUrl = properties.baseUrl,
            tokenFactory = ServiceAccountTokenFactory(
                ServiceAccountCredentials(
                    authEndpoint = properties.baseUrl,
                    clientId = properties.clientId,
                    clientSecret = properties.clientSecret
                )
            )
        )
    }
}
