package com.shsshobuj.shstube.ui.library

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shsshobuj.shstube.service.DownloadService
import com.shsshobuj.shstube.ui.theme.CopperOrange
import com.shsshobuj.shstube.ui.theme.MetallicBlue
import com.shsshobuj.shstube.ui.theme.ObsidianBlack
import com.shsshobuj.shstube.ui.theme.SurfaceDark

@Composable
fun LibraryScreen(libraryViewModel: LibraryViewModel = viewModel()) {
    val downloads by libraryViewModel.downloads.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF060B14))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${downloads.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = CopperOrange
                )
            }
        }

        if (downloads.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        tint = Color(0xFF2A2A3A),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Downloads Yet",
                        color = Color(0xFF555566),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Go to Browser tab and download media",
                        color = Color(0xFF333344),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(downloads) { download ->
                    DownloadCard(
                        download = download,
                        onCancel = {
                            val intent = Intent(context, DownloadService::class.java).apply {
                                action = DownloadService.ACTION_CANCEL_DOWNLOAD
                                putExtra(DownloadService.EXTRA_DOWNLOAD_ID, download.id)
                            }
                            context.startService(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadCard(
    download: DownloadService.DownloadTask,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
                Icon(
                    imageVector = when (download.status) {
                        DownloadService.DownloadStatus.DOWNLOADING -> Icons.Filled.Download
                        DownloadService.DownloadStatus.COMPLETED -> Icons.Filled.CheckCircle
                        DownloadService.DownloadStatus.FAILED -> Icons.Filled.Error
                        DownloadService.DownloadStatus.CANCELLED -> Icons.Filled.Cancel
                        DownloadService.DownloadStatus.PAUSED -> Icons.Filled.Pause
                        DownloadService.DownloadStatus.QUEUED -> Icons.Filled.HourglassTop
                    },
                    contentDescription = null,
                    tint = when (download.status) {
                        DownloadService.DownloadStatus.DOWNLOADING -> MetallicBlue
                        DownloadService.DownloadStatus.COMPLETED -> Color(0xFF4CAF50)
                        DownloadService.DownloadStatus.FAILED -> Color(0xFFE53935)
                        DownloadService.DownloadStatus.CANCELLED -> Color(0xFF888888)
                        DownloadService.DownloadStatus.PAUSED -> CopperOrange
                        DownloadService.DownloadStatus.QUEUED -> Color(0xFF888888)
                    },
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${download.format.uppercase()} • ${download.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        color = Color(0xFF888888),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (download.status == DownloadService.DownloadStatus.DOWNLOADING) {
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Cancel,
                            contentDescription = "Cancel",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Progress Bar for active downloads
            if (download.status == DownloadService.DownloadStatus.DOWNLOADING ||
                download.status == DownloadService.DownloadStatus.QUEUED) {
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { if (download.status == DownloadService.DownloadStatus.QUEUED) 0f else download.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MetallicBlue,
                    trackColor = Color(0xFF2A2A3A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(download.progress * 100).toInt()}%",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                    if (download.eta.isNotBlank()) {
                        Text(
                            text = "ETA: ${download.eta}",
                            color = Color(0xFF888888),
                            fontSize = 11.sp
                        )
                    }
                    if (download.speed.isNotBlank()) {
                        Text(
                            text = download.speed,
                            color = Color(0xFF888888),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (download.status == DownloadService.DownloadStatus.FAILED && download.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = download.errorMessage,
                    color = Color(0xFFE53935),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
