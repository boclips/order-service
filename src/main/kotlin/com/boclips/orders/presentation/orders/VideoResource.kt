package com.boclips.orders.presentation.orders

import org.springframework.hateoas.Link

data class VideoResource(
    val id: String,
    val type: String,
    val title: String,
    val videoReference: String,
    val _links: Map<String, Link>?
)
