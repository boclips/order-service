package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderStatus
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.util.Currency

class ExportAllOrdersToCsvIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var exportAllOrdersToCsv: ExportAllOrdersToCsv

    lateinit var retrievedOrder: Order

    @BeforeEach
    fun `before all` () {
        ordersRepository.deleteAll()

        val order = OrderFactory.order(
            legacyOrderId = "legacy-order-id",
            status = OrderStatus.READY,
            currency = Currency.getInstance("GBP"),
            items = listOf(
                OrderFactory.orderItem(
                    price = PriceFactory.tenPounds(),
                    video = TestFactories.video(
                        channel = TestFactories.channel(
                            currency = Currency.getInstance(
                                "AUD"
                            )
                        )
                    )
                )
            )
        )

        retrievedOrder = ordersRepository.save(order)
    }

    @Test
    fun `can export orders to csv`() {
        val csvResource = exportAllOrdersToCsv(
            aud = BigDecimal.valueOf(2.313),
            usd = BigDecimal.ONE,
            eur = BigDecimal.ONE,
            sgd = BigDecimal.ONE,
            cad = BigDecimal.ONE
        )

        val firstRow = csvResource.parseCsv().first()
        assertThat(firstRow[ManifestCsvMetadata.ORDER_ID]).isEqualTo(retrievedOrder.legacyOrderId)
        assertThat(firstRow[ManifestCsvMetadata.ORDER_STATUS]).isEqualTo(OrderStatus.READY.toString())
        assertThat(firstRow[ManifestCsvMetadata.FX_RATE]).isEqualTo("2.31")
        assertThat(firstRow[ManifestCsvMetadata.LICENSE_SALES_AMOUNT]).isEqualTo("23.13")
    }

    @Test
    fun `returns empty fields when rates are not given`() {
        val csvResource = exportAllOrdersToCsv(
            aud = null,
            usd = null,
            eur = null,
            sgd = null,
            cad = null
        )

        val firstRow = csvResource.parseCsv().first()
        assertThat(firstRow[ManifestCsvMetadata.ORDER_ID]).isEqualTo(retrievedOrder.legacyOrderId)
        assertThat(firstRow[ManifestCsvMetadata.ORDER_STATUS]).isEqualTo(OrderStatus.READY.toString())
        assertThat(firstRow[ManifestCsvMetadata.FX_RATE]).isEqualTo("")
        assertThat(firstRow[ManifestCsvMetadata.LICENSE_SALES_AMOUNT]).isEqualTo("")
    }
}
