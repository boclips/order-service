package com.boclips.terry.infrastructure.outgoing.videos

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FakeVideoServiceTests : VideoServiceTests() {
    @Before
    fun setUp() {
        videoService = FakeVideoService()
                .respondWith(FoundVideo(
                        videoId = "5c54d8cad8eafeecae2179af",
                        title = "Tesco opens new discount supermarket 'Jack's'",
                        description = expectedDescription,
                        thumbnailUrl = "https://cdnapisec.kaltura.com/p/1776261/thumbnail/entry_id/1_y0g6ftvy/height/250/vid_slices/3/vid_slice/2"
                ))

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

    val expectedDescription = "VOICED: Tesco has launched its own discount store today, in a bid to take on the thriving German supermarkets - Lidl and Aldi.The budget chain is called Jack's in tribute to Tesco's founder Jack Cohen.15 of the stores will open over the next year.\nInterviews with Dave Lewis, Tesco Chief Executive and Steve Dresser, Retail Analyst\nShows: New Jacks store and some products in the store on the 19th September 2018 in Chatteris, United Kingdom."

    @Test
    fun `retrieves videos that exist`() {
        assertThat(videoService!!.get("2584078"))
                .isEqualTo(FoundVideo(
                        videoId = "5c54d8cad8eafeecae2179af",
                        title = "Tesco opens new discount supermarket 'Jack's'",
                        description = expectedDescription,
                        thumbnailUrl = "https://cdnapisec.kaltura.com/p/1776261/thumbnail/entry_id/1_y0g6ftvy/height/250/vid_slices/3/vid_slice/2"
                ))
    }

    @Test
    fun `returns missing video when video doesn't exist`() {
        assertThat(missingVideoService!!.get("987654321"))
                .isEqualTo(MissingVideo(videoId = "987654321"))
    }
}
