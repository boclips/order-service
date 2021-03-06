package com.boclips.orders.config

import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.test.fakes.ChannelsClientFake
import com.boclips.videos.api.httpclient.test.fakes.VideosClientFake
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class UserClientConfigFake {
    @Bean
    fun usersClient(): UsersClient {
        return UsersClientFake()
    }
}
