package com.boclips.terry

import com.boclips.terry.application.Terry
import com.boclips.terry.infrastructure.incoming.AppMention
import com.boclips.terry.infrastructure.incoming.EventNotification
import com.boclips.terry.infrastructure.incoming.VerificationRequest
import com.boclips.terry.infrastructure.outgoing.*
import com.boclips.terry.infrastructure.outgoing.slack.Attachment
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.Error
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import com.boclips.terry.infrastructure.outgoing.videos.MissingVideo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test
import java.util.*

class TerryTests {
    private val irrelevant: String = "irrelevant"

    @Test
    fun `verifies Slack`() {
        assertThat(Terry().receiveSlack(
                request = VerificationRequest(
                        challenge = "bet-you-cant-copy-paste-this-m8",
                        type = irrelevant
                )
        )).isEqualTo(Decision(
                response = VerificationResponse(
                        challenge = "bet-you-cant-copy-paste-this-m8"
                ),
                log = "Responding to verification challenge"
        ))
    }

    @Test
    fun `responds to Slack enquiry about his job description`() {
        assertThat(mentionTerry("hi Tezza", userId = "UBS7V80PR"))
                .isEqualTo(Decision(
                        response = ChatReply(
                                slackMessage = SlackMessage(
                                        channel = "#engineering",
                                        text = "<@UBS7V80PR> I don't do much yet"
                                )),
                        log = """Responding via chat with "<@UBS7V80PR> I don't do much yet""""
                ))
    }

    @Test
    fun `retrieves video details when given an ID`() {
        val decision = mentionTerry("I would like video 12345678")
        assertThat(decision.log).isEqualTo("Retrieving video ID 12345678")
        when (val response = decision.response) {
            is VideoRetrieval -> {
                assertThat(response.videoId).isEqualTo("12345678")
            }
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    @Test
    fun `successful receipt of video triggers a chat message with the details`() {
        when (val response = mentionTerry("Yo can I get video myvid123 please?", userId = "THAD123").response) {
            is VideoRetrieval ->
                assertThat(response.onComplete(FoundVideo(videoId = "abcdefg", title = "Boclips 4evah", description = "boclips is...interesting", thumbnailUrl = "validurl")))
                        .isEqualTo(ChatReply(
                                slackMessage = SlackMessage(
                                        channel = "#engineering",
                                        text = "<@THAD123> Here's the video details for myvid123:",
                                        attachments = listOf(Attachment(imageUrl = "validurl", title = "Boclips 4evah", videoId = "abcdefg", description = "boclips is...interesting"))
                                )
                        ))
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    @Test
    fun `missing video triggers a chat message with an apology`() {
        when (val response = mentionTerry("video myvid123 doesn't even exist, m8", userId = "THAD123").response) {
            is VideoRetrieval ->
                assertThat(response.onComplete(MissingVideo(videoId = "myvid123")))
                        .isEqualTo(ChatReply(
                                slackMessage = SlackMessage(
                                        channel = "#engineering",
                                        text = """<@THAD123> Sorry, video myvid123 doesn't seem to exist! :("""
                                )
                        ))
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    @Test
    fun `server error triggers a chat message with some blame`() {
        when (val response = mentionTerry("please find video thatbreaksvideoservice", userId = "THAD123").response) {
            is VideoRetrieval ->
                assertThat(response.onComplete(Error(message = "500 REALLY BAD")))
                        .isEqualTo(ChatReply(
                                slackMessage = SlackMessage(
                                        channel = "#engineering",
                                        text = """<@THAD123> looks like the video service is broken :("""
                                )
                        ))
            else ->
                fail<String>("Expected a video retrieval, but got $response")
        }
    }

    private fun mentionTerry(message: String, userId: String = "DEFAULTUSERID"): Decision = Terry().receiveSlack(
            request = EventNotification(
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
