package com.boclips.orders.domain.model.orderItem

enum class IncompleteReason {

    CAPTIONS_UNAVAILABLE {
        override fun check(orderItem: OrderItem): Boolean {
            return orderItem.video.captionStatus == AssetStatus.UNAVAILABLE && orderItem.transcriptRequested
        }
    },
    PRICE_UNAVAILABLE {
        override fun check(orderItem: OrderItem): Boolean {
            return orderItem.price.currency == null || orderItem.price.amount == null
        }
    },
    LICENSE_UNAVAILABLE {
        override fun check(orderItem: OrderItem): Boolean {
            return orderItem.license?.duration == null || orderItem.license.territory == null
        }
    };

    abstract fun check(orderItem: OrderItem): Boolean
}
