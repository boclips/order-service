package com.boclips.orders.domain.model.orderItem

import com.boclips.orders.domain.model.Price

data class OrderItem(
    val id: String,
    val price: Price,
    val transcriptRequested: Boolean,
    val captionsRequested: Boolean,
    val editingRequested: String?,
    val trim: TrimRequest,
    val video: Video,
    val license: OrderItemLicense?,
    val notes: String?
) {
    companion object {
        fun builder() = Builder()
    }

    val status: OrderItemStatus
        get() {
            if (incompleteReasons.isNotEmpty()) {
                return OrderItemStatus.INCOMPLETED
            }

            if (video.captionStatus == AssetStatus.REQUESTED ||
                video.captionStatus == AssetStatus.PROCESSING
            ) {
                return OrderItemStatus.IN_PROGRESS
            }

            return OrderItemStatus.READY
        }

    val incompleteReasons: List<IncompleteReason>
        get() {
            return IncompleteReason.values().filter { it.check(this) }
        }

    class Builder {
        private lateinit var id: String
        private lateinit var price: Price
        private var captionsRequested: Boolean = false
        private var transcriptRequested: Boolean = false
        private var editingRequested: String? = null
        private lateinit var trim: TrimRequest
        private lateinit var video: Video
        private var license: OrderItemLicense? = null
        private var notes: String? = null

        fun price(price: Price) = apply { this.price = price }
        fun captionsRequested(captionsRequested: Boolean) = apply { this.captionsRequested = captionsRequested }
        fun transcriptRequested(transcriptRequested: Boolean) = apply { this.transcriptRequested = transcriptRequested }
        fun editingRequested(editingRequested: String?) = apply { this.editingRequested = editingRequested }
        fun trim(trim: TrimRequest) = apply { this.trim = trim }
        fun video(video: Video) = apply { this.video = video }
        fun license(license: OrderItemLicense) = apply { this.license = license }
        fun notes(notes: String?) = apply { this.notes = notes }
        fun id(id: String) = apply { this.id = id }

        fun build() = OrderItem(
            price = price,
            captionsRequested = captionsRequested,
            transcriptRequested = transcriptRequested,
            editingRequested = editingRequested,
            trim = trim,
            video = video,
            license = license,
            notes = notes,
            id = id
        )
    }
}
