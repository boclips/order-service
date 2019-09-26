package com.boclips.terry.presentation.resources

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser

object OrderCsvUploadConverter {
    fun convertToMetadata(orderCsv: ByteArray): List<CsvOrderItemMetadata> {
        CsvMapper().apply {
            this.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE)
            val schema = this.schemaFor(CsvOrderItemMetadata::class.java)
                .withHeader()
                .withColumnReordering(true)
                .withNullValue("")
            return this.readerFor(CsvOrderItemMetadata::class.java)
                .with(schema)
                .readValues<CsvOrderItemMetadata>(orderCsv)
                .readAll()
        }
    }
}
