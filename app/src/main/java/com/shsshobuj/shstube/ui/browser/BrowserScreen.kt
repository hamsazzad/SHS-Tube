package com.shsshobuj.shstube.ui.browser

import android.annotation.SuppressLint
import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shsshobuj.shstube.service.UrlSnifferService
import com.shsshobuj.shstube.ui.main.MainViewModel
import com.shsshobuj.shstube.ui.theme.CopperOrange
import com.shsshobuj.shstube.ui.theme.MetallicBlue
import com.shsshobuj.shstube.ui.theme.ObsidianBlack
import com.shsshobuj.shstube.ui.theme.SurfaceDark
import com.shsshobuj.shstube.ui.theme.SurfaceDarker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: MainViewModel) {
    val browserViewModel: BrowserViewModel = viewModel()
    val currentUrl by viewModel.browserUrl.collectAsState()
    val detectedMedia by browserViewModel.detectedMedia.collectAsState()
    val mediaCount by browserViewModel.mediaCount.collectAsState()
    val isLoading by browserViewModel.isLoading.collectAsState()
    val pageTitle by browserViewModel.pageTitle.collectAsState()
    val loadingProgress by browserViewModel.loadingProgress.collectAsState()

    var showDownloadSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    var urlText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // ── Top Browser Toolbar ───────────────────────────────────────────────
        BrowserToolbar(
            urlText = urlText,
            onUrlChange = { urlText = it },
            onNavigate = { url ->
                val formattedUrl = formatUrl(url)
                urlText = formattedUrl
                viewModel.updateBrowserUrl(formattedUrl)
                webViewRef?.loadUrl(formattedUrl)
            },
            onBack = { webViewRef?.goBack() },
            onForward = { webViewRef?.goForward() },
            onRefresh = { webViewRef?.reload() },
            onHome = {
                val homeUrl = "https://www.youtube.com"
                urlText = homeUrl
                webViewRef?.loadUrl(homeUrl)
                browserViewModel.resetSniffer()
            },
            mediaCount = mediaCount,
            onDownloadClick = {
                scope.launch { sheetState.show() }
                showDownloadSheet = true
            },
            isLoading = isLoading
        )

        // ── Loading Progress Bar ──────────────────────────────────────────────
        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MetallicBlue,
                trackColor = SurfaceDark
            )
        }

        // ── WebView ───────────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    configureWebView(this, browserViewModel, viewModel) { newUrl ->
                        urlText = newUrl
                    }
                    webViewRef = this
                    loadUrl(currentUrl)
                }
            },
            update = { webView ->
                webViewRef = webView
                if (webView.url != currentUrl && currentUrl != webView.url) {
                    webView.loadUrl(currentUrl)
                }
            }
        )
    }

    // ── Media Detection Bottom Sheet ─────────────────────────────────────────
    if (showDownloadSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showDownloadSheet = false
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState,
            containerColor = SurfaceDarker,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF555555)) }
        ) {
            MediaDetectionBottomSheet(
                detectedMedia = detectedMedia,
                pageTitle = pageTitle,
                onDownload = { media ->
                    browserViewModel.startDownload(media)
                    showDownloadSheet = false
                },
                onDismiss = {
                    showDownloadSheet = false
                    scope.launch { sheetState.hide() }
                }
            )
        }
    }
}

@Composable
fun BrowserToolbar(
    urlText: String,
    onUrlChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onHome: () -> Unit,
    mediaCount: Int,
    onDownloadClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation buttons
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFFCCCCCC), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onForward, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ArrowForward, "Forward", tint = Color(0xFFCCCCCC), modifier = Modifier.size(20.dp))
            }

            // URL Bar
            TextField(
                value = urlText,
                onValueChange = onUrlChange,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                singleLine = true,
                placeholder = { Text("Search or enter URL", fontSize = 13.sp, color = Color(0xFF666666)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onNavigate(urlText) }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1A2E),
                    unfocusedContainerColor = Color(0xFF141420),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFCCCCCC),
                    focusedIndicatorColor = MetallicBlue,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MetallicBlue
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                shape = RoundedCornerShape(8.dp)
            )

            // Refresh/Stop
            IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (isLoading) Icons.Filled.Close else Icons.Filled.Refresh,
                    "Refresh",
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Download badge button
            BadgedBox(
                badge = {
                    if (mediaCount > 0) {
                        Badge(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = if (mediaCount > 12) "12+" else mediaCount.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Download,
                        "Download",
                        tint = if (mediaCount > 0) CopperOrange else Color(0xFF888888),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaDetectionBottomSheet(
    detectedMedia: List<UrlSnifferService.DetectedMedia>,
    pageTitle: String,
    onDownload: (UrlSnifferService.DetectedMedia) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Detected Media",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (pageTitle.isNotBlank()) {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "${detectedMedia.size} found",
                style = MaterialTheme.typography.bodySmall,
                color = CopperOrange,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0xFF2A2A3A))
        Spacer(modifier = Modifier.height(8.dp))

        if (detectedMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        tint = Color(0xFF444455),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No media detected yet",
                        color = Color(0xFF666677),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Browse a page with video/audio content",
                        color = Color(0xFF444455),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(detectedMedia) { media ->
                    MediaItemCard(media = media, onDownload = { onDownload(media) })
                }
            }
        }
    }
}

@Composable
fun MediaItemCard(
    media: UrlSnifferService.DetectedMedia,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Format Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (media.isStream) MetallicBlue.copy(alpha = 0.3f)
                        else CopperOrange.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = media.format.uppercase(),
                    color = if (media.isStream) MetallicBlue else CopperOrange,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (media.displayName.isNotBlank()) media.displayName else "Media File",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quality chip
                    InfoChip(
                        text = media.quality,
                        color = if (media.quality.contains("1080") || media.quality.contains("720"))
                            Color(0xFF4CAF50) else Color(0xFF888888)
                    )
                    // Size chip
                    InfoChip(
                        text = media.fileSizeEstimate,
                        color = Color(0xFF888888)
                    )
                    if (media.isStream) {
                        InfoChip(text = "STREAM", color = MetallicBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Download Button
            Button(
                onClick = onDownload,
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MetallicBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = "Download",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Get", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun InfoChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── WebView Configuration ─────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
private fun configureWebView(
    webView: WebView,
    browserViewModel: BrowserViewModel,
    mainViewModel: MainViewModel,
    onUrlChanged: (String) -> Unit
) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        userAgentString = "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Mobile Safari/537.36"
        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        allowContentAccess = true
        allowFileAccess = true
        mediaPlaybackRequiresUserGesture = false
    }

    webView.webViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let {
                onUrlChanged(it)
                mainViewModel.updateBrowserUrl(it)
                browserViewModel.onPageStarted(it)
                browserViewModel.analyzeUrl(it)
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let {
                onUrlChanged(it)
                browserViewModel.onPageFinished(it)
                browserViewModel.analyzeUrl(it)
            }
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            request?.url?.toString()?.let { url ->
                // This is the core sniffer intercept point
                // Every network request the page makes is checked here
                browserViewModel.analyzeUrl(url)
            }
            return null // Let WebView handle normally
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return false // Let WebView handle
            }
            return true // Block other schemes
        }
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            browserViewModel.updateProgress(newProgress)
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            title?.let { browserViewModel.updatePageTitle(it) }
        }
    }
}

private fun formatUrl(input: String): String {
    val trimmed = input.trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
        else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
    }
}
