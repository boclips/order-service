package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.Order
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.Date

@JsonPropertyOrder(
    ManifestCsvMetadata.CONTENT_PARTNER,
    ManifestCsvMetadata.ORDER_DATE,
    ManifestCsvMetadata.BOCLIPS_ID,
    ManifestCsvMetadata.SOURCE_ID,
    ManifestCsvMetadata.TITLE,
    ManifestCsvMetadata.LICENSE_DURATION,
    ManifestCsvMetadata.TERRITORY,
    ManifestCsvMetadata.SALES_AMOUNT
)
data class ManifestCsvMetadata(
    @get:JsonProperty(CONTENT_PARTNER)
    val contentPartner: String,
    @get:JsonProperty(ORDER_DATE)
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val orderDate: Date,
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
    val salesAmount: String
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

        fun from(order: Order) : List<ManifestCsvMetadata> = order.items.map { orderItem ->
            ManifestCsvMetadata(
                contentPartner = orderItem.video.contentPartner.name,
                orderDate = Date.from(order.createdAt),
                boclipsId = orderItem.video.videoServiceId.value,
                sourceId = orderItem.video.contentPartnerVideoId,
                title = orderItem.video.title,
                licenseDuration = orderItem.license.duration.toReadableString(),
                territory = orderItem.license.territory,
                salesAmount = orderItem.price.toReadableString()
            )
        }
    }
}
