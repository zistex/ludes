package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DriverStage
import com.example.data.PatunganGroupEntity
import com.example.data.PatunganStatus
import com.example.data.calculatePatunganSplit
import com.example.data.formatDistanceKmCompact
import com.example.data.formatRupiah
import com.example.ui.components.PatunganRouteMiniMap
import com.example.ui.components.PrimaryOrangeButton
import com.example.ui.components.SecondaryGreenButton
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.ActionOrangeBorder
import com.example.ui.theme.ActionOrangeDark
import com.example.ui.theme.ActionOrangeSoft
import com.example.ui.theme.BgLightGray
import com.example.ui.theme.BgWhite
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenBorder
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.LiveRed
import com.example.ui.theme.LiveRedSoft
import com.example.ui.theme.StarYellow
import com.example.ui.theme.StatusAmberSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextGray

/**
 * Layar 4: Patungan Berangkat - Status Driver
 */
@Composable
fun PatunganDriverStatusScreen(
    group: PatunganGroupEntity,
    onBack: () -> Unit,
    onAdvanceStage: () -> Unit,
    onOpenDriverScreen: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val stage = DriverStage.entries.firstOrNull { it.name == group.driverStage } ?: DriverStage.MENUJU_TOKO
    val sortedStops = remember(group.membersJson) {
        group.members.sortedBy { it.distanceKmFromStore }
    }
    val userStopPos = remember(sortedStops) {
        val idx = sortedStops.indexOfFirst { it.isCurrentUser }
        if (idx >= 0) idx + 1 else sortedStops.size.coerceAtLeast(1)
    }
    val totalStops = sortedStops.size.coerceAtLeast(1)
    val split = remember(group.bagPriceRp, group.patunganShippingRp) {
        calculatePatunganSplit(group.bagPriceRp, group.patunganShippingRp)
    }

    var showRouteDialog by remember { mutableStateOf(false) }
    var showDriverContactDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgLightGray,
        topBar = {
            SubScreenTopBar(
                title = "Patungan Berangkat!",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card Info Driver: Pak Agus - Motor B 1234 XYZ - Rating 4.9 - Foto Driver
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(FreshGreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_avatar_siti),
                                    contentDescription = "Foto Driver ${group.driverName}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(FreshGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SportsMotorsports,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.driverName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = TextBlack
                                )
                                Text(
                                    text = group.driverPlate,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextGray
                                )
                            }
                            Surface(shape = RoundedCornerShape(50), color = StatusAmberSoft) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rating Driver",
                                        tint = StarYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${group.driverRating}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = TextBlack
                                    )
                                }
                            }
                        }

                        // Info urutan & Estimasi
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FreshGreenSoft,
                            border = BorderStroke(1.dp, FreshGreenBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (group.isSoloFallbackDelivery) {
                                        "Antar langsung 1 tujuan dari ${group.storeName}."
                                    } else {
                                        "Kantong kamu No. $userStopPos dari $totalStops. Driver antar searah 1 rute."
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = FreshGreenDark
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = FreshGreenDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (stage == DriverStage.SAMPAI) {
                                            "Sampai! Kantong sudah diterima."
                                        } else {
                                            "Sampai ${group.etaMinutes} menit lagi"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = FreshGreenDark
                                    )
                                }
                            }
                        }

                        // Status 4 Tahap Stepper
                        Text(
                            text = "Status Pengantaran (4 Tahap)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DriverStage.entries.forEach { step ->
                                val isDone = stage.stepIndex > step.stepIndex || stage == DriverStage.SAMPAI
                                val isCurrent = stage == step
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isCurrent -> ActionOrangeSoft
                                        isDone -> FreshGreenSoft
                                        else -> BgLightGray
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            isCurrent -> ActionOrangeBorder
                                            isDone -> FreshGreenBorder
                                            else -> SurfaceBorder
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = when {
                                                    isDone -> FreshGreen
                                                    isCurrent -> ActionOrange
                                                    else -> TextGray.copy(alpha = 0.4f)
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "${step.stepIndex + 1}. ${step.label}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isCurrent || isDone) TextBlack else TextGray
                                            )
                                        }
                                        if (isCurrent) {
                                            Text(
                                                text = "Saat Ini",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = ActionOrangeDark
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (stage != DriverStage.SAMPAI) {
                            SecondaryGreenButton(
                                text = "Simulasi Lanjut Tahap Driver",
                                onClick = onAdvanceStage,
                                testTag = "patungan_advance_driver_stage",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Peta Kecil: Titik driver bergerak dari toko ke komplek
                PatunganRouteMiniMap(
                    storeName = group.storeName,
                    areaName = group.areaName,
                    members = sortedStops,
                    driverStage = stage
                )

                // Tombol HUBUNGI DRIVER & LIHAT RUTE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showDriverContactDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FreshGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("patungan_contact_driver_button")
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HUBUNGI DRIVER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { showRouteDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionOrange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("patungan_view_route_button")
                    ) {
                        Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIHAT RUTE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1
                        )
                    }
                }

                // Aturan Pembagian Uang Patungan (Section 7)
                PatunganMoneySplitCard(
                    split = split,
                    isSettled = stage == DriverStage.SAMPAI
                )

                // Pintasan ke Layar Driver / Kurir Patungan (Section 6)
                OutlinedButton(
                    onClick = onOpenDriverScreen,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, FreshGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("open_driver_patungan_from_status")
                ) {
                    Icon(Icons.Filled.TwoWheeler, contentDescription = null, tint = FreshGreenDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buka Layar Driver Patungan (${sortedStops.size} Kantong)",
                        style = MaterialTheme.typography.labelLarge,
                        color = FreshGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showRouteDialog) {
        AlertDialog(
            onDismissRequest = { showRouteDialog = false },
            containerColor = BgWhite,
            title = { Text("Rute Antar Searah • ${group.areaName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Titik Ambil: ${group.storeName} (${group.storeAddress})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = FreshGreenDark
                    )
                    sortedStops.forEachIndexed { idx, member ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. ${member.aliasName} - ${member.addressLabel}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (member.isCurrentUser) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (member.isCurrentUser) ActionOrangeDark else TextBlack,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (member.isDelivered) "Sampai" else formatDistanceKmCompact(member.distanceKmFromStore),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (member.isDelivered) FreshGreenDark else TextGray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRouteDialog = false }) { Text("Tutup") }
            }
        )
    }

    if (showDriverContactDialog) {
        AlertDialog(
            onDismissRequest = { showDriverContactDialog = false },
            containerColor = BgWhite,
            title = { Text("Hubungi ${group.driverName}") },
            text = {
                Text("${group.driverPlate} • Rating ${group.driverRating}\nDriver sedang mengantar ${sortedStops.size} kantong searah ke komplek ${group.areaName}.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDriverContactDialog = false
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:081299887766"))
                        runCatching { context.startActivity(dialIntent) }
                            .onFailure { onShowMessage("Nomor Driver ${group.driverName}: 0812-9988-7766") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Telepon Driver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDriverContactDialog = false }) { Text("Tutup") }
            }
        )
    }
}

/**
 * Layar 5: Jika Patungan Gagal Penuh (< 3 orang saat jam batas)
 */
@Composable
fun PatunganFailedScreen(
    group: PatunganGroupEntity,
    onBack: () -> Unit,
    onKeepSoloDelivery: () -> Unit,
    onSwitchToPickup: () -> Unit,
    onCancelAll: () -> Unit
) {
    val count = group.filledSlots.coerceAtMost(2).coerceAtLeast(1)
    val bagPrice = group.bagPriceRp
    val extraDelivery = group.normalShippingRp - group.patunganShippingRp // Rp12.000
    val soloTotal = bagPrice + group.normalShippingRp // Rp25.000 jika kantong Rp10.000
    val paidSoFar = group.userTotalPaidRp.takeIf { it > 0 } ?: (bagPrice + group.patunganShippingRp)

    Scaffold(
        containerColor = BgLightGray,
        topBar = {
            SubScreenTopBar(
                title = "Patungan Belum Penuh",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LiveRedSoft,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.WarningAmber,
                                    contentDescription = null,
                                    tint = LiveRed,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Text(
                            text = "Patungan Belum Penuh",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextBlack
                        )
                        Text(
                            text = "Cuma $count orang ikut. Mau tetap diantar?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextGray
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BgLightGray,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${group.storeName} • ${group.productTitle}\nMinimal 3 orang agar driver patungan bisa berangkat. Dana ${formatRupiah(paidSoFar)} masih ditahan aman di LUDES.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Tombol 1: TETAP DIANTAR SENDIRI - Oranye
                Button(
                    onClick = onKeepSoloDelivery,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .testTag("patungan_failed_solo_button")
                ) {
                    Icon(Icons.Filled.TwoWheeler, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TETAP DIANTAR SENDIRI",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Tambah ${formatRupiah(extraDelivery)}, Total jadi ${formatRupiah(soloTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.94f)
                        )
                    }
                }

                // Tombol 2: AMBIL SENDIRI - Hijau
                Button(
                    onClick = onSwitchToPickup,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FreshGreen,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .testTag("patungan_failed_pickup_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AMBIL SENDIRI",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Uang ongkir ${formatRupiah(group.patunganShippingRp)} kembali jadi Saldo LUDES",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.94f)
                        )
                    }
                }

                // Tombol 3: BATALKAN SEMUA - Abu-abu
                Button(
                    onClick = onCancelAll,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5E7EB),
                        contentColor = TextBlack
                    ),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("patungan_failed_cancel_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BATALKAN SEMUA",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Uang ${formatRupiah(paidSoFar)} kembali jadi Saldo LUDES",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Layar 6 & 7: Layar Khusus Driver / Kurir Patungan
 */
@Composable
fun DriverPatunganScreen(
    groups: List<PatunganGroupEntity>,
    onBack: () -> Unit,
    onMarkPickedUpAtStore: (String) -> Unit,
    onMarkMemberDelivered: (groupId: String, slotIndex: Int) -> Unit
) {
    val activeCandidates = remember(groups) {
        groups.sortedWith(
            compareByDescending<PatunganGroupEntity> { it.status == PatunganStatus.BERANGKAT.name }
                .thenByDescending { it.filledSlots }
        )
    }
    var selectedGroupId by remember(activeCandidates) {
        mutableStateOf(activeCandidates.firstOrNull()?.id.orEmpty())
    }
    val group = activeCandidates.firstOrNull { it.id == selectedGroupId } ?: activeCandidates.firstOrNull()

    Scaffold(
        containerColor = BgLightGray,
        topBar = {
            SubScreenTopBar(
                title = "Mode Driver / Kurir Patungan",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        if (group == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada order patungan aktif.", color = TextGray)
            }
            return@Scaffold
        }

        val sortedStops = remember(group.membersJson) {
            group.members.sortedBy { it.distanceKmFromStore }
        }
        val stage = DriverStage.entries.firstOrNull { it.name == group.driverStage } ?: DriverStage.MENUJU_TOKO
        val allDelivered = sortedStops.isNotEmpty() && sortedStops.all { it.isDelivered }
        val split = remember(group.bagPriceRp, group.patunganShippingRp) {
            calculatePatunganSplit(group.bagPriceRp, group.patunganShippingRp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Switcher antar order patungan
                if (activeCandidates.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeCandidates.take(4).forEach { candidate ->
                            val selected = candidate.id == group.id
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) ActionOrange else BgWhite,
                                border = BorderStroke(1.dp, if (selected) ActionOrange else SurfaceBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { selectedGroupId = candidate.id }
                            ) {
                                Text(
                                    text = "${candidate.storeName} (${candidate.filledSlots}/${candidate.maxSlots})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selected) Color.White else TextBlack,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Header Order Patungan - 5 Kantong
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order Patungan - ${sortedStops.size} Kantong",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextBlack
                            )
                            Surface(shape = RoundedCornerShape(50), color = FreshGreenSoft) {
                                Text(
                                    text = "Area ${group.areaName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FreshGreenDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FreshGreenSoft,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Storefront, contentDescription = null, tint = FreshGreenDark)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.storeName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextBlack
                                )
                                Text(
                                    text = "${group.storeAddress} • Jam Ambil ${group.pickupSlot}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                        }

                        // Tombol Besar Hijau: SUDAH AMBIL DI TOKO
                        Button(
                            onClick = { onMarkPickedUpAtStore(group.id) },
                            enabled = !group.hasPickedUpAtStore && !allDelivered,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FreshGreen,
                                contentColor = Color.White,
                                disabledContainerColor = FreshGreenSoft,
                                disabledContentColor = FreshGreenDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("driver_picked_up_button")
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (group.hasPickedUpAtStore || allDelivered) {
                                    "SUDAH DIAMBIL DI TOKO"
                                } else {
                                    "SUDAH AMBIL DI TOKO"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }

                // Peta Rute Searah (dari rumah paling dekat ke paling jauh)
                PatunganRouteMiniMap(
                    storeName = group.storeName,
                    areaName = group.areaName,
                    members = sortedStops,
                    driverStage = stage
                )

                // Daftar Kantong dengan Label Nama + Urutan Antar Searah
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = ActionOrangeDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Urutan Antar Searah (Terdekat → Terjauh)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextBlack
                            )
                        }

                        sortedStops.forEachIndexed { idx, member ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (member.isDelivered) FreshGreenSoft else BgLightGray,
                                border = BorderStroke(
                                    1.dp,
                                    if (member.isDelivered) FreshGreenBorder else SurfaceBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (member.isDelivered) FreshGreen else ActionOrange,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${idx + 1}",
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${idx + 1}. ${member.aliasName} - ${member.addressLabel}",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = TextBlack,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Label Kantong #${idx + 1} • Jarak ${formatDistanceKmCompact(member.distanceKmFromStore)} dari toko",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextGray
                                                )
                                            }
                                        }
                                    }

                                    // Tombol Besar per Rumah: SUDAH DIANTAR - Nama
                                    Button(
                                        onClick = { onMarkMemberDelivered(group.id, member.slotIndex) },
                                        enabled = !member.isDelivered,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ActionOrange,
                                            contentColor = Color.White,
                                            disabledContainerColor = FreshGreen,
                                            disabledContentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("driver_delivered_${member.slotIndex}")
                                    ) {
                                        Icon(
                                            imageVector = if (member.isDelivered) Icons.Filled.CheckCircle else Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (member.isDelivered) {
                                                "SUDAH DIANTAR - ${member.aliasName}"
                                            } else {
                                                "SUDAH DIANTAR - ${member.aliasName}"
                                            },
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Banner Selesai + Pendapatan Driver setelah semua diantar
                if (allDelivered) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FreshGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("driver_patungan_completed_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Selesai! Kamu dapat Rp15.000 + Bonus Rp2.500",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Semua ${sortedStops.size} kantong sudah berstatus SUDAH DIANTAR. Dana diteruskan H+1 ke warung dan driver.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.94f)
                            )
                        }
                    }
                }

                // Rincian Pembagian Otomatis Setelah Selesai (Section 7)
                PatunganMoneySplitCard(
                    split = split,
                    isSettled = allDelivered
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PatunganMoneySplitCard(
    split: com.example.data.PatunganSplitBreakdown,
    isSettled: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgWhite,
        border = BorderStroke(1.dp, if (isSettled) FreshGreenBorder else SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = FreshGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pembagian Uang Patungan (Per Orang)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextBlack
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSettled) FreshGreenSoft else StatusAmberSoft
                ) {
                    Text(
                        text = if (isSettled) "Diteruskan H+1" else "Ditahan LUDES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSettled) FreshGreenDark else ActionOrangeDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Dibayar User", style = MaterialTheme.typography.bodySmall, color = TextGray)
                Text(
                    text = formatRupiah(split.userTotalPaidRp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextBlack
                )
            }
            HorizontalDivider(color = SurfaceBorder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Warung dapat (dari ${formatRupiah(split.bagPriceRp)}, fee LUDES 10% dipotong)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRupiah(split.merchantReceivesRp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FreshGreenDark
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Driver dapat (dari ongkir patungan ${formatRupiah(split.patunganShippingRp)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRupiah(split.driverReceivesRp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FreshGreenDark
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LUDES (${formatRupiah(split.ludesBagFeeRp)} fee kantong + ${formatRupiah(split.ludesPatunganFeeRp)} atur patungan)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRupiah(split.ludesTotalFeeRp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ActionOrangeDark
                )
            }
            Text(
                text = "Dana diteruskan H+1 ke warung dan driver setelah semua status SUDAH DIANTAR.",
                style = MaterialTheme.typography.labelSmall,
                color = FreshGreenDark
            )
        }
    }
}
