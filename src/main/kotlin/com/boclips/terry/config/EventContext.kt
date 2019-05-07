package com.boclips.terry.config

import com.boclips.events.spring.EnableBoclipsEvents
import org.springframework.context.annotation.Configuration

@Configuration
@EnableBoclipsEvents(appName = "order-service")
class EventContext
