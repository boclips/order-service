package com.boclips.orders.domain.exceptions

class MissingCurrencyForContentPartner(contentPartnerName: String) :
    BoclipsException("Content partner '$contentPartnerName' has no currency defined")
