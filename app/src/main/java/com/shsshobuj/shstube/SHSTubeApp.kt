package com.shsshobuj.shstube

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SHSTubeApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        const val CHANNEL_ID_DOWNLOAD = "shstube_download_channel"
        const val CHANNEL_ID_SNIFFER = "shstube_sniffer_channel"
        lateinit var instance: SHSTubeApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        initializeYtDlp()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val downloadChannel = NotificationChannel(
                CHANNEL_ID_DOWNLOAD,
                "SHS Tube Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications"
                setShowBadge(true)
            }

            val snifferChannel = NotificationChannel(
                CHANNEL_ID_SNIFFER,
                "Media Sniffer",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Background media detection service"
            }

            notificationManager.createNotificationChannel(downloadChannel)
            notificationManager.createNotificationChannel(snifferChannel)
        }
    }

    private fun initializeYtDlp() {
        applicationScope.launch {
            try {
                YoutubeDL.getInstance().init(this@SHSTubeApp)
                FFmpeg.getInstance().init(this@SHSTubeApp)
            } catch (e: YoutubeDLException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
