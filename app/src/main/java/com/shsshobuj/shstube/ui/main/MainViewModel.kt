package com.shsshobuj.shstube.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Currently selected bottom nav destination
    private val _selectedTab = MutableStateFlow(BottomNavTab.HOME)
    val selectedTab: StateFlow<BottomNavTab> = _selectedTab.asStateFlow()

    // Shared URL from intent/share
    private val _sharedUrl = MutableStateFlow<String?>(null)
    val sharedUrl: StateFlow<String?> = _sharedUrl.asStateFlow()

    // Browser current URL
    private val _browserUrl = MutableStateFlow("https://www.youtube.com")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    fun selectTab(tab: BottomNavTab) {
        _selectedTab.value = tab
    }

    fun handleSharedUrl(url: String) {
        viewModelScope.launch {
            _sharedUrl.value = url
            _browserUrl.value = url
            _selectedTab.value = BottomNavTab.BROWSER
        }
    }

    fun updateBrowserUrl(url: String) {
        _browserUrl.value = url
    }

    fun consumeSharedUrl() {
        _sharedUrl.value = null
    }
}

enum class BottomNavTab {
    HOME, BROWSER, LIBRARY, SETTINGS
}
