package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidExportRequest
import com.boclips.orders.application.orders.converters.FxRateRequestConverter
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.presentation.exceptions.FailedExportException
import com.boclips.orders.presentation.orders.PoundFxRateRequest
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ExportAllOrdersToCsv(val orderService: OrderService) {

    operator fun invoke(eur: BigDecimal?, usd: BigDecimal?, aud: BigDecimal?, sgd: BigDecimal?, cad: BigDecimal?) =
        try {
            val fxRatesAgainstPound = FxRateRequestConverter.convert(
                PoundFxRateRequest(
                    eur = eur,
                    usd = usd,
                    aud = aud,
                    sgd = sgd,
                    cad = cad
                )
            )

            orderService.exportManifest(fxRatesAgainstPound).items
                .map { ManifestCsvMetadata.from(it) }
                .toCsv()
                .let { ByteArrayResource(it) }
        } catch (e: IllegalOrderStateExport) {
            throw FailedExportException(
                error = "Invalid Order State",
                message = "Order ${e.order.id.value}: The order isn't complete and cannot be exported"
            )
        }
}

private fun List<ManifestCsvMetadata>.toCsv() =
    CsvMapper().let<CsvMapper, ByteArray?> { mapper ->
        mapper.schemaFor(ManifestCsvMetadata::class.java).withHeader().let { schema ->
            mapper.writer(schema)
                .writeValueAsBytes(this)
        }
    }!!

