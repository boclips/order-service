package com.boclips.orders.domain.model

import com.boclips.orders.domain.service.ManifestConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.ManifestFactory
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.util.Currency

class ManifestConverterTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var manifestConverter: ManifestConverter

    @Test
    fun `Convert orders into a manifest`() {
        val orderItem1 = OrderFactory.orderItem(
            price = PriceFactory.onePound(),
            video = TestFactories.video(channel = TestFactories.channel(currency = Currency.getInstance("USD")))
        )
        val orderItem2 = OrderFactory.orderItem(
            price = PriceFactory.tenPounds(), video = TestFactories.video(
                channel = TestFactories.channel(currency = Currency.getInstance("USD"))
            )
        )
        val orderItem3 = OrderFactory.orderItem(
            price = PriceFactory.zeroEuros(),
            video = TestFactories.video(channel = TestFactories.channel(currency = Currency.getInstance("USD")))
        )
        val orders = arrayOf(
            OrderFactory.order(
                id = OrderId("123"),
                legacyOrderId = "1",
                createdAt = LocalDate.of(2019, Month.APRIL, 3).atStartOfDay().toInstant(ZoneOffset.UTC),
                status = OrderStatus.READY,
                items = listOf(
                    orderItem1,
                    orderItem2
                )
            ),
            OrderFactory.order(
                id = OrderId("2"),
                legacyOrderId = null,
                createdAt = LocalDate.of(2019, Month.APRIL, 13).atStartOfDay().toInstant(ZoneOffset.UTC),
                status = OrderStatus.INCOMPLETED,
                items = listOf(
                    orderItem3
                )
            )
        )
        val expectedManifest = ManifestFactory.manifest(
            items = listOf(
                ManifestFactory.item(
                    legacyOrderId = "1",
                    video = orderItem1.video,
                    license = orderItem1.license!!,
                    orderDate = LocalDate.of(2019, Month.APRIL, 3),
                    salePrice = PriceFactory.onePound(),
                    fxRate = BigDecimal.ONE.setScale(5),
                    orderStatus = OrderStatus.READY
                ),
                ManifestFactory.item(
                    legacyOrderId = "1",
                    video = orderItem2.video,
                    license = orderItem2.license!!,
                    orderDate = LocalDate.of(2019, Month.APRIL, 3),
                    salePrice = PriceFactory.tenPounds(),
                    fxRate = BigDecimal.ONE.setScale(5),
                    orderStatus = OrderStatus.READY
                ),
                ManifestFactory.item(
                    legacyOrderId = "2",
                    video = orderItem3.video,
                    license = orderItem3.license!!,
                    orderDate = LocalDate.of(2019, Month.APRIL, 13),
                    salePrice = PriceFactory.zeroEuros(),
                    fxRate = BigDecimal.ONE.setScale(5),
                    orderStatus = OrderStatus.INCOMPLETED
                )
            )
        )

        val manifest = manifestConverter.toManifest(
            mapOf(
                Currency.getInstance("GBP") to BigDecimal.ONE,
                Currency.getInstance("EUR") to BigDecimal.ONE,
                Currency.getInstance("USD") to BigDecimal.ONE
            ), *orders
        )

        assertThat(manifest).isEqualTo(expectedManifest)
    }

    @Test
    fun `Convert orders into a manifest doesn't fail on missing optional fields`() {
        val orderItem1 = OrderFactory.orderItem(
            price = Price(amount = null, currency = null),
            video = TestFactories.video(channel = TestFactories.channel(currency = Currency.getInstance("USD"))),
            license = null
        )

        val orders = arrayOf(
            OrderFactory.order(
                legacyOrderId = "legacy-order-id",
                createdAt = LocalDate.of(2019, Month.APRIL, 3).atStartOfDay().toInstant(ZoneOffset.UTC),
                status = OrderStatus.READY,
                items = listOf(orderItem1)
            )
        )
        val expectedManifest = ManifestFactory.manifest(
            items = listOf(
                ManifestFactory.item(
                    legacyOrderId = "legacy-order-id",
                    video = orderItem1.video,
                    license = null,
                    orderDate = LocalDate.of(2019, Month.APRIL, 3),
                    salePrice = PriceFactory.empty(),
                    fxRate = null,
                    orderStatus = OrderStatus.READY
                )
            )
        )

        val manifest = manifestConverter.toManifest(
            mapOf(
                Currency.getInstance("GBP") to BigDecimal.ONE,
                Currency.getInstance("EUR") to BigDecimal.ONE,
                Currency.getInstance("USD") to BigDecimal.ONE
            ), *orders
        )

        assertThat(manifest).isEqualTo(expectedManifest)
    }
}
