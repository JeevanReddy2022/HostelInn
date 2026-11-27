package com.example.hostelmanager.ui.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hostelmanager.R
import com.example.hostelmanager.ui.components.SectionHeader
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppOnNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSky
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftGreen
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import kotlinx.coroutines.delay

/* ---------------- Brand constants ---------------- */
private val BrandNavy = Color(0xFF0B1F33)
private val BrandDeep = Color(0xFF123B5D)
private val BrandTeal = Color(0xFF0F766E)
private val BrandSky = Color(0xFF38BDF8)
private val BrandGreen = Color(0xFF34D399)

private const val APP_TAGLINE = "BOOKING & STAYS"
private const val FALLBACK_VERSION = "1.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    @Suppress("DEPRECATION")
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: FALLBACK_VERSION
    }

    val appInfo = remember(versionName) {
        """
        HostelInn — Booking & Stays
        Version $versionName
        Package ${context.packageName}
        Android ${Build.VERSION.RELEASE} · ${Build.MODEL}
        Created by Adam
        """.trimIndent()
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogoTile(size = 28.dp, corner = 8.dp, elevation = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("About", fontWeight = FontWeight.Bold, color = AppText)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = AppText,
                    navigationIconContentColor = AppText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            /* ---------- Hero ---------- */
            Reveal(0) { HeroCard(versionName) }

            /* ---------- At a glance ---------- */
            Reveal(70) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlanceTile(Icons.Default.WifiOff, "Offline", "No internet needed", AppSky, AppSoftBlue, Modifier.weight(1f))
                    GlanceTile(Icons.Default.Lock, "Private", "Stays on device", AppGreen, AppSoftGreen, Modifier.weight(1f))
                    GlanceTile(Icons.Default.Apartment, "Multi-site", "Many hostels", AppOrange, AppSoftOrange, Modifier.weight(1f))
                }
            }

            /* ---------- What it does ---------- */
            Reveal(140) {
                Column {
                    SectionHeader("What HostelInn does")
                    Spacer(Modifier.height(12.dp))
                    val features = listOf(
                        Feature(Icons.Default.Bed, "Rooms & Beds", "Live vacancy", AppSky, AppSoftBlue),
                        Feature(Icons.Default.People, "Residents", "Full stay history", AppGreen, AppSoftGreen),
                        Feature(Icons.Default.Hotel, "Daily Guests", "Per-night billing", AppOrange, AppSoftOrange),
                        Feature(Icons.Default.Payments, "Payments", "Dues & arrears", AppNavy, AppSoftBlue),
                        Feature(Icons.Default.Receipt, "Expenses", "Category-wise", AppRed, AppSoftRed),
                        Feature(Icons.Default.BarChart, "Reports", "Monthly profit", AppGreen, AppSoftGreen),
                        Feature(Icons.Default.Notifications, "Notice", "Planned check-outs", AppOrange, AppSoftOrange),
                        Feature(Icons.Default.History, "Audit Trail", "Every edit logged", AppSky, AppSoftBlue)
                    )
                    features.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { f -> FeatureTile(f, Modifier.weight(1f)) }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            /* ---------- Author ---------- */
            Reveal(210) {
                Column {
                    SectionHeader("Author")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Built and owned by the person who runs the desk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                    Spacer(Modifier.height(12.dp))

                    AuthorCard(
                        initial = "A",
                        name = "Adam",
                        role = "Creator & Product Owner",
                        bio = "Runs the hostel and shaped every rule in this app — billing cycles, " +
                                "notice periods, daily guests and write-offs all come from real desk " +
                                "experience. Designed the workflows, decided what the app should do, " +
                                "and tested every screen against real data.",
                        highlights = listOf("Product Design", "Hostel Operations", "Testing"),
                        gradient = listOf(BrandNavy, BrandTeal),
                        accent = AppGreen,
                        soft = AppSoftGreen
                    )
                }
            }

            /* ---------- Privacy ---------- */
            Reveal(280) {
                Column {
                    SectionHeader("Your data")
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppCard),
                        border = BorderStroke(1.dp, AppBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            PrivacyRow(Icons.Default.PhoneAndroid, "Everything lives on this phone", "Rooms, residents, payments and expenses are stored in a local database.")
                            PrivacyRow(Icons.Default.Lock, "Photos stay private", "ID and profile photos are saved in app-private storage, never uploaded.")
                            PrivacyRow(Icons.Default.WifiOff, "No ads, no tracking", "No analytics, no accounts, no servers. The app works fully offline.")
                        }
                    }
                }
            }

            /* ---------- Build info + actions ---------- */
            Reveal(350) {
                Column {
                    SectionHeader("Build info")
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppCard),
                        border = BorderStroke(1.dp, AppBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppLogoTile(size = 44.dp, corner = 12.dp, elevation = 3.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "HostelInn",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppText
                                    )
                                    Text(
                                        "Version $versionName",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppMuted
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            InfoRow("Package", context.packageName)
                            InfoRow("Android", Build.VERSION.RELEASE)
                            InfoRow("Device", Build.MODEL)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("HostelInn", appInfo))
                                    Toast.makeText(context, "App info copied", Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    Toast.makeText(context, "Could not copy", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, AppNavy)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AppNavy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copy info", color = AppNavy, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Button(
                            onClick = {
                                runCatching {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "HostelInn")
                                        putExtra(Intent.EXTRA_TEXT, appInfo)
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share HostelInn"))
                                }.onFailure {
                                    Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppNavy, contentColor = AppOnNavy)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Share", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }

            /* ---------- Footer ---------- */
            Reveal(420) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppLogoTile(size = 40.dp, corner = 11.dp, elevation = 4.dp)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier.width(44.dp).height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(BrandSky, BrandGreen)))
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "HostelInn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "© 2026 Adam · All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                    Text(
                        "Made for hostel desks, not spreadsheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  The real launcher icon, rendered exactly as Android draws it        */
/* ------------------------------------------------------------------ */
@Composable
private fun AppLogoTile(
    size: Dp,
    corner: Dp,
    elevation: Dp = 8.dp
) {
    val context = LocalContext.current
    val px = with(LocalDensity.current) { size.roundToPx() }.coerceAtLeast(1)

    val icon: ImageBitmap? = remember(px) {
        runCatching {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                ?: context.packageManager.getApplicationIcon(context.packageName)
            val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap)
            drawable.setBounds(0, 0, px, px)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }

    val shape = RoundedCornerShape(corner)
    Box(
        modifier = Modifier
            .size(size)
            .shadow(elevation = elevation, shape = shape, clip = false)
            .clip(shape)
            .background(BrandNavy)
            .border(1.dp, Color.White.copy(alpha = 0.20f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = "HostelInn app logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            DrawnLogoFallback(size = size * 0.8f)
        }
    }
}

/** Only used if the platform drawable cannot be read. */
@Composable
private fun DrawnLogoFallback(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val towerW = w * 0.27f
        val leftX = w * 0.11f
        val rightX = w * 0.62f
        val topY = h * 0.05f
        val botY = h * 0.95f
        val towerH = botY - topY
        val barTop = h * 0.44f
        val barH = h * 0.12f

        drawRect(Color.White, Offset(leftX, topY), Size(towerW, towerH))
        drawRect(Color.White, Offset(rightX, topY), Size(towerW, towerH))
        drawRect(Color.White, Offset(leftX + towerW, barTop), Size(rightX - leftX - towerW, barH))

        val rows = 8
        val cols = 2
        val padX = towerW * 0.17f
        val stepX = (towerW - padX * 2) / cols
        val cellW = stepX * 0.68f
        val innerTop = topY + towerH * 0.045f
        val stepY = (towerH * 0.91f) / rows
        val cellH = stepY * 0.60f

        val litLeft = mapOf(0 to BrandSky, 3 to BrandGreen, 5 to BrandSky, 7 to BrandGreen)
        val litRight = mapOf(1 to BrandSky, 2 to BrandGreen, 4 to BrandSky, 6 to BrandGreen)

        for (t in 0..1) {
            val baseX = if (t == 0) leftX else rightX
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val lit = if (t == 0) litLeft[r] else litRight[r]
                    val color = if (lit != null && (r + c) % 2 == 0) lit else BrandNavy
                    drawRect(
                        color = color,
                        topLeft = Offset(
                            baseX + padX + c * stepX + (stepX - cellW) / 2f,
                            innerTop + r * stepY + (stepY - cellH) / 2f
                        ),
                        size = Size(cellW, cellH)
                    )
                }
            }
        }

        val barCells = 4
        val barPad = w * 0.012f
        val barAvail = rightX - leftX - towerW - barPad * 2
        val bStep = barAvail / barCells
        val bCell = minOf(bStep * 0.6f, barH * 0.42f)
        for (i in 0 until barCells) {
            drawRect(
                color = if (i == 2) BrandGreen else BrandNavy,
                topLeft = Offset(
                    leftX + towerW + barPad + i * bStep + (bStep - bCell) / 2f,
                    barTop + (barH - bCell) / 2f
                ),
                size = Size(bCell, bCell)
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Hero                                                               */
/* ------------------------------------------------------------------ */
@Composable
private fun HeroCard(versionName: String) {
    val drift = rememberInfiniteTransition(label = "heroGlow")
    val shift by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowShift"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(Modifier.background(Brush.linearGradient(listOf(BrandNavy, BrandDeep, BrandTeal)))) {
            Box(
                Modifier.matchParentSize().background(
                    Brush.radialGradient(
                        colors = listOf(BrandSky.copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(120f + shift * 220f, 60f + shift * 90f),
                        radius = 620f
                    )
                )
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.radialGradient(
                        colors = listOf(BrandGreen.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(760f - shift * 200f, 620f - shift * 120f),
                        radius = 640f
                    )
                )
            )

            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppLogoTile(size = 72.dp, corner = 20.dp, elevation = 10.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "HostelInn",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            APP_TAGLINE,
                            fontSize = 10.sp,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSky,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    "A complete front-desk companion for hostel owners — rooms, residents, " +
                            "daily guests, rent, expenses and profit, all in one place and all offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroPill("v$versionName")
                    HeroPill("Offline-first")
                    HeroPill("Audit-safe")
                }
            }
        }
    }
}

@Composable
private fun HeroPill(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
    }
}

/* ------------------------------------------------------------------ */
/*  Tiles                                                              */
/* ------------------------------------------------------------------ */
@Composable
private fun GlanceTile(
    icon: ImageVector,
    title: String,
    caption: String,
    accent: Color,
    soft: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(soft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.height(9.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppText, maxLines = 1)
            Text(caption, fontSize = 10.sp, color = AppMuted, maxLines = 2, lineHeight = 13.sp)
        }
    }
}

private data class Feature(
    val icon: ImageVector,
    val title: String,
    val caption: String,
    val accent: Color,
    val soft: Color
)

@Composable
private fun FeatureTile(f: Feature, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(f.soft),
                contentAlignment = Alignment.Center
            ) {
                Icon(f.icon, contentDescription = null, tint = f.accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(f.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppText, maxLines = 1)
                Text(f.caption, fontSize = 10.sp, color = AppMuted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun AuthorCard(
    initial: String,
    name: String,
    role: String,
    bio: String,
    highlights: List<String>,
    gradient: List<Color>,
    accent: Color,
    soft: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppText)
                    Spacer(Modifier.height(5.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(50)).background(soft)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(role, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(bio, style = MaterialTheme.typography.bodySmall, color = AppMuted, lineHeight = 19.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                highlights.forEach { tag ->
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppSoftBlue)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(tag, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = AppNavy, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyRow(icon: ImageVector, title: String, caption: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(AppSoftGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AppText)
            Text(caption, style = MaterialTheme.typography.bodySmall, color = AppMuted, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppMuted, modifier = Modifier.weight(0.35f))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = AppText,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.65f)
        )
    }
}

/* ------------------------------------------------------------------ */
/*  Staggered entrance                                                 */
/* ------------------------------------------------------------------ */
@Composable
private fun Reveal(delayMs: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        progress.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 26f
        }
    ) { content() }
}