package com.boclips.terry.infrastructure

import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.terry.domain.exceptions.MissingCurrencyForContentPartner
import com.boclips.terry.domain.exceptions.VideoNotFoundException
import com.boclips.videos.service.client.ContentPartner
import com.boclips.videos.service.client.ContentPartnerId
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories
import java.util.Currency

internal class VideoServiceVideoProviderTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoProvider: VideoProvider

    @BeforeEach
    fun setUp() {
        fakeVideoClient.setUseInternalProjection(true)
    }

    @Test
    fun `can get a video`() {
        fakeVideoClient.createContentPartner(
            ContentPartner.builder()
                .contentPartnerId(ContentPartnerId("cp-id"))
                .name("our content partner")
                .currency(Currency.getInstance("GBP"))
                .build()
        )

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                title = "hello",
                providerId = "cp-id",
                contentType = VideoType.NEWS
            )
        )

        val video = videoProvider.get(videoId = VideoId(value = videoId.value))!!

        assertThat(video.title).isEqualTo("hello")
        assertThat(video.contentPartner.name).isEqualTo("our content partner")
    }

    @Test
    fun `exception if video cannot be found`() {
        assertThrows<VideoNotFoundException> {
            videoProvider.get(videoId = VideoId(value = "hideandseek"))
        }
    }

    @Test
    fun `exception if content partner has no currency defined`() {
        fakeVideoClient.createContentPartner(
            ContentPartner.builder()
                .contentPartnerId(ContentPartnerId("cp-id"))
                .name("our content partner")
                .currency(null)
                .build()
        )

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                title = "hello",
                providerId = "cp-id",
                contentType = VideoType.NEWS
            )
        )

        assertThrows<MissingCurrencyForContentPartner> {
            videoProvider.get(videoId = VideoId(value = videoId.value))
        }
    }
}
