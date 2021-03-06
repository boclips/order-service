package com.boclips.orders.presentation.resources

import com.boclips.orders.presentation.orders.OrderCsvUploadConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class OrderCsvUploadConverterTest {
    lateinit var ordersCsv: File

    private val orderUploadConverter: OrderCsvUploadConverter =
        OrderCsvUploadConverter

    @BeforeEach
    fun setup() {
        val classLoader = javaClass.classLoader
        ordersCsv = File(classLoader.getResource("master-orders.csv")!!.file)
    }

    @Test
    fun `can read a valid csv`() {
        val request = orderUploadConverter.convertToMetadata(orderCsv = ordersCsv.readBytes())
        assertThat(request).hasSize(1)

        val orderItem = request.first()
        assertThat(orderItem.legacyOrderId).isEqualTo("5d6cda057f0dc0dd363841ed")
        assertThat(orderItem.month).isEqualTo("Aug-19")
        assertThat(orderItem.requestDate).isEqualTo("30/08/2019")
        assertThat(orderItem.fulfilmentDate).isEqualTo(null)
        assertThat(orderItem.quarter).isEqualTo("2019 Q3")
        assertThat(orderItem.memberRequest).isEqualTo("The Requester")
        assertThat(orderItem.memberAuthorise).isEqualTo(null)
        assertThat(orderItem.title).isEqualTo("Why the Evolutionary Epic Matters: Crash Course Big History #203")
        assertThat(orderItem.source).isEqualTo("Crash Course History")
        assertThat(orderItem.sourceCode).isEqualTo("BigHistory203")
        assertThat(orderItem.licenseDuration).isEqualTo("5")
        assertThat(orderItem.territory).isEqualTo("Worldwide")
        assertThat(orderItem.type).isEqualTo("Instructional Clips")
        assertThat(orderItem.price).isEqualTo("USD $100")
        assertThat(orderItem.publisher).isEqualTo("A Great Organisation")
        assertThat(orderItem.isbnProductNumber).isEqualTo("Stile Education - Ed")
        assertThat(orderItem.language).isEqualTo(null)
        assertThat(orderItem.captioning).isEqualTo("no")
        assertThat(orderItem.trim).isEqualTo(null)
        assertThat(orderItem.notes).isEqualTo(null)
        assertThat(orderItem.remittanceNotes).isEqualTo(null)
        assertThat(orderItem.orderThroughPlatform).isEqualTo("yes")
    }
}
