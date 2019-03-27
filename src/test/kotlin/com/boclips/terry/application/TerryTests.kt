package com.boclips.terry.application

import com.boclips.terry.infrastructure.incoming.AppMention
import com.boclips.terry.infrastructure.incoming.EventNotification
import com.boclips.terry.infrastructure.incoming.VerificationRequest
import com.boclips.terry.infrastructure.outgoing.slack.Attachment
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import com.boclips.terry.infrastructure.outgoing.videos.FoundYouTubeVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import io.kotlintest.properties.assertAll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.util.Date

class TerryTests {
    private val irrelevant: String = "irrelevant"

    @Test
    fun `verifies Slack`() {
        assertThat(
            Terry().receiveSlack(
                request = VerificationRequest(
                    challenge = "bet-you-cant-copy-paste-this-m8",
                    type = irrelevant
                )
            )
        ).isEqualTo(
            Decision(
                action = VerificationResponse(
                    challenge = "bet-you-cant-copy-paste-this-m8"
                ),
                log = "Responding to verification challenge"
            )
        )
    }

    @Test
    fun `responds to Slack enquiry about his job description`() {
        assertThat(mentionTerry("hi Tezza", userId = "UBS7V80PR"))
            .isEqualTo(
                Decision(
                    action = ChatReply(
                        slackMessage = SlackMessage(
                            channel = "#engineering",
                            text = "<@UBS7V80PR> I don't do much yet"
                        )
                    ),
                    log = """Responding via chat with "<@UBS7V80PR> I don't do much yet""""
                )
            )
    }

    @Test
    fun `retrieves video details when given an ID`() {
        assertAll { videoId: Long ->
            val decision = mentionTerry("I would like video $videoId")
            assertThat(decision.log).isEqualTo("Retrieving video ID $videoId")
            when (val response = decision.action) {
                is VideoRetrieval -> {
                    assertThat(response.videoId).isEqualTo("$videoId")
                }
                else ->
                    fail<String>("Expected a video retrieval, but got $response")
            }
        }
    }

    @Test
    fun `successful receipt of Kaltura video triggers a chat message with the Kaltura details`() {
        when (val action = mentionTerry("Yo can I get video myvid123 please?", userId = "THAD123").action) {
            is VideoRetrieval ->
                assertThat(
                    action.onComplete(
                        FoundKalturaVideo(
                            videoId = "abcdefg",
                            title = "Boclips 4evah",
                            description = "boclips is...interesting",
                            thumbnailUrl = "validurl",
                            playbackId = "1234",
                            streamUrl = "https://cdnapisec.kaltura.com/p/1776261/sp/177626100/playManifest/entryId/1_y0g6ftvy/format/applehttp/protocol/https/video.mp4"
                        )
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "<@THAD123> Here are the video details for myvid123:",
                                attachments = listOf(
                                    Attachment(
                                        imageUrl = "validurl",
                                        title = "Boclips 4evah",
                                        videoId = "abcdefg",
                                        type = "Kaltura",
                                        playbackId = "1234"
                                    )
                                )
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $action")
        }
    }

    @Test
    fun `successful receipt of YouTube video triggers a chat message with the YouTube details`() {
        when (val response = mentionTerry("Yo can I get video myvid123 please?", userId = "THAD123").action) {
            is VideoRetrieval ->
                assertThat(
                    response.onComplete(
                        FoundYouTubeVideo(
                            videoId = "abcdefg",
                            title = "Boclips 4evah",
                            description = "boclips is...interesting",
                            thumbnailUrl = "validurl",
                            playbackId = "1234"
                        )
                    )
                )
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = "<@THAD123> Here are the video details for myvid123:",
                                attachments = listOf(
                                    Attachment(
                                        imageUrl = "validurl",
                                        title = "Boclips 4evah",
                                        videoId = "abcdefg",
                                        type = "YouTube",
                                        playbackId = "1234"
                                    )
                                )
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    @Test
    fun `missing video triggers a chat message with an apology`() {
        when (val response = mentionTerry("video myvid123 doesn't even exist, m8", userId = "THAD123").action) {
            is VideoRetrieval ->
                assertThat(response.onComplete(MissingVideo(videoId = "myvid123")))
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = """<@THAD123> Sorry, video myvid123 doesn't seem to exist! :("""
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    @Test
    fun `server error triggers a chat message with some blame`() {
        when (val response = mentionTerry("please find video thatbreaksvideoservice", userId = "THAD123").action) {
            is VideoRetrieval ->
                assertThat(response.onComplete(Error(message = "500 REALLY BAD")))
                    .isEqualTo(
                        ChatReply(
                            slackMessage = SlackMessage(
                                channel = "#engineering",
                                text = """<@THAD123> looks like the video service is broken :("""
                            )
                        )
                    )
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    private fun mentionTerry(message: String, userId: String = "DEFAULTUSERID"): Decision =
        Terry().receiveSlack(
            EventNotification(
                teamId = irrelevant,
                apiAppId = irrelevant,
                event = AppMention(
                    type = irrelevant,
                    channel = "#engineering",
                    text = "<@TERRYID> $message",
                    eventTs = irrelevant,
                    ts = irrelevant,
                    user = userId
                ),
                type = irrelevant,
                authedUsers = emptyList(),
                eventId = irrelevant,
                eventTime = Date()
            )
        )
}
