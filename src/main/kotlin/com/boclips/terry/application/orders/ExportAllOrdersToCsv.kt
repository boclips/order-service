package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.service.OrderService
import com.boclips.terry.presentation.exceptions.FailedExportException
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component

@Component
class ExportAllOrdersToCsv(val orderService: OrderService) {

    operator fun invoke() =
        try {
            orderService.exportManifest()
                .let { it.items }
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

