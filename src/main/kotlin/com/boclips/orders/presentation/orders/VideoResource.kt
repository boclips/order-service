package com.boclips.orders.presentation.orders

import org.springframework.hateoas.Link

data class VideoResource(
    val id: String,
    val types: List<String>,
    val title: String,
    val videoReference: String,
    val maxResolutionAvailable: Boolean,
    val captionStatus: CaptionStatusResource,
    val _links: Map<String, Link>?
)
