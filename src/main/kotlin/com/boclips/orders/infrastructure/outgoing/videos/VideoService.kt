package com.boclips.orders.infrastructure.outgoing.videos

interface VideoService {
    fun get(videoId: String): VideoServiceResponse
}