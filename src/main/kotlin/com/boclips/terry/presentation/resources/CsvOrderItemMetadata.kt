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
    @JsonProperty(value = ORDER_NO)
    var legacyOrderId: String? = null

    @JsonProperty(value = MONTH_DATE)
    var month: String? = null

    @JsonProperty(value = ORDER_REQUEST_DATE)
    var requestDate: String? = null

    @JsonProperty(value = ORDER_FULFILLMENT_DATE)
    var fulfilmentDate: String? = null

    @JsonProperty(value = QUARTER)
    var quarter: String? = null

    @JsonProperty(value = MEMBER_REQUEST)
    var memberRequest: String? = null

    @JsonProperty(value = MEMBER_AUTHORISE)
    var memberAuthorise: String? = null

    @JsonProperty(value = CLIP_ID)
    var videoId: String? = null

    @JsonProperty(value = TITLE)
    var title: String? = null

    @JsonProperty(value = SOURCE)
    var source: String? = null

    @JsonProperty(value = SOURCE_CODE)
    var sourceCode: String? = null

    @JsonProperty(value = LICENSE_DURATION)
    var licenseDuration: String? = null

    @JsonProperty(value = TERRITORY)
    var territory: String? = null

    @JsonProperty(value = TYPE)
    var type: String? = null

    @JsonProperty(value = PRICE)
    var price: String? = null

    @JsonProperty(value = PUBLISHER)
    var publisher: String? = null

    @JsonProperty(value = ISBN_PRODUCT_NUMBER)
    var isbnProductNumber: String? = null

    @JsonProperty(value = LANGUAGE)
    var language: String? = null

    @JsonProperty(value = CAPTIONING)
    var captioning: String? = null

    @JsonProperty(value = TRIM)
    var trim: String? = null

    @JsonProperty(value = NOTES)
    var notes: String? = null

    @JsonProperty(value = REMITTANCE_NOTES)
    var remittanceNotes: String? = null

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
