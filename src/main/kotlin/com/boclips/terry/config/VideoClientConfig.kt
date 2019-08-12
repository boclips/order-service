package com.boclips.terry.config

import com.boclips.videos.service.client.spring.EnableVideoServiceClient
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@EnableVideoServiceClient
@Configuration
class VideoClientConfig
