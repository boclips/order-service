package com.boclips.orders.presentation

import com.boclips.kalturaclient.KalturaClient
import com.boclips.orders.application.VideoRetrieval
import com.boclips.orders.application.VideoTagging
import com.boclips.orders.infrastructure.outgoing.slack.PostFailure
import com.boclips.orders.infrastructure.outgoing.slack.PostSuccess
import com.boclips.orders.infrastructure.outgoing.slack.SlackMessage
import com.boclips.orders.infrastructure.outgoing.slack.SlackPoster
import com.boclips.orders.infrastructure.outgoing.videos.VideoService
import org.springframework.scheduling.annotation.Async

open class SlackControllerJobs(
    private val slackPoster: SlackPoster,
    private val videoService: VideoService,
    private val kalturaClient: KalturaClient
) {
    @Async
    open fun getVideo(action: VideoRetrieval) {
        action
            .onComplete(videoService.get(action.videoId))
            .apply { chat(slackMessage, "https://slack.com/api/chat.postMessage") }
    }

    @Async
    open fun tagVideo(action: VideoTagging) {
        kalturaClient.tag(action.entryId, listOf(action.tag))
        chat(
            action.onComplete(
                com.boclips.orders.infrastructure.outgoing.transcripts.Success(entryId = action.entryId)
            ).slackMessage,
            action.responseUrl
        )
    }

    @Async
    open fun chat(slackMessage: SlackMessage, url: String): Unit =
        when (slackPoster.chatPostMessage(slackMessage, url = url)) {
            is PostSuccess ->
                SlackController.logger.debug { "Successful post of $slackMessage" }
            is PostFailure ->
                SlackController.logger.error { "Failed post to Slack" }
        }
}
