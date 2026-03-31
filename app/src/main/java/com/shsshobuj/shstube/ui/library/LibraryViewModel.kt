package com.shsshobuj.shstube.ui.library

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import com.shsshobuj.shstube.service.DownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _downloads = MutableStateFlow<List<DownloadService.DownloadTask>>(emptyList())
    val downloads: StateFlow<List<DownloadService.DownloadTask>> = _downloads.asStateFlow()

    private var downloadService: DownloadService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? DownloadService.DownloadBinder
            downloadService = binder?.getService()
            downloadService?.downloads?.let { flow ->
                // Collect from service state flow - done via coroutine in a real impl
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
        }
    }

    init {
        val context = application
        Intent(context, DownloadService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
    }
}
