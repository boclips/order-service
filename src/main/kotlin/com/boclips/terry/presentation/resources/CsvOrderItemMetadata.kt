package com.boclips.terry.presentation.resources

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.Date

@JsonPropertyOrder(
    CsvOrderItemMetadata.ORDER_NO,
    CsvOrderItemMetadata.MONTH_DATE,
    CsvOrderItemMetadata.ORDER_REQUEST_DATE,
    CsvOrderItemMetadata.ORDER_FULFILLMENT_DATE,
    CsvOrderItemMetadata.QUARTER,
    CsvOrderItemMetadata.MEMBER_REQUEST,
    CsvOrderItemMetadata.MEMBER_AUTHORISE,
    CsvOrderItemMetadata.CLIP_ID,
    CsvOrderItemMetadata.TITLE,
    CsvOrderItemMetadata.SOURCE,
    CsvOrderItemMetadata.SOURCE_CODE,
    CsvOrderItemMetadata.LICENSE_DURATION,
    CsvOrderItemMetadata.TERRITORY,
    CsvOrderItemMetadata.TYPE,
    CsvOrderItemMetadata.PRICE,
    CsvOrderItemMetadata.PUBLISHER,
    CsvOrderItemMetadata.ISBN_PRODUCT_NUMBER,
    CsvOrderItemMetadata.LANGUAGE,
    CsvOrderItemMetadata.CAPTIONING,
    CsvOrderItemMetadata.TRIM,
    CsvOrderItemMetadata.NOTES,
    CsvOrderItemMetadata.REMITTANCE_NOTES
)

class CsvOrderItemMetadata {
    @JsonProperty(value = Companion.ORDER_NO)
    var legacyOrderId: String = ""

    @JsonProperty(value = Companion.MONTH_DATE)
    var month: String = ""

    @JsonProperty(value = Companion.ORDER_REQUEST_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "en_GB")
    var requestDate: Date? = null

    @JsonProperty(value = Companion.ORDER_FULFILLMENT_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", locale = "en_GB")
    var fulfilmentDate: Date? = null

    @JsonProperty(value = Companion.QUARTER)
    var quarter: String = ""

    @JsonProperty(value = Companion.MEMBER_REQUEST)
    var memberRequest: String = ""

    @JsonProperty(value = Companion.MEMBER_AUTHORISE)
    var memberAuthorise: String = ""

    @JsonProperty(value = Companion.CLIP_ID)
    var videoId: String = ""

    @JsonProperty(value = Companion.TITLE)
    var title: String = ""

    @JsonProperty(value = Companion.SOURCE)
    var source: String = ""

    @JsonProperty(value = Companion.SOURCE_CODE)
    var sourceCode: String = ""

    @JsonProperty(value = Companion.LICENSE_DURATION)
    var licenseDuration: String = ""

    @JsonProperty(value = Companion.TERRITORY)
    var territory: String = ""

    @JsonProperty(value = Companion.TYPE)
    var type: String = ""

    @JsonProperty(value = Companion.PRICE)
    var price: String = ""

    @JsonProperty(value = Companion.PUBLISHER)
    var publisher: String = ""

    @JsonProperty(value = Companion.ISBN_PRODUCT_NUMBER)
    var isbnProductNumber: String = ""

    @JsonProperty(value = Companion.LANGUAGE)
    var language: String = ""

    @JsonProperty(value = Companion.CAPTIONING)
    var captioning: String = ""

    @JsonProperty(value = Companion.TRIM)
    var trim: String = ""

    @JsonProperty(value = Companion.NOTES)
    var notes: String = ""

    @JsonProperty(value = Companion.REMITTANCE_NOTES)
    var remittanceNotes: String = ""

    companion object {
        const val ORDER_NO = "Order No"
        const val MONTH_DATE = "Month Date"
        const val ORDER_REQUEST_DATE = "Order request Date"
        const val ORDER_FULFILLMENT_DATE = "Order Fulfillment Date"
        const val QUARTER = "Quarter"
        const val MEMBER_REQUEST = "Member (request)"
        const val MEMBER_AUTHORISE = "Member (authorise) ID"
        const val CLIP_ID = "Clip ID"
        const val TITLE = "Title"
        const val SOURCE = "Source"
        const val SOURCE_CODE = "Source Code"
        const val LICENSE_DURATION = "License Duration"
        const val TERRITORY = "Territory"
        const val TYPE = "Type"
        const val PRICE = "Price"
        const val PUBLISHER = "Publisher"
        const val ISBN_PRODUCT_NUMBER = "ISBN / PRODUCT DESCRIP"
        const val LANGUAGE = "Language"
        const val CAPTIONING = "Captioning"
        const val TRIM = "Trim"
        const val NOTES = "Notes"
        const val REMITTANCE_NOTES = "Remittance Notes"
    }
}
