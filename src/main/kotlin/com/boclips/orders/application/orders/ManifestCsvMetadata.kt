package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.ManifestItem
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.math.RoundingMode

@JsonPropertyOrder(
    ManifestCsvMetadata.CONTENT_PARTNER,
    ManifestCsvMetadata.ORDER_DATE,
    ManifestCsvMetadata.BOCLIPS_ID,
    ManifestCsvMetadata.SOURCE_ID,
    ManifestCsvMetadata.TITLE,
    ManifestCsvMetadata.LICENSE_DURATION,
    ManifestCsvMetadata.TERRITORY,
    ManifestCsvMetadata.SALES_AMOUNT,
    ManifestCsvMetadata.FX_RATE,
    ManifestCsvMetadata.LICENSE_CURRENCY,
    ManifestCsvMetadata.LICENSE_SALES_AMOUNT
)
data class ManifestCsvMetadata(
    @get:JsonProperty(CONTENT_PARTNER)
    val contentPartner: String,
    @get:JsonProperty(ORDER_DATE)
    val orderDate: String,
    @get:JsonProperty(BOCLIPS_ID)
    val boclipsId: String,
    @get:JsonProperty(SOURCE_ID)
    val sourceId: String,
    @get:JsonProperty(TITLE)
    val title: String,
    @get:JsonProperty(LICENSE_DURATION)
    val licenseDuration: String,
    @get:JsonProperty(TERRITORY)
    val territory: String,
    @get:JsonProperty(SALES_AMOUNT)
    val salesAmount: String,
    @get:JsonProperty(FX_RATE)
    val fxRate: String,
    @get:JsonProperty(LICENSE_CURRENCY)
    val currency: String,
    @get:JsonProperty(LICENSE_SALES_AMOUNT)
    val licenseSalesAmount: String

) {
    companion object {
        const val CONTENT_PARTNER = "Content Partner"
        const val ORDER_DATE = "Order date"
        const val BOCLIPS_ID = "boclips ID"
        const val SOURCE_ID = "Source ID"
        const val TITLE = "Title"
        const val LICENSE_DURATION = "License Duration"
        const val TERRITORY = "Territory"
        const val SALES_AMOUNT = "Sales Amount (Original Currency)"
        const val FX_RATE = "FX Rate"
        const val LICENSE_CURRENCY = "License Currency"
        const val LICENSE_SALES_AMOUNT = "License Sales Amount"

        fun from(manifestItem: ManifestItem): ManifestCsvMetadata = manifestItem.let {
            ManifestCsvMetadata(
                contentPartner = it.video.channel.name,
                orderDate = manifestItem.orderDate.toString(),
                boclipsId = it.video.videoServiceId.value,
                sourceId = it.video.channelVideoId,
                title = it.video.title,
                licenseDuration = it.license.duration.toReadableString(),
                territory = it.license.territory,
                salesAmount = it.salePrice.toReadableString(),
                fxRate = it.fxRate.setScale(2, RoundingMode.HALF_UP).toString(),
                currency = it.convertedSalesAmount.currency?.currencyCode
                    ?: throw IllegalStateException("Currency cannot be null"),
                licenseSalesAmount = it.convertedSalesAmount.amount.toString()
            )
        }
    }
}
