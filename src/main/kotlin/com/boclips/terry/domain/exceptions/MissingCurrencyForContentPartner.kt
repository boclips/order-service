package com.boclips.terry.domain.exceptions

class MissingCurrencyForContentPartner(contentPartnerName: String) :
    BoclipsException("Content partner '$contentPartnerName' has no currency defined")
