package com.boclips.terry.presentation

import com.boclips.terry.application.VideoRetrieval
import com.boclips.terry.infrastructure.outgoing.slack.PostFailure
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster
import com.boclips.terry.infrastructure.outgoing.videos.VideoService
import org.springframework.scheduling.annotation.Async

class HomeControllerJobs(
    private val slackPoster: SlackPoster,
    private val videoService: VideoService
) {
    @Async
    fun getVideo(action: VideoRetrieval) {
        action
            .onComplete(videoService.get(action.videoId))
            .apply { chat(slackMessage) }
    }

    @Async
    fun chat(slackMessage: SlackMessage): Unit =
        when (slackPoster.chatPostMessage(slackMessage)) {
            is PostSuccess ->
                HomeController.logger.debug { "Successful post of $slackMessage" }
            is PostFailure ->
                HomeController.logger.error { "Failed post to Slack" }
        }
}
