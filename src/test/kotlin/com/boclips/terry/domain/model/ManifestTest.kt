package com.boclips.terry.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.ManifestFactory
import testsupport.OrderFactory
import testsupport.PriceFactory
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset

class ManifestTest {
    @Test
    fun `Convert orders into a manifest`() {
        val orderItem1 = OrderFactory.orderItem(price = PriceFactory.onePound())
        val orderItem2 = OrderFactory.orderItem(price = PriceFactory.tenPounds())
        val orderItem3 = OrderFactory.orderItem(price = PriceFactory.zeroEuros())
        val orders = arrayOf(
            OrderFactory.order(
                createdAt = LocalDate.of(2019, Month.APRIL, 3).atStartOfDay().toInstant(ZoneOffset.UTC),
                status = OrderStatus.COMPLETED,
                items = listOf(
                    orderItem1,
                    orderItem2
                )
            ),
            OrderFactory.order(
                createdAt = LocalDate.of(2019, Month.APRIL, 13).atStartOfDay().toInstant(ZoneOffset.UTC),
                status = OrderStatus.COMPLETED,
                items = listOf(
                    orderItem3
                )
            )
        )
        val expectedManifest = ManifestFactory.manifest(
            items = listOf(
                ManifestFactory.item(
                    video = orderItem1.video,
                    license = orderItem1.license,
                    orderDate = LocalDate.of(2019, Month.APRIL, 3),
                    salePrice = PriceFactory.onePound()
                ),
                ManifestFactory.item(
                    video = orderItem2.video,
                    license = orderItem2.license,
                    orderDate = LocalDate.of(2019, Month.APRIL, 3),
                    salePrice = PriceFactory.tenPounds()
                ),
                ManifestFactory.item(
                    video = orderItem3.video,
                    license = orderItem3.license,
                    orderDate = LocalDate.of(2019, Month.APRIL, 13),
                    salePrice = PriceFactory.zeroEuros()
                )
            )
        )

        val manifest = Manifest.from(*orders)

        assertThat(manifest).isEqualTo(expectedManifest)
    }
}
