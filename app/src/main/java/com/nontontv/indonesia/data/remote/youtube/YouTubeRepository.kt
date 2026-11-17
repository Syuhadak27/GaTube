package com.nontontv.indonesia.data.remote.youtube

import android.content.Context
import com.nontontv.indonesia.data.model.AudioOption
import com.nontontv.indonesia.data.model.ChannelDetail
import com.nontontv.indonesia.data.model.CommentItem
import com.nontontv.indonesia.data.model.Kategori
import com.nontontv.indonesia.data.model.PlaylistDetail
import com.nontontv.indonesia.data.model.StreamOption
import com.nontontv.indonesia.data.model.VideoDetail
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.util.FormatUtils
import com.nontontv.indonesia.util.NewPipeDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.comments.CommentsInfo
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.VideoStream

/**
 * Lapisan akses data YouTube memakai NewPipeExtractor.
 *
 * Catatan penting: NewPipeExtractor hanya dipakai untuk mengambil metadata dan
 * URL stream langsung. Aplikasi ini tidak menampilkan iklan sama sekali karena
 * iklan YouTube tidak ikut saat kita memutar URL stream langsung.
 */
object YouTubeRepository {

    private var diinisialisasi = false

    /** Dipanggil sekali saat aplikasi mulai. */
    fun init(context: Context) {
        if (diinisialisasi) return
        NewPipe.init(
            NewPipeDownloader,
            Localization("id", "ID"),
            ContentCountry("ID")
        )
        diinisialisasi = true
    }

    private val service get() = ServiceList.YouTube

    private fun urlVideo(id: String): String = "https://www.youtube.com/watch?v=$id"

    /** Memilih thumbnail dengan resolusi terbesar. */
    private fun gambarTerbaik(gambar: List<Image>): String {
        return gambar.maxByOrNull {
            it.width.coerceAtLeast(0) * it.height.coerceAtLeast(0)
        }?.url.orEmpty()
    }

    private fun StreamInfoItem.toVideoItem(): VideoItem {
        return VideoItem(
            id = FormatUtils.ekstrakIdVideo(url).orEmpty(),
            title = name.orEmpty(),
            thumbnailUrl = gambarTerbaik(thumbnails),
            durationSeconds = duration,
            viewCount = viewCount,
            uploaderName = uploaderName.orEmpty(),
            uploaderUrl = uploaderUrl.orEmpty(),
            uploaderAvatarUrl = gambarTerbaik(uploaderAvatars),
            uploadDate = textualUploadDate.orEmpty(),
            isLive = streamType == org.schabi.newpipe.extractor.stream.StreamType.LIVE_STREAM,
            isShort = isShortFormContent
        )
    }

    private fun InfoItem.toVideoItem(): VideoItem? {
        return if (this is StreamInfoItem) toVideoItem() else null
    }

    /** Daftar kiosk/kategori yang tersedia (Trending, Musik, Game, dll). */
    fun daftarKategori(): List<Kategori> {
        val kiosk = service.kioskList
        val namaNyata = mapOf(
            "Trending" to "Trending",
            "trending_music" to "Musik",
            "trending_gaming" to "Game",
            "trending_movies_and_shows" to "Film & Acara",
            "trending_podcasts_episodes" to "Podcast",
            "live" to "Siaran Langsung"
        )
        return kiosk.availableKiosks.map { Kategori(it, namaNyata[it] ?: it) }
    }

    /** Video trending bawaan. */
    suspend fun trending(): List<VideoItem> = withContext(Dispatchers.IO) {
        val extractor = service.kioskList.getDefaultKioskExtractor()
        extractor.getInitialPage().items.mapNotNull { it.toVideoItem() }
    }

    /** Video berdasarkan kategori/kiosk. */
    suspend fun videoKategori(idKategori: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val extractor = service.kioskList.getExtractorById(idKategori, null)
        extractor.getInitialPage().items.mapNotNull { it.toVideoItem() }
    }

    /** Pencarian video. */
    suspend fun cari(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val extractor = service.getSearchExtractor(query, listOf("videos"), "ID")
        SearchInfo.getInfo(extractor).relatedItems.mapNotNull { it.toVideoItem() }
    }

    /** Saran pencarian dari YouTube. */
    suspend fun saran(query: String): List<String> = withContext(Dispatchers.IO) {
        runCatching { service.suggestionExtractor.suggestionList(query) }
            .getOrDefault(emptyList())
    }

    /** Detail video + pilihan kualitas + video terkait. */
    suspend fun detail(id: String): VideoDetail = withContext(Dispatchers.IO) {
        val info = StreamInfo.getInfo(service, urlVideo(id))

        val videoStreams = (info.videoStreams + info.videoOnlyStreams).map { stream: VideoStream ->
            StreamOption(
                url = stream.url.orEmpty(),
                quality = stream.resolution.ifBlank { "${stream.height}p" },
                height = stream.height,
                fps = stream.fps,
                isVideoOnly = stream.isVideoOnly,
                mimeType = stream.format?.mimeType.orEmpty()
            )
        }.distinctBy { it.url }

        val audioStreams = info.audioStreams.map { stream ->
            AudioOption(
                url = stream.url.orEmpty(),
                bitrate = stream.averageBitrate,
                mimeType = stream.format?.mimeType.orEmpty()
            )
        }.distinctBy { it.url }

        VideoDetail(
            id = id,
            title = info.name.orEmpty(),
            description = info.description?.content.orEmpty(),
            thumbnailUrl = gambarTerbaik(info.thumbnails),
            durationSeconds = info.duration,
            viewCount = info.viewCount,
            likeCount = info.likeCount,
            uploaderName = info.uploaderName.orEmpty(),
            uploaderUrl = info.uploaderUrl.orEmpty(),
            uploaderAvatarUrl = gambarTerbaik(info.uploaderAvatars),
            uploaderSubscriberCount = info.uploaderSubscriberCount,
            uploadDate = info.textualUploadDate.orEmpty(),
            videoStreams = videoStreams,
            audioStreams = audioStreams,
            related = info.relatedItems.mapNotNull { it.toVideoItem() }
        )
    }

    /** Komentar video. */
    suspend fun komentar(id: String): List<CommentItem> = withContext(Dispatchers.IO) {
        val info = CommentsInfo.getInfo(service, urlVideo(id))
        info.relatedItems.map { item ->
            CommentItem(
                id = item.commentId.orEmpty(),
                author = item.uploaderName.orEmpty(),
                authorAvatarUrl = gambarTerbaik(item.uploaderAvatars),
                text = item.commentText?.content.orEmpty(),
                likeCount = item.likeCount,
                publishedTime = item.textualUploadDate.orEmpty(),
                isHearted = item.isHeartedByUploader,
                isPinned = item.isPinned,
                replyCount = item.replyCount
            )
        }
    }

    /** Detail kanal + daftar video yang diunggah. */
    suspend fun kanal(url: String): ChannelDetail = withContext(Dispatchers.IO) {
        val info = ChannelInfo.getInfo(service, url)
        val tab = info.tabs.firstOrNull { tab ->
            tab.contentFilters.any { it.equals("videos", ignoreCase = true) }
        } ?: info.tabs.firstOrNull()

        val videos = if (tab != null) {
            ChannelTabInfo.getInfo(service, tab).relatedItems.mapNotNull { it.toVideoItem() }
        } else {
            emptyList()
        }

        ChannelDetail(
            id = info.id.orEmpty(),
            name = info.name.orEmpty(),
            avatarUrl = gambarTerbaik(info.avatars),
            bannerUrl = gambarTerbaik(info.banners),
            subscriberCount = info.subscriberCount,
            description = info.description.orEmpty(),
            videoCount = videos.size.toLong(),
            videos = videos
        )
    }

    /** Detail playlist YouTube. */
    suspend fun playlist(url: String): PlaylistDetail = withContext(Dispatchers.IO) {
        val info = PlaylistInfo.getInfo(service, url)
        PlaylistDetail(
            id = info.id.orEmpty(),
            name = info.name.orEmpty(),
            thumbnailUrl = gambarTerbaik(info.thumbnails),
            uploaderName = info.uploaderName.orEmpty(),
            uploaderUrl = info.uploaderUrl.orEmpty(),
            videoCount = info.streamCount,
            videos = info.relatedItems.mapNotNull { it.toVideoItem() }
        )
    }
}
