package com.boclips.orders.infrastructure.orders.converters

import java.net.URL

class KalturaLinkConverter {
    companion object {
        fun getVideoUploadLink(playbackId: String?) =
            URL("https://kmc.kaltura.com/index.php/kmcng/content/entries/entry/$playbackId/flavours")

        fun getCaptionAdminLink(playbackId: String?) =
            URL("https://kmc.kaltura.com/index.php/kmcng/content/entries/entry/$playbackId/metadata")
    }
}
