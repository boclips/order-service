package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.model.Price
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Currency

class ExportAllOrdersToCsvTest {

    @Test
    fun `when valid CSV`() {
        val ordersRepository = mock<OrdersRepository>()
        whenever(ordersRepository.findAll()).thenReturn(
            listOf(
                OrderFactory.order(
                    createdAt = LocalDate.of(2019, Month.JUNE, 20).atStartOfDay().toInstant(ZoneOffset.UTC),
                    items = listOf(
                        OrderFactory.orderItem(
                            price = Price(BigDecimal.TEN, Currency.getInstance("USD")),
                            license = OrderItemLicense(Duration.Time(5, ChronoUnit.YEARS), "WW"),
                            video = TestFactories.video(
                                title = "Carbon Dioxide and Climate Change",
                                videoServiceId = "5c54d5e8d8eafeecae1ff471",
                                videoReference = "INT_LUMPTL_333_006",
                                contentPartner = TestFactories.contentPartner(
                                    name = "1 Minute in a Museum"
                                )
                            )
                        ),
                        OrderFactory.orderItem(
                            price = Price(BigDecimal.ONE, Currency.getInstance("USD")),
                            license = OrderItemLicense(Duration.Time(100, ChronoUnit.YEARS), "UK"),
                            video = TestFactories.video(
                                title = "Dispersal of the Tribes, The",
                                videoServiceId = "5c54d5f4d8eafeecae1ffba5",
                                videoReference = "INT_UN_28K_004",
                                contentPartner = TestFactories.contentPartner(
                                    name = "1 Minute in a Museum"
                                )
                            )
                        )
                    )
                ),
                OrderFactory.order(
                    createdAt = LocalDate.of(2019, Month.APRIL, 1).atStartOfDay().toInstant(ZoneOffset.UTC),
                    items = listOf(
                        OrderFactory.orderItem(
                            price = Price(BigDecimal.ZERO, Currency.getInstance("GBP")),
                            license = OrderItemLicense(Duration.Time(10, ChronoUnit.YEARS), "WW"),
                            video = TestFactories.video(
                                title = "Connecting Despite the Loss of Sight or Hearing",
                                videoServiceId = "5c54d5efd8eafeecae1ff874",
                                videoReference = "INT_IO_08K_011",
                                contentPartner = TestFactories.contentPartner(
                                    name = "A content partner"
                                )
                            )
                        )
                    )
                )
            )
        )
        val expectedCSV =
            """ |Content Partner,Order date,boclips ID,Source ID,Title,License Duration,Territory,"Sales Amount (Original Currency)"
                |1 Minute in a Museum,2019-06-20,5c54d5e8d8eafeecae1ff471,INT_LUMPTL_333_006,Carbon Dioxide and Climate Change,5,WW,USD 10
                |1 Minute in a Museum,2019-06-20,5c54d5f4d8eafeecae1ffba5,INT_UN_28K_004,"Dispersal of the Tribes, The",100,UK,USD 1
                |A content partner,2019-04-01,5c54d5efd8eafeecae1ff874,INT_IO_08K_011,Connecting Despite the Loss of Sight or Hearing,10,WW,GBP 0""".trimMargin()

        val csvResource: Resource = ExportAllOrdersToCsv(ordersRepository)()

        assertThat(csvResource.parseCsv()).isEqualTo(expectedCSV.parseCsv())
    }
}

private fun Resource.parseCsv() = inputStream.readBytes().toString(Charset.defaultCharset()).parseCsv()

private fun String.parseCsv(): Iterable<Map<String, Any>> = CsvMapper().let { mapper ->
    mapper.schema().withHeader().let { schema ->
        mapper.readerFor(Map::class.java)
            .with(schema)
            .readValues<Map<String, Any>>(this)
            .readAll()
    }
}

