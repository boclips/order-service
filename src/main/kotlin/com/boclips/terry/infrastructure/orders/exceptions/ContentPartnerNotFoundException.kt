package com.boclips.terry.infrastructure.orders.exceptions

import com.boclips.terry.domain.model.orderItem.ContentPartnerId

class ContentPartnerNotFoundException(contentPartnerId: ContentPartnerId) :
    UserFacingException("Could not find content partner: $contentPartnerId")
