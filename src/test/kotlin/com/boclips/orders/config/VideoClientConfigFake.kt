package com.boclips.orders.config

import com.boclips.videos.api.httpclient.ContentPartnersClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.test.fakes.ContentPartnersClientFake
import com.boclips.videos.api.httpclient.test.fakes.VideosClientFake
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class VideoClientConfigFake {
    @Bean
    fun contentPartnersClient(): ContentPartnersClient {
        return ContentPartnersClientFake()
    }

    @Bean
    fun videosClient(): VideosClient {
        return VideosClientFake()
    }
}