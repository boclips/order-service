package com.boclips.terry.infrastructure

import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.videos.service.client.CreateContentPartnerRequest
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories

internal class VideoServiceVideoProviderTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoProvider: VideoProvider

    @BeforeEach
    fun setUp() {
        fakeVideoClient.setUseInternalProjection(true)
    }

    @Test
    fun `can get a video`() {
        val contentPartnerId =
            fakeVideoClient.createContentPartner(
                CreateContentPartnerRequest
                    .builder()
                    .name("our content partner")
                    .build()
            )

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                title = "hello",
                providerId = contentPartnerId.value,
                contentType = VideoType.NEWS
            )
        )

        val video = videoProvider.get(videoId = VideoId(value = videoId.value))!!

        assertThat(video.title).isEqualTo("hello")
        assertThat(video.contentPartner.name).isEqualTo("our content partner")
    }

    @Test
    fun `null if video cannot be found`() {
        val video = videoProvider.get(videoId = VideoId(value = "hideandseek"))
        assertThat(video).isNull()
    }
}
