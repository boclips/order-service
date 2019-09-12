package com.boclips.terry.presentation.resources

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.util.DateUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class OrderCsvUploadConverterTest {
    lateinit var ordersCsv: File

    private val orderUploadConverter: OrderCsvUploadConverter = OrderCsvUploadConverter

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
        assertThat(DateUtil.truncateTime(orderItem.requestDate!!)).isEqualTo("2019-08-30")
        assertThat(orderItem.fulfilmentDate).isEqualTo(null)
        assertThat(orderItem.quarter).isEqualTo("2019 Q3")
        assertThat(orderItem.memberRequest).isEqualTo("")
        assertThat(orderItem.memberAuthorise).isEqualTo("The Requester")
        assertThat(orderItem.title).isEqualTo("Why the Evolutionary Epic Matters: Crash Course Big History #203")
        assertThat(orderItem.source).isEqualTo("Crash Course History")
        assertThat(orderItem.sourceCode).isEqualTo("BigHistory203")
        assertThat(orderItem.licenseDuration).isEqualTo("5")
        assertThat(orderItem.territory).isEqualTo("Worldwide")
        assertThat(orderItem.type).isEqualTo("Instructional Clips")
        assertThat(orderItem.price).isEqualTo("USD $100")
        assertThat(orderItem.publisher).isEqualTo("A Great Organisation")
        assertThat(orderItem.isbnProductNumber).isEqualTo("Stile Education - Ed")
        assertThat(orderItem.language).isEqualTo("")
        assertThat(orderItem.captioning).isEqualTo("no")
        assertThat(orderItem.trim).isEqualTo("")
        assertThat(orderItem.notes).isEqualTo("")
        assertThat(orderItem.remittanceNotes).isEqualTo("")
    }
}
