package com.boclips.terry.contracts

import com.boclips.terry.FakeVideoService
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import com.boclips.terry.infrastructure.outgoing.videos.HTTPVideoService
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FakeVideoServiceTests : VideoServiceTests() {
    @Before
    fun setUp() {
        videoService = FakeVideoService()
                .respondWith(FoundVideo(videoId = "5c54d8cad8eafeecae2179af", title = "Tesco opens new discount supermarket 'Jack's'"))
        missingVideoService = FakeVideoService()
                .respondWith(MissingVideo(videoId = "987654321"))
    }
}

class HTTPVideoServiceTests : VideoServiceTests() {
    @Before
    fun setUp() {
        videoService = HTTPVideoService("https://api.boclips.com/v1/videos")
        missingVideoService = HTTPVideoService("https://httpbin.org/status/404")
    }
}

abstract class VideoServiceTests {
    var videoService: VideoService? = null
    var missingVideoService: VideoService? = null

    @Test
    fun `retrieves videos that exist`() {
        assertThat(videoService!!.get("2584078"))
                .isEqualTo(FoundVideo(
                        videoId = "5c54d8cad8eafeecae2179af",
                        title = "Tesco opens new discount supermarket 'Jack's'"
                ))
    }

    @Test
    fun `returns missing video when video doesn't exist`() {
        assertThat(missingVideoService!!.get("987654321"))
                .isEqualTo(MissingVideo(videoId = "987654321"))
    }
}
