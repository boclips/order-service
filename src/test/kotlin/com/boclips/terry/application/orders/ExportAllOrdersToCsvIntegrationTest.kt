package com.boclips.terry.application.orders

import com.boclips.terry.application.exceptions.InvalidExportRequest
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.presentation.orders.PoundFxRateRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.util.Currency

class ExportAllOrdersToCsvIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var exportAllOrdersToCsv: ExportAllOrdersToCsv

    @Test
    fun `can export orders to csv`() {
        val order = OrderFactory.order(
            status = OrderStatus.COMPLETED,
            items = listOf(
                OrderFactory.orderItem(
                    price = PriceFactory.tenPounds(),
                    video = TestFactories.video(
                        contentPartner = TestFactories.contentPartner(
                            currency = Currency.getInstance(
                                "AUD"
                            )
                        )
                    )
                )
            )
        )
        ordersRepository.save(order)

        val csvResource = exportAllOrdersToCsv(
                aud = BigDecimal.valueOf(2.313),
                usd = BigDecimal.ONE,
                eur = BigDecimal.ONE,
                sgd = BigDecimal.ONE
        )

        val firstRow = csvResource.parseCsv().first()
        assertThat(firstRow[ManifestCsvMetadata.FX_RATE]).isEqualTo("2.31")
        assertThat(firstRow[ManifestCsvMetadata.LICENSE_SALES_AMOUNT]).isEqualTo("23.13")
    }

    @Test
    fun `throws if request is invalid`() {
        assertThrows<InvalidExportRequest> {
            exportAllOrdersToCsv(
                    aud = null,
                    usd = BigDecimal.ONE,
                    eur = BigDecimal.ONE,
                    sgd = BigDecimal.ONE
            )
        }
    }
}
