package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidVideoIdException
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import testsupport.TestFactories
import java.time.temporal.ChronoUnit

class OrderItemFieldConverterTest {
    private val videoProviderMock = mock(VideoProvider::class.java)

    private val itemFieldConverter = OrderItemFieldConverter(videoProviderMock)

    @BeforeEach
    fun setup() {
        doReturn(TestFactories.video())
            .whenever(videoProviderMock)
            .get(any())
    }

    @Test
    fun `converts transcript requests to true ignoring case`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "YES")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(true)
    }

    @Test
    fun `converts transcript requests to true`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "yes")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(true)
    }

    @Test
    fun `converts transcript requests to false`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "No")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(false)
    }

    @Test
    fun `converts transcript requests to false when empty`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(false)
    }

    @Test
    fun `converts trimming to a trim request`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(trim = "00:20 - 01:00")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.trim).isEqualTo(TrimRequest.WithTrimming(label = "00:20 - 01:00"))
    }

    @Test
    fun `converts license duration if valid`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(licenseDuration = "100")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.license.duration).isEqualTo(Duration.Time(amount = 100, unit = ChronoUnit.YEARS))
    }

    @Test
    fun `converts worldwide license territory`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(territory = "Worldwide")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.license.territory).isEqualTo(OrderItemLicense.WORLDWIDE)
    }

    @Test
    fun `converts video id if present`() {
        doReturn(TestFactories.video(videoServiceId = "1234"))
            .whenever(videoProviderMock)
            .get(VideoId(value = "1234"))

        val csvMetadataItem = TestFactories.csvOrderItemMetadata(videoId = "1234")

        val orderItem = itemFieldConverter.convert(csvMetadataItem)

        assertThat(orderItem.video.videoServiceId.value).isEqualTo("1234")
    }

    @Test
    fun `throws if video is missing from video provider`() {
        doReturn(null)
            .whenever(videoProviderMock)
            .get(VideoId(value = "1234"))

        assertThrows<InvalidVideoIdException> {
            itemFieldConverter.convert(TestFactories.csvOrderItemMetadata(videoId = "1234"))
        }
    }
}
