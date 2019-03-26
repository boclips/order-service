package com.boclips.terry.infrastructure.outgoing.videos

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class FakeVideoServiceTests : VideoServiceTests() {
    @Before
    fun setUp() {
        videoServiceForKaltura = FakeVideoService()
                .respondWith(FoundKalturaVideo(
                        videoId = "5c54d8cad8eafeecae2179af",
                        title = "Tesco opens new discount supermarket 'Jack's'",
                        description = expectedKalturaDescription,
                        thumbnailUrl = "https://cdnapisec.kaltura.com/p/1776261/thumbnail/entry_id/1_y0g6ftvy/height/250/vid_slices/3/vid_slice/2",
                        playbackId = "5472c7f2-f1e5-4e6f-8823-dce8bcc82cab"
                ))
        videoServiceForYouTube = FakeVideoService()
                .respondWith(FoundYouTubeVideo(
                        videoId = "5c670be1350a1b0001a75e23",
                        title = "8 Ways to say Hello and Goodbye | Super Easy Catalan 1",
                        description = expectedYouTubeDescription,
                        thumbnailUrl = "https://i.ytimg.com/vi/RLa7mFtZjMw/hqdefault.jpg",
                        playbackId = "RLa7mFtZjMw"
                ))
        missingVideoService = FakeVideoService()
                .respondWith(MissingVideo(videoId = "987654321"))
    }
}

class HTTPVideoServiceTests : VideoServiceTests() {
    @Before
    fun setUp() {
        videoServiceForKaltura = HTTPVideoService("https://api.boclips.com/v1/videos")
        videoServiceForYouTube = HTTPVideoService("https://api.boclips.com/v1/videos")
        missingVideoService = HTTPVideoService("https://httpbin.org/status/404")
    }
}

abstract class VideoServiceTests {
    var videoServiceForKaltura: VideoService? = null
    var videoServiceForYouTube: VideoService? = null
    var missingVideoService: VideoService? = null

    val expectedKalturaDescription = "VOICED: Tesco has launched its own discount store today, in a bid to take on the thriving German supermarkets - Lidl and Aldi.The budget chain is called Jack's in tribute to Tesco's founder Jack Cohen.15 of the stores will open over the next year.\nInterviews with Dave Lewis, Tesco Chief Executive and Steve Dresser, Retail Analyst\nShows: New Jacks store and some products in the store on the 19th September 2018 in Chatteris, United Kingdom."
    val expectedYouTubeDescription = "SUBSCRIBE TO EASY LANGUAGES: http://goo.gl/sdP9nz\nFACEBOOK: https://web.facebook.com/EasyCatalan/\nhttp://www.facebook.com/easylanguagesstreetinterviews\nBECOME A CO-PRODUCER: https://bit.ly/2kyB9nM\n\n---\n\nEasy Languages is an international video project aiming at supporting people worldwide to learn languages through authentic street interviews and expose the street culture of participating partner countries abroad. Episodes are produced in local languages and contain subtitles in both the original language as well as in English.\nhttp://www.easy-languages.org/\n\n---\n\nAprèn català amb Easy Catalan! En el nostre primer Super Easy us expliquem les salutacions i els comiats. Sabeu com heu de saludar, quan us trobeu algú pel carrer?\nNota: Si els entrevistats fan errors, seran corregits en els subtítols entre parèntesis.\n\n\nLearn Catalan with Easy Catalan! In our first Super Easy we explain greetings and farewells. Do you know how to say hello when you meet someone in the street?\nNote: If the interviewees make errors, we will correct them in the subtitles using parenthesis.\n\nSalutacions:\nGreetings:\nHola / Hello - 00:10\nBon dia / Good morning [also used throughout the day] - 00:19\nBon dia i bona hora / [a longer and less common variant for 'Bon dia', but with the same meaning] - 00:26\nBona tarda / Good afternoon - 00:32\nBones, Ep, Ei / Hey! - 00:37\nDéu vos guard / God bless you [literally, 'May God watch over you'] - 00:45\n\n\nComiats:\nFarewells:\nAdeu / Bye - 01:23\nAdeu-siau / Goodbye - 01:33\nFins aviat, fins demà, fins després, fins dimarts, fins més tard... /\nSee you soon, see you tomorrow, see you later, see you Tuesday, see you later... - 01:39\nBona nit / Goodnight/night - 01:51\nSalut, que vagi bé, passiu-ho bé, arreveure /\nSee you, all the best, have fun/enjoy, see you - 02:05\n\n\nHost/interviewer: Gemma, Roger and Sílvia\nCamera and editing: Joan and Sílvia\nSubtitles: Andreu, Laia and Sophie"

    @Test
    fun `retrieves a Kaltura video that exists`() {
        assertThat(videoServiceForKaltura!!.get("2584078"))
                .isEqualTo(FoundKalturaVideo(
                        videoId = "5c54d8cad8eafeecae2179af",
                        title = "Tesco opens new discount supermarket 'Jack's'",
                        description = expectedKalturaDescription,
                        thumbnailUrl = "https://cdnapisec.kaltura.com/p/1776261/thumbnail/entry_id/1_y0g6ftvy/height/250/vid_slices/3/vid_slice/2",
                        playbackId = "5472c7f2-f1e5-4e6f-8823-dce8bcc82cab"
                ))
    }

    @Test
    fun `retrieves a YouTube video that exists`() {
        assertThat(videoServiceForYouTube!!.get("5c670be1350a1b0001a75e23"))
                .isEqualTo(FoundYouTubeVideo(
                        videoId = "5c670be1350a1b0001a75e23",
                        title = "8 Ways to say Hello and Goodbye | Super Easy Catalan 1",
                        description = expectedYouTubeDescription,
                        thumbnailUrl = "https://i.ytimg.com/vi/RLa7mFtZjMw/hqdefault.jpg",
                        playbackId = "RLa7mFtZjMw"
                ))
    }

    @Test
    fun `returns missing video when video doesn't exist`() {
        assertThat(missingVideoService!!.get("987654321"))
                .isEqualTo(MissingVideo(videoId = "987654321"))
    }
}