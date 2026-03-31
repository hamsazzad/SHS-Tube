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
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class DownloadService : Service() {

    inner class DownloadBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }

    private val binder = DownloadBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadJobs = ConcurrentHashMap<String, Job>()

    data class DownloadTask(
        val id: String,
        val url: String,
        val title: String,
        val format: String,
        val outputPath: String,
        var status: DownloadStatus = DownloadStatus.QUEUED,
        var progress: Float = 0f,
        var speed: String = "",
        var eta: String = "",
        var errorMessage: String = ""
    )

    enum class DownloadStatus {
        QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
    }

    private val _downloads = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloads: StateFlow<List<DownloadTask>> = _downloads.asStateFlow()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Download"
                val format = intent.getStringExtra(EXTRA_FORMAT) ?: "mp4"
                val outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH) ?: getExternalFilesDir(null)?.absolutePath ?: ""
                startDownload(url, title, format, outputPath)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val id = intent.getStringExtra(EXTRA_DOWNLOAD_ID) ?: return START_STICKY
                cancelDownload(id)
            }
        }
        return START_STICKY
    }

    fun startDownload(url: String, title: String, format: String, outputPath: String) {
        val id = System.currentTimeMillis().toString()
        val task = DownloadTask(id = id, url = url, title = title, format = format, outputPath = outputPath)

        val currentList = _downloads.value.toMutableList()
        currentList.add(0, task)
        _downloads.value = currentList

        startForeground(NOTIFICATION_ID_BASE, buildProgressNotification(title, 0))

        val job = serviceScope.launch {
            try {
                updateDownloadStatus(id, DownloadStatus.DOWNLOADING)

                val request = YoutubeDLRequest(url).apply {
                    addOption("-o", "$outputPath/%(title)s.%(ext)s")
                    when (format) {
                        "mp3", "m4a", "aac", "flac", "wav", "ogg" -> {
                            addOption("-x")
                            addOption("--audio-format", format)
                            addOption("--audio-quality", "0")
                        }
                        "m3u8", "mpd" -> {
                            addOption("--hls-use-mpegts")
                            addOption("--merge-output-format", "mp4")
                        }
                        else -> {
                            addOption("-f", "bestvideo+bestaudio/best")
                            addOption("--merge-output-format", format)
                        }
                    }
                    addOption("--no-warnings")
                    addOption("--no-playlist")
                }

                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                    val etaStr = when {
                        etaInSeconds > 3600 -> "${etaInSeconds / 3600}h ${(etaInSeconds % 3600) / 60}m"
                        etaInSeconds > 60 -> "${etaInSeconds / 60}m ${etaInSeconds % 60}s"
                        else -> "${etaInSeconds}s"
                    }

                    updateDownloadProgress(id, progress / 100f, "", etaStr)
                    updateNotificationProgress(title, progress.toInt())
                }

                updateDownloadStatus(id, DownloadStatus.COMPLETED)
            } catch (e: Exception) {
                updateDownloadStatus(id, DownloadStatus.FAILED, e.message ?: "Unknown error")
            }
        }

        downloadJobs[id] = job
    }

    fun cancelDownload(id: String) {
        downloadJobs[id]?.cancel()
        downloadJobs.remove(id)
        updateDownloadStatus(id, DownloadStatus.CANCELLED)
    }

    private fun updateDownloadStatus(id: String, status: DownloadStatus, error: String = "") {
        val list = _downloads.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(status = status, errorMessage = error)
            _downloads.value = list
        }
    }

    private fun updateDownloadProgress(id: String, progress: Float, speed: String, eta: String) {
        val list = _downloads.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(progress = progress, speed = speed, eta = eta)
            _downloads.value = list
        }
    }

    private fun buildProgressNotification(title: String, progress: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SHSTubeApp.CHANNEL_ID_DOWNLOAD)
            .setContentTitle("Downloading: $title")
            .setContentText("$progress%")
            .setSmallIcon(R.drawable.ic_download_notification)
            .setProgress(100, progress, progress == 0)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotificationProgress(title: String, progress: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE, buildProgressNotification(title, progress))
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID_BASE, buildProgressNotification("SHS Tube", 0))
    }

    companion object {
        const val NOTIFICATION_ID_BASE = 2001
        const val ACTION_START_DOWNLOAD = "com.shsshobuj.shstube.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.shsshobuj.shstube.CANCEL_DOWNLOAD"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_FORMAT = "extra_format"
        const val EXTRA_OUTPUT_PATH = "extra_output_path"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"
    }
}
