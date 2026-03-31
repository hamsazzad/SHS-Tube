package com.shsshobuj.shstube.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shsshobuj.shstube.ui.theme.CopperOrange
import com.shsshobuj.shstube.ui.theme.MetallicBlue
import com.shsshobuj.shstube.ui.theme.ObsidianBlack
import com.shsshobuj.shstube.ui.theme.SurfaceDark
import com.shsshobuj.shstube.ui.theme.SurfaceDarker

data class CategoryChip(val label: String, val icon: ImageVector, val url: String)
data class QuickAccessSite(val name: String, val url: String, val emoji: String)

val categories = listOf(
    CategoryChip("Trending", Icons.Filled.TrendingUp, "https://www.youtube.com/feed/trending"),
    CategoryChip("Music", Icons.Filled.MusicNote, "https://music.youtube.com"),
    CategoryChip("Gaming", Icons.Filled.SportsEsports, "https://www.youtube.com/gaming"),
    CategoryChip("Movies", Icons.Filled.PlayArrow, "https://www.youtube.com/feed/trending?bp=4gIuChgCEhDlqgpn7Lq2Rh9M5hXi3bfVEhIKAhAB"),
)

val quickAccessSites = listOf(
    QuickAccessSite("YouTube", "https://www.youtube.com", "▶"),
    QuickAccessSite("Facebook", "https://www.facebook.com", "🅕"),
    QuickAccessSite("Instagram", "https://www.instagram.com", "📷"),
    QuickAccessSite("Twitter/X", "https://www.x.com", "𝕏"),
    QuickAccessSite("TikTok", "https://www.tiktok.com", "🎵"),
    QuickAccessSite("Dailymotion", "https://www.dailymotion.com", "🎬"),
    QuickAccessSite("Vimeo", "https://www.vimeo.com", "🎥"),
    QuickAccessSite("SoundCloud", "https://www.soundcloud.com", "🎸"),
    QuickAccessSite("Twitch", "https://www.twitch.tv", "🎮"),
    QuickAccessSite("Reddit", "https://www.reddit.com", "🔴"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onUrlClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trending") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // ── Hero Header ────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF001233),
                                ObsidianBlack
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "SHS Tube",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Download anything, anywhere",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CopperOrange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Futuristic Search Bar
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp)),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Search YouTube, paste any video URL...",
                                fontSize = 13.sp,
                                color = Color(0xFF666677)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, null, tint = MetallicBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    val url = if (searchQuery.startsWith("http"))
                                        searchQuery
                                    else
                                        "https://www.youtube.com/results?search_query=${searchQuery.replace(" ", "+")}"
                                    onUrlClick(url)
                                }) {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        "Search",
                                        tint = CopperOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotBlank()) {
                                val url = if (searchQuery.startsWith("http"))
                                    searchQuery
                                else
                                    "https://www.youtube.com/results?search_query=${searchQuery.replace(" ", "+")}"
                                onUrlClick(url)
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A1A2E),
                            unfocusedContainerColor = Color(0xFF141420),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MetallicBlue
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                }
            }
        }

        // ── Category Chips ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.label,
                        onClick = {
                            selectedCategory = cat.label
                            onUrlClick(cat.url)
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(cat.icon, null, modifier = Modifier.size(14.dp))
                                Text(cat.label, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MetallicBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = SurfaceDark,
                            labelColor = Color(0xFFAAAAAA)
                        ),
                        shape = RoundedCornerShape(50.dp)
                    )
                }
            }
        }

        // ── Quick Access Grid ─────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFAAAAAA),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        val chunkedSites = quickAccessSites.chunked(5)
        items(chunkedSites) { rowSites ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowSites.forEach { site ->
                    QuickAccessItem(site = site, onClick = { onUrlClick(site.url) })
                }
                // Fill empty spots
                repeat(5 - rowSites.size) {
                    Box(modifier = Modifier.size(60.dp))
                }
            }
        }

        // ── Supported Platforms ───────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ Powered by yt-dlp",
                        style = MaterialTheme.typography.titleSmall,
                        color = CopperOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1000+ supported sites including YouTube, Facebook, Instagram, TikTok, Twitter, Vimeo, SoundCloud, Dailymotion, and more.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888),
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Tip Card ──────────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1628)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MetallicBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Pro Tip",
                            color = MetallicBlue,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Browse to any site using the Browser tab. The red badge on the download icon shows detected media. Tap it to see and download all found files!",
                            color = Color(0xFF8888AA),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickAccessItem(site: QuickAccessSite, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDarker)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = site.emoji,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = site.name.take(7),
            color = Color(0xFFAAAAAA),
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
