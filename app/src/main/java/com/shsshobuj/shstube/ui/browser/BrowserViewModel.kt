package com.shsshobuj.shstube.ui.browser

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shsshobuj.shstube.service.DownloadService
import com.shsshobuj.shstube.service.UrlSnifferService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val sniffer = UrlSnifferService()

    val detectedMedia: StateFlow<List<UrlSnifferService.DetectedMedia>> = sniffer.detectedMedia
    val mediaCount: StateFlow<Int> = sniffer.mediaCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _pageTitle = MutableStateFlow("")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    fun analyzeUrl(url: String) {
        sniffer.analyzeUrl(url)
    }

    fun resetSniffer() {
        sniffer.resetDetectedMedia()
    }

    fun onPageStarted(url: String) {
        _isLoading.value = true
        _pageTitle.value = ""
        resetSniffer()
    }

    fun onPageFinished(url: String) {
        _isLoading.value = false
        _loadingProgress.value = 100
    }

    fun updateProgress(progress: Int) {
        _loadingProgress.value = progress
        _isLoading.value = progress < 100
    }

    fun updatePageTitle(title: String) {
        _pageTitle.value = title
    }

    fun startDownload(media: UrlSnifferService.DetectedMedia) {
        val context = getApplication<Application>()
        val outputPath = context.getExternalFilesDir(null)?.absolutePath
            ?: "/sdcard/SHSTube"

        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_START_DOWNLOAD
            putExtra(DownloadService.EXTRA_URL, media.url)
            putExtra(DownloadService.EXTRA_TITLE, media.displayName.ifBlank { "Download" })
            putExtra(DownloadService.EXTRA_FORMAT, media.format)
            putExtra(DownloadService.EXTRA_OUTPUT_PATH, outputPath)
        }
        context.startService(intent)
    }
}
