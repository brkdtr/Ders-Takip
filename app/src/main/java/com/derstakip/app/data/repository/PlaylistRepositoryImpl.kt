package com.derstakip.app.data.repository

import com.derstakip.app.BuildConfig
import com.derstakip.app.data.local.dao.PlaylistDao
import com.derstakip.app.data.local.dao.StudySettingsDao
import com.derstakip.app.data.local.dao.VideoDao
import com.derstakip.app.data.local.entity.PlaylistEntity
import com.derstakip.app.data.local.entity.VideoEntity
import com.derstakip.app.data.remote.ParsedPlaylistData
import com.derstakip.app.data.remote.YouTubeApiService
import com.derstakip.app.data.remote.YouTubeDirectParser
import com.derstakip.app.domain.model.Playlist
import com.derstakip.app.domain.model.TopicProgress
import com.derstakip.app.domain.model.Video
import com.derstakip.app.domain.repository.PlaylistRepository
import com.derstakip.app.util.IsoDurationParser
import com.derstakip.app.util.TopicExtractor
import com.derstakip.app.util.YouTubeUrlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val videoDao: VideoDao,
    private val studySettingsDao: StudySettingsDao,
    private val youTubeApiService: YouTubeApiService,
    private val youTubeDirectParser: YouTubeDirectParser
) : PlaylistRepository {

    override suspend fun importPlaylist(urlOrId: String): Result<Playlist> = withContext(Dispatchers.IO) {
        val playlistId = YouTubeUrlParser.extractPlaylistId(urlOrId)
            ?: return@withContext Result.failure(
                IllegalArgumentException("Geçersiz YouTube Oynatma Listesi linki veya ID'si. Lütfen 'https://www.youtube.com/playlist?list=...' biçiminde bir link girin.")
            )

        // Check if the user specifically requested the sample list
        if (playlistId.contains("ornek", ignoreCase = true) || playlistId.equals("sample", ignoreCase = true)) {
            return@withContext importSampleCourse()
        }

        // 1. First Priority: Direct YouTube Parser (No API Key Required!)
        // Fetches real videos, real titles, and real durations directly from YouTube
        val directResult = youTubeDirectParser.fetchPlaylist(playlistId)
        if (directResult.isSuccess) {
            val parsedData = directResult.getOrThrow()
            return@withContext saveParsedPlaylist(parsedData)
        }

        // 2. Second Priority: Official YouTube Data API v3 (if configured with a non-placeholder key)
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        if (!apiKey.contains("PLACEHOLDER") && apiKey.isNotBlank()) {
            val apiResult = fetchFromYouTubeApi(playlistId, apiKey)
            if (apiResult.isSuccess) {
                val playlist = apiResult.getOrThrow()
                studySettingsDao.updateActivePlaylist(playlist.id)
                return@withContext Result.success(playlist)
            }
        }

        // If both failed, return the actual error instead of silently showing sample English lessons
        val originalError = directResult.exceptionOrNull()?.message 
            ?: "Oynatma listesi yüklenemedi. Lütfen listenin 'Herkese Açık' (Public) veya 'Liste Dışı' (Unlisted) olduğundan ve internet bağlantınızdan emin olun."

        Result.failure(IllegalStateException(originalError))
    }

    private suspend fun saveParsedPlaylist(parsedData: ParsedPlaylistData): Result<Playlist> {
        val videoEntities = parsedData.videos.map { pv ->
            VideoEntity(
                id = pv.id,
                playlistId = parsedData.id,
                title = pv.title,
                description = "",
                thumbnailUrl = pv.thumbnailUrl,
                durationSeconds = pv.durationSeconds,
                position = pv.position,
                topic = pv.topic,
                isCompleted = false
            )
        }

        val totalDuration = videoEntities.sumOf { it.durationSeconds }

        val entity = PlaylistEntity(
            id = parsedData.id,
            title = parsedData.title,
            description = "YouTube üzerinden içe aktarıldı.",
            channelTitle = parsedData.channelTitle,
            thumbnailUrl = parsedData.thumbnailUrl,
            itemCount = videoEntities.size,
            totalDurationSeconds = totalDuration,
            isActive = true
        )

        playlistDao.insertPlaylist(entity)
        playlistDao.setActivePlaylist(parsedData.id)
        videoDao.deleteVideosByPlaylistId(parsedData.id)
        videoDao.insertVideos(videoEntities)
        studySettingsDao.updateActivePlaylist(parsedData.id)

        return Result.success(
            Playlist(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                channelTitle = entity.channelTitle,
                thumbnailUrl = entity.thumbnailUrl,
                videoCount = entity.itemCount,
                totalDurationSeconds = entity.totalDurationSeconds,
                isActive = true
            )
        )
    }

    override suspend fun importSampleCourse(): Result<Playlist> = withContext(Dispatchers.IO) {
        val samplePlaylistId = "PL_sample_english_course"
        val courseTitle = "Sıfırdan İleri Seviye İngilizce Eğitim Seti (A1 - B2)"
        val channel = "İngilizce Akademi & Öğretmenler Kulübü"

        val sampleVideosData = listOf(
            Triple("İngilizce Gramer #1 - Present Simple (Geniş Zaman)", 1800L, "Gramer (Grammar)"),
            Triple("İngilizce Gramer #2 - Present Continuous (Şimdiki Zaman)", 2100L, "Gramer (Grammar)"),
            Triple("İngilizce Gramer #3 - Simple Past Tense (Geçmiş Zaman)", 2400L, "Gramer (Grammar)"),
            Triple("İngilizce Gramer #4 - Past Continuous & While/When", 1950L, "Gramer (Grammar)"),
            Triple("İngilizce Gramer #5 - Future Tense (Will vs Be Going To)", 2200L, "Gramer (Grammar)"),
            Triple("Kelime Bilgisi | En Çok Kullanılan 100 Fiil ve Örnek Cümleler", 2700L, "Kelime Bilgisi (Vocabulary)"),
            Triple("Kelime Bilgisi | Günlük Yaşamda 50 Phrasal Verb", 2400L, "Kelime Bilgisi (Vocabulary)"),
            Triple("Kelime Bilgisi | B1 Seviyesi Sıfatlar ve Zıt Anlamlılar", 1800L, "Kelime Bilgisi (Vocabulary)"),
            Triple("Dinleme (Listening) | A2-B1 Seviye Günlük Konuşma Diyalogları", 1500L, "Dinleme (Listening)"),
            Triple("Dinleme (Listening) | İngilizce Podcast: İş ve Sosyal Yaşam", 2100L, "Dinleme (Listening)"),
            Triple("Konuşma & Telaffuz | Doğru Telaffuz Teknikleri ve Vurgular", 1800L, "Konuşma & Telaffuz"),
            Triple("Konuşma & Telaffuz | Akıcı Konuşma Pratikleri ve Kalıplar", 2100L, "Konuşma & Telaffuz"),
            Triple("Okuma & Çeviri | Kısa Hikayelerle İngilizce Okuma Analizi", 2400L, "Okuma & Çeviri (Reading)"),
            Triple("Okuma & Çeviri | Makale Çevirisi ve Cümle Çözümlemesi", 2700L, "Okuma & Çeviri (Reading)"),
            Triple("Sınav Hazırlığı | YDS & YÖKDİL Gramer Soru Çözümü #1", 3000L, "Sınav Hazırlığı"),
            Triple("Sınav Hazırlığı | Cümle Tamamlama ve Paragraf Taktikleri", 2700L, "Sınav Hazırlığı")
        )

        val videoEntities = sampleVideosData.mapIndexed { index, data ->
            VideoEntity(
                id = "${samplePlaylistId}_vid_$index",
                playlistId = samplePlaylistId,
                title = data.first,
                description = "Bu derste ${data.third} konusu kapsamlı ve örneklerle anlatılmaktadır.",
                thumbnailUrl = "https://img.youtube.com/vi/${samplePlaylistId}/hqdefault.jpg",
                durationSeconds = data.second,
                position = index,
                topic = data.third,
                isCompleted = false
            )
        }

        val totalDuration = videoEntities.sumOf { it.durationSeconds }

        val entity = PlaylistEntity(
            id = samplePlaylistId,
            title = courseTitle,
            description = "İngilizce öğretmenleri ve öğrencileri için hazırlanmış video ders takip programı.",
            channelTitle = channel,
            thumbnailUrl = "https://img.youtube.com/vi/${samplePlaylistId}/hqdefault.jpg",
            itemCount = videoEntities.size,
            totalDurationSeconds = totalDuration,
            isActive = true
        )

        playlistDao.insertPlaylist(entity)
        playlistDao.setActivePlaylist(samplePlaylistId)
        videoDao.deleteVideosByPlaylistId(samplePlaylistId)
        videoDao.insertVideos(videoEntities)
        studySettingsDao.updateActivePlaylist(samplePlaylistId)

        Result.success(
            Playlist(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                channelTitle = entity.channelTitle,
                thumbnailUrl = entity.thumbnailUrl,
                videoCount = entity.itemCount,
                totalDurationSeconds = entity.totalDurationSeconds,
                isActive = true
            )
        )
    }

    private suspend fun fetchFromYouTubeApi(playlistId: String, apiKey: String): Result<Playlist> {
        return try {
            val playlistResponse = youTubeApiService.getPlaylistDetails(
                part = "snippet,contentDetails",
                playlistId = playlistId,
                apiKey = apiKey
            )

            val playlistItem = playlistResponse.items.firstOrNull()
                ?: return Result.failure(IllegalStateException("Oynatma listesi bulunamadı."))

            val title = playlistItem.snippet?.title ?: "YouTube Oynatma Listesi"
            val description = playlistItem.snippet?.description ?: ""
            val channelTitle = playlistItem.snippet?.channelTitle ?: "YouTube Kanalı"
            val thumb = playlistItem.snippet?.thumbnails?.mediumThumb?.url
                ?: playlistItem.snippet?.thumbnails?.defaultThumb?.url ?: ""

            // Fetch videos with pagination
            val allVideoEntities = mutableListOf<VideoEntity>()
            var nextPageToken: String? = null
            var currentPosition = 0

            do {
                val itemsResponse = youTubeApiService.getPlaylistItems(
                    part = "snippet,contentDetails",
                    playlistId = playlistId,
                    maxResults = 50,
                    pageToken = nextPageToken,
                    apiKey = apiKey
                )

                val videoIds = itemsResponse.items.mapNotNull { it.snippet?.resourceId?.videoId }.filter { it.isNotBlank() }
                if (videoIds.isNotEmpty()) {
                    val videosDetailResponse = youTubeApiService.getVideoDetails(
                        part = "snippet,contentDetails",
                        videoIds = videoIds.joinToString(","),
                        apiKey = apiKey
                    )

                    val durationMap = videosDetailResponse.items.associate { it.id to it.contentDetails?.duration.orEmpty() }

                    for (item in itemsResponse.items) {
                        val vid = item.snippet?.resourceId?.videoId ?: continue
                        val videoTitle = item.snippet?.title ?: "Ders #${currentPosition + 1}"
                        val isoDuration = durationMap[vid] ?: "PT15M"
                        val durationSec = IsoDurationParser.parseToSeconds(isoDuration).let { if (it <= 0L) 900L else it }
                        val topic = TopicExtractor.extractTopic(videoTitle, title)
                        val vThumb = item.snippet?.thumbnails?.mediumThumb?.url ?: ""

                        allVideoEntities.add(
                            VideoEntity(
                                id = vid,
                                playlistId = playlistId,
                                title = videoTitle,
                                description = item.snippet?.description ?: "",
                                thumbnailUrl = vThumb,
                                durationSeconds = durationSec,
                                position = currentPosition++,
                                topic = topic,
                                isCompleted = false
                            )
                        )
                    }
                }

                nextPageToken = itemsResponse.nextPageToken
            } while (nextPageToken != null && allVideoEntities.size < 200)

            val totalDuration = allVideoEntities.sumOf { it.durationSeconds }

            val entity = PlaylistEntity(
                id = playlistId,
                title = title,
                description = description,
                channelTitle = channelTitle,
                thumbnailUrl = thumb,
                itemCount = allVideoEntities.size,
                totalDurationSeconds = totalDuration,
                isActive = true
            )

            playlistDao.insertPlaylist(entity)
            playlistDao.setActivePlaylist(playlistId)
            videoDao.deleteVideosByPlaylistId(playlistId)
            videoDao.insertVideos(allVideoEntities)

            Result.success(
                Playlist(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    channelTitle = channelTitle,
                    thumbnailUrl = entity.thumbnailUrl,
                    videoCount = entity.itemCount,
                    totalDurationSeconds = entity.totalDurationSeconds,
                    isActive = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { list ->
            list.map {
                Playlist(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    channelTitle = it.channelTitle,
                    thumbnailUrl = it.thumbnailUrl,
                    videoCount = it.itemCount,
                    totalDurationSeconds = it.totalDurationSeconds,
                    isActive = it.isActive
                )
            }
        }
    }

    override fun getPlaylistById(playlistId: String): Flow<Playlist?> {
        return playlistDao.getPlaylistById(playlistId).map { entity ->
            entity?.let {
                Playlist(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    channelTitle = it.channelTitle,
                    thumbnailUrl = it.thumbnailUrl,
                    videoCount = it.itemCount,
                    totalDurationSeconds = it.totalDurationSeconds,
                    isActive = it.isActive
                )
            }
        }
    }

    override fun getActivePlaylist(): Flow<Playlist?> {
        return playlistDao.getActivePlaylist().map { entity ->
            entity?.let {
                Playlist(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    channelTitle = it.channelTitle,
                    thumbnailUrl = it.thumbnailUrl,
                    videoCount = it.itemCount,
                    totalDurationSeconds = it.totalDurationSeconds,
                    isActive = it.isActive
                )
            }
        }
    }

    override suspend fun setActivePlaylist(playlistId: String) {
        playlistDao.setActivePlaylist(playlistId)
        studySettingsDao.updateActivePlaylist(playlistId)
    }

    override fun getVideosForPlaylist(playlistId: String): Flow<List<Video>> {
        return videoDao.getVideosForPlaylist(playlistId).map { list ->
            list.map {
                Video(
                    id = it.id,
                    playlistId = it.playlistId,
                    title = it.title,
                    description = it.description,
                    thumbnailUrl = it.thumbnailUrl,
                    durationSeconds = it.durationSeconds,
                    position = it.position,
                    topic = it.topic ?: "Genel",
                    isCompleted = it.isCompleted,
                    completedAt = it.completedAt
                )
            }
        }
    }

    override suspend fun toggleVideoCompletion(videoId: String, isCompleted: Boolean) {
        videoDao.setCompletion(videoId, isCompleted)
    }

    override fun getTopicProgress(playlistId: String): Flow<List<TopicProgress>> {
        return videoDao.getTopicStats(playlistId).map { statsList ->
            statsList.map { stats ->
                TopicProgress(
                    topic = stats.topic,
                    totalVideos = stats.totalCount,
                    completedVideos = stats.completedCount,
                    totalDurationSeconds = stats.totalDurationSeconds,
                    completedDurationSeconds = stats.completedDurationSeconds
                )
            }
        }
    }

    override suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylistById(playlistId)
    }
}
