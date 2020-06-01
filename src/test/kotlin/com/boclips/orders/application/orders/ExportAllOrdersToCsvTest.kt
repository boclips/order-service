package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.service.OrderService
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import testsupport.ManifestFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.Currency

class ExportAllOrdersToCsvTest {

    @Test
    fun `when all orders are COMPLETED`() {
        val orderService = mock<OrderService>()
        whenever(orderService.exportManifest(any())).thenReturn(
            ManifestFactory.manifest(
                items = listOf(
                    ManifestFactory.item(
                        orderDate = LocalDate.of(2019, Month.JUNE, 20),
                        salePrice = Price(BigDecimal.TEN, Currency.getInstance("USD")),
                        license = OrderItemLicense(Duration.Time(5, ChronoUnit.YEARS), "WW"),
                        video = TestFactories.video(
                            title = "Carbon Dioxide and Climate Change",
                            videoServiceId = "5c54d5e8d8eafeecae1ff471",
                            videoReference = "INT_LUMPTL_333_006",
                            channel = TestFactories.channel(
                                name = "1 Minute in a Museum",
                                currency = Currency.getInstance("GBP")
                            )
                        ),
                        fxRate = BigDecimal.valueOf(1.11)
                    ),
                    ManifestFactory.item(
                        orderDate = LocalDate.of(2019, Month.JUNE, 20),
                        salePrice = Price(BigDecimal.ONE, Currency.getInstance("USD")),
                        license = OrderItemLicense(Duration.Time(100, ChronoUnit.YEARS), "UK"),
                        video = TestFactories.video(
                            title = "Dispersal of the Tribes, The",
                            videoServiceId = "5c54d5f4d8eafeecae1ffba5",
                            videoReference = "INT_UN_28K_004",
                            channel = TestFactories.channel(
                                name = "1 Minute in a Museum",
                                currency = Currency.getInstance("GBP")
                            )
                        ),
                        fxRate = BigDecimal.valueOf(2.12)
                    ),
                    ManifestFactory.item(
                        orderDate = LocalDate.of(2019, Month.APRIL, 1),
                        salePrice = Price(BigDecimal.ZERO, Currency.getInstance("GBP")),
                        license = OrderItemLicense(Duration.Time(10, ChronoUnit.YEARS), "WW"),
                        video = TestFactories.video(
                            title = "Connecting Despite the Loss of Sight or Hearing",
                            videoServiceId = "5c54d5efd8eafeecae1ff874",
                            videoReference = "INT_IO_08K_011",
                            channel = TestFactories.channel(
                                name = "A content partner",
                                currency = Currency.getInstance("SGD")
                            )
                        ),
                        fxRate = BigDecimal.valueOf(0.5)
                    )
                )
            )
        )
        val expectedCSV =
            """ |Content Partner,Order date,boclips ID,Source ID,Title,License Duration,Territory,Sales Amount (Original Currency),FX Rate,License Currency,License Sales Amount
                |1 Minute in a Museum,2019-06-20,5c54d5e8d8eafeecae1ff471,INT_LUMPTL_333_006,Carbon Dioxide and Climate Change,5,WW,USD 10.00,1.11,GBP,11.10
                |1 Minute in a Museum,2019-06-20,5c54d5f4d8eafeecae1ffba5,INT_UN_28K_004,"Dispersal of the Tribes, The",100,UK,USD 1.00,2.12,GBP,2.12
                |A content partner,2019-04-01,5c54d5efd8eafeecae1ff874,INT_IO_08K_011,Connecting Despite the Loss of Sight or Hearing,10,WW,GBP 0.00,0.50,SGD,0.00""".trimMargin()

        val csvResource: Resource = ExportAllOrdersToCsv(orderService)(
            eur = BigDecimal.ONE,
            usd = BigDecimal.TEN,
            aud = BigDecimal.TEN,
            sgd = BigDecimal.valueOf(2.5),
            cad = BigDecimal.ONE
        )

        assertThat(csvResource.parseCsv()).isEqualTo(expectedCSV.parseCsv())
    }
}

fun Resource.parseCsv() = inputStream.readBytes().toString(Charset.defaultCharset()).parseCsv()

fun String.parseCsv(): Iterable<Map<String, Any>> = CsvMapper().let { mapper ->
    mapper.schema().withHeader().let { schema ->
        mapper.readerFor(Map::class.java)
            .with(schema)
            .readValues<Map<String, Any>>(this)
            .readAll()
    }
}

