package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrdersRepository
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component

@Component
class ExportAllOrdersToCsv(val ordersRepository: OrdersRepository) {

    operator fun invoke() =
        ordersRepository.findAll()
            .flatMap { ManifestCsvMetadata.from(it) }
            .toCsv()
            .let { ByteArrayResource(it) }
}

private fun List<ManifestCsvMetadata>.toCsv() =
    CsvMapper().let<CsvMapper, ByteArray?> { mapper ->
        mapper.schemaFor(ManifestCsvMetadata::class.java).withHeader().let { schema ->
            mapper.writer(schema)
                .writeValueAsBytes(this)
        }
    }!!

