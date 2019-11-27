package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.orderItem.ContentPartnerId

class ContentPartnerNotFoundException(contentPartnerId: ContentPartnerId) :
    BoclipsException("Could not find content partner with ID=${contentPartnerId.value}")
