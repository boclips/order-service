package com.boclips.terry.domain.model.orderItem

import com.boclips.terry.domain.model.Price

data class OrderItem(
    val price: Price,
    val transcriptRequested: Boolean,
    val trim: TrimRequest,
    val video: Video,
    val license: OrderItemLicense,
    val notes: String?
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private lateinit var price: Price
        private var transcriptRequested: Boolean = false
        private lateinit var trim: TrimRequest
        private lateinit var video: Video
        private lateinit var license: OrderItemLicense
        private var notes: String? = null

        fun price(price: Price) = apply { this.price = price }
        fun transcriptRequested(transcriptRequested: Boolean) = apply { this.transcriptRequested = transcriptRequested }
        fun trim(trim: TrimRequest) = apply { this.trim = trim }
        fun video(video: Video) = apply { this.video = video }
        fun license(license: OrderItemLicense) = apply { this.license = license }
        fun notes(notes: String?) = apply { this.notes = notes }

        fun build() = OrderItem(
            price = price,
            transcriptRequested = transcriptRequested,
            trim = trim,
            video = video,
            license = license,
            notes = notes
        )
    }
}
