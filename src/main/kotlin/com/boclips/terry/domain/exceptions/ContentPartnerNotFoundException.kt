package com.boclips.terry.domain.exceptions

import com.boclips.terry.domain.model.orderItem.ContentPartnerId

class ContentPartnerNotFoundException(contentPartnerId: ContentPartnerId) :
    BoclipsException("Could not find content partner with ID=${contentPartnerId.value}")
