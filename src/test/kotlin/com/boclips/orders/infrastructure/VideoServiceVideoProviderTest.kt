package com.boclips.orders.infrastructure

import com.boclips.orders.domain.exceptions.MissingCurrencyForContentPartner
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.video.VideoResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import java.util.Currency

internal class VideoServiceVideoProviderTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoProvider: VideoProvider

    @Test
    fun `can get a video`() {
        val contentPartner = fakeContentPartnersClient.add(
            ContentPartnerResource(
                id = "cp-id",
                name = "our content partner",
                currency = Currency.getInstance("GBP").currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoId = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "our content partner",
                contentPartnerId = contentPartner.id,
                contentPartnerVideoId = "",
                _links = null
            )
        )

        val video = videoProvider.get(videoId = VideoId(value = videoId.id!!))

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
        fakeContentPartnersClient.add(
            ContentPartnerResource(
                id = "cp-id",
                name = "our content partner",
                currency = null,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoResource = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "creator",
                contentPartnerId = "cp-id",
                contentPartnerVideoId = "x",
                _links = null
            )
        )

        assertThrows<MissingCurrencyForContentPartner> {
            videoProvider.get(videoId = VideoId(value = videoResource.id!!))
        }
    }
}
