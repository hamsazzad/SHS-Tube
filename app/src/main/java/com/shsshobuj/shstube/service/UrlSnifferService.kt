package com.shsshobuj.shstube.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shsshobuj.shstube.R
import com.shsshobuj.shstube.SHSTubeApp
import com.shsshobuj.shstube.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * UrlSnifferService
 *
 * A foreground service that acts as the media sniffer engine.
 * It receives URLs intercepted from the WebView, analyzes them using
 * regex pattern matching, deduplicates entries, and maintains a reactive
 * StateFlow of detected media items so the browser UI badge counter updates
 * in real time.
 *
 * Architecture: WebView → shouldInterceptRequest() → UrlSnifferService → StateFlow → BadgeCounter UI
 */
class UrlSnifferService : Service() {

    // ─── Binder for Activity/Fragment binding ────────────────────────────────
    inner class SnifferBinder : Binder() {
        fun getService(): UrlSnifferService = this@UrlSnifferService
    }

    private val binder = SnifferBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ─── Media Detection Regex Patterns ──────────────────────────────────────
    private val MEDIA_PATTERNS = listOf(
        // HLS/DASH manifest streams (highest priority)
        Regex("""\.m3u8(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.mpd(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        // Direct video files
        Regex("""\.mp4(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.webm(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.mkv(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.avi(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.mov(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.flv(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        // Direct audio files
        Regex("""\.mp3(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.m4a(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.ogg(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.flac(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.wav(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.aac(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        // CDN/streaming URL patterns (no extension)
        Regex("""videoplayback""", RegexOption.IGNORE_CASE),
        Regex("""manifest\.googlevideo""", RegexOption.IGNORE_CASE),
        Regex("""stream\.php""", RegexOption.IGNORE_CASE),
        Regex("""/hls/""", RegexOption.IGNORE_CASE),
        Regex("""/dash/""", RegexOption.IGNORE_CASE),
        Regex("""/video/\d+""", RegexOption.IGNORE_CASE),
        Regex("""content-type=video""", RegexOption.IGNORE_CASE),
    )

    private val EXCLUDED_PATTERNS = listOf(
        Regex("""\.js(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.css(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.png(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.jpg(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.gif(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.ico(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""\.woff(\?.*)?${'$'}""", RegexOption.IGNORE_CASE),
        Regex("""googleapis\.com/css""", RegexOption.IGNORE_CASE),
        Regex("""analytics""", RegexOption.IGNORE_CASE),
        Regex("""tracking""", RegexOption.IGNORE_CASE),
        Regex("""doubleclick\.net""", RegexOption.IGNORE_CASE),
    )

    // ─── State Management ─────────────────────────────────────────────────────
    data class DetectedMedia(
        val url: String,
        val format: String,         // mp4, m3u8, webm, mp3, etc.
        val isStream: Boolean,      // true for HLS/DASH
        val quality: String = "Unknown",
        val fileSizeEstimate: String = "Unknown",
        val displayName: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _detectedMedia = MutableStateFlow<List<DetectedMedia>>(emptyList())
    val detectedMedia: StateFlow<List<DetectedMedia>> = _detectedMedia.asStateFlow()

    private val _mediaCount = MutableStateFlow(0)
    val mediaCount: StateFlow<Int> = _mediaCount.asStateFlow()

    private val seenUrls = mutableSetOf<String>()
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // ─── Service Lifecycle ────────────────────────────────────────────────────
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ANALYZE_URL -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_STICKY
                analyzeUrl(url)
            }
            ACTION_RESET -> resetDetectedMedia()
        }
        return START_STICKY
    }

    // ─── Core Sniffing Logic ──────────────────────────────────────────────────

    /**
     * Analyzes a single URL to determine if it is media content.
     * Called from WebView's shouldInterceptRequest on every network request.
     */
    fun analyzeUrl(url: String) {
        if (url.isBlank() || url.length < 10) return
        if (isExcluded(url)) return
        if (!isMediaUrl(url)) return

        val normalizedUrl = normalizeUrl(url)
        if (seenUrls.contains(normalizedUrl)) return

        seenUrls.add(normalizedUrl)
        val format = extractFormat(url)
        val isStream = format == "m3u8" || format == "mpd"
        val displayName = extractDisplayName(url)

        val media = DetectedMedia(
            url = url,
            format = format,
            isStream = isStream,
            displayName = displayName
        )

        val currentList = _detectedMedia.value.toMutableList()
        currentList.add(0, media) // newest first
        _detectedMedia.value = currentList
        _mediaCount.value = currentList.size

        // Attempt HEAD request to get file size / quality in background
        if (!isStream) {
            serviceScope.launch {
                enrichMediaInfo(normalizedUrl, media, currentList.size - 1)
            }
        }
    }

    /**
     * Batch analyze multiple URLs (e.g., from page source scan)
     */
    fun analyzeUrls(urls: List<String>) {
        urls.forEach { analyzeUrl(it) }
    }

    /**
     * Reset state when user navigates to a new page
     */
    fun resetDetectedMedia() {
        seenUrls.clear()
        _detectedMedia.value = emptyList()
        _mediaCount.value = 0
    }

    // ─── Pattern Matching ─────────────────────────────────────────────────────

    private fun isMediaUrl(url: String): Boolean {
        return MEDIA_PATTERNS.any { pattern -> pattern.containsMatchIn(url) }
    }

    private fun isExcluded(url: String): Boolean {
        return EXCLUDED_PATTERNS.any { pattern -> pattern.containsMatchIn(url) }
    }

    private fun extractFormat(url: String): String {
        val cleanUrl = url.substringBefore("?").lowercase()
        return when {
            cleanUrl.endsWith(".m3u8") -> "m3u8"
            cleanUrl.endsWith(".mpd") -> "mpd"
            cleanUrl.endsWith(".mp4") -> "mp4"
            cleanUrl.endsWith(".webm") -> "webm"
            cleanUrl.endsWith(".mkv") -> "mkv"
            cleanUrl.endsWith(".avi") -> "avi"
            cleanUrl.endsWith(".mov") -> "mov"
            cleanUrl.endsWith(".flv") -> "flv"
            cleanUrl.endsWith(".mp3") -> "mp3"
            cleanUrl.endsWith(".m4a") -> "m4a"
            cleanUrl.endsWith(".ogg") -> "ogg"
            cleanUrl.endsWith(".flac") -> "flac"
            cleanUrl.endsWith(".wav") -> "wav"
            cleanUrl.endsWith(".aac") -> "aac"
            url.contains("videoplayback", ignoreCase = true) -> "mp4"
            url.contains("/hls/", ignoreCase = true) -> "m3u8"
            url.contains("/dash/", ignoreCase = true) -> "mpd"
            else -> "mp4"
        }
    }

    private fun extractDisplayName(url: String): String {
        return try {
            url.substringBefore("?").substringAfterLast("/")
                .takeIf { it.isNotBlank() } ?: "Media File"
        } catch (e: Exception) {
            "Media File"
        }
    }

    private fun normalizeUrl(url: String): String {
        return url.substringBefore("?").lowercase().trimEnd('/')
    }

    // ─── Media Info Enrichment (HEAD requests) ───────────────────────────────

    private suspend fun enrichMediaInfo(url: String, media: DetectedMedia, index: Int) {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .head()
                    .addHeader("User-Agent", "Mozilla/5.0 (Android)")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    val contentLength = response.header("Content-Length")?.toLongOrNull()
                    val contentType = response.header("Content-Type") ?: ""

                    val sizeStr = when {
                        contentLength == null -> "Unknown size"
                        contentLength < 1024 * 1024 -> "${contentLength / 1024} KB"
                        contentLength < 1024 * 1024 * 1024 -> "%.1f MB".format(contentLength / (1024.0 * 1024.0))
                        else -> "%.2f GB".format(contentLength / (1024.0 * 1024.0 * 1024.0))
                    }

                    val quality = when {
                        contentType.contains("1080") || url.contains("1080") -> "1080p"
                        contentType.contains("720") || url.contains("720") -> "720p"
                        contentType.contains("480") || url.contains("480") -> "480p"
                        contentType.contains("360") || url.contains("360") -> "360p"
                        contentType.contains("audio") || media.format in listOf("mp3","m4a","ogg","flac","wav","aac") -> "Audio"
                        else -> "Video"
                    }

                    val enriched = media.copy(
                        fileSizeEstimate = sizeStr,
                        quality = quality
                    )

                    val currentList = _detectedMedia.value.toMutableList()
                    val idx = currentList.indexOfFirst { it.url == media.url }
                    if (idx >= 0) {
                        currentList[idx] = enriched
                        _detectedMedia.value = currentList
                    }
                }
            } catch (e: Exception) {
                // Silent fail - HEAD request failed, keep defaults
            }
        }
    }

    // ─── Foreground Notification ──────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SHSTubeApp.CHANNEL_ID_SNIFFER)
            .setContentTitle("SHS Tube")
            .setContentText("Media sniffer active")
            .setSmallIcon(R.drawable.ic_download_notification)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_ANALYZE_URL = "com.shsshobuj.shstube.ANALYZE_URL"
        const val ACTION_RESET = "com.shsshobuj.shstube.RESET_SNIFFER"
        const val EXTRA_URL = "extra_url"
    }
}
