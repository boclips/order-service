package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.time.temporal.ChronoUnit

class OrderItemFieldConverterTest {
    @Test
    fun `converts transcript requests to true ignoring case`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "YES")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(true)
    }

    @Test
    fun `converts transcript requests to true`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "yes")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(true)
    }

    @Test
    fun `converts transcript requests to false`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "No")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(false)
    }

    @Test
    fun `converts transcript requests to false when empty`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(captioning = "")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.transcriptRequested).isEqualTo(false)
    }

    @Test
    fun `converts trimming to a trim request`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(trim = "00:20 - 01:00")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.trim).isEqualTo(TrimRequest.WithTrimming(label = "00:20 - 01:00"))
    }

    @Test
    fun `converts license duration if valid`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(licenseDuration = "100")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.license.duration).isEqualTo(Duration.Time(amount = 100, unit = ChronoUnit.YEARS))
    }

    @Test
    fun `converts worldwide license territory`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(territory = "Worldwide")

        val orderItem = OrderItemFieldConverter().convert(csvMetadataItem)

        assertThat(orderItem.license.territory).isEqualTo(OrderItemLicense.WORLDWIDE)
    }
}
