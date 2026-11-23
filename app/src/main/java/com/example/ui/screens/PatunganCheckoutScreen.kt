package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PatunganArea
import com.example.data.PatunganGroupEntity
import com.example.data.PatunganPaymentOption
import com.example.data.ProductEntity
import com.example.data.formatRupiah
import com.example.data.isPatunganAreaCompatible
import com.example.data.resolveUserPatunganArea
import com.example.ui.SisaEnakUiState
import com.example.ui.components.FoodProductImage
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
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextGray

/**
 * Layar 2: Checkout Patungan Gojek
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PatunganCheckoutScreen(
    product: ProductEntity,
    group: PatunganGroupEntity,
    uiState: SisaEnakUiState,
    isSoloFullMode: Boolean = false,
    onBack: () -> Unit,
    onSwitchUserLocationToArea: (PatunganArea) -> Unit,
    onPayPatungan: (userAreaDetail: String, userAddress: String, payment: PatunganPaymentOption) -> Unit
) {
    val groupArea = PatunganArea.entries.firstOrNull { it.areaName == group.areaName } ?: PatunganArea.DAGO
    val userArea = resolveUserPatunganArea(
        uiState.userPrefs.location,
        uiState.userPrefs.userLat,
        uiState.userPrefs.userLng
    )
    val isAreaCompatible = isSoloFullMode || isPatunganAreaCompatible(
        userLocationLabel = uiState.userPrefs.location,
        userLat = uiState.userPrefs.userLat,
        userLng = uiState.userPrefs.userLng,
        groupArea = groupArea
    )

    var selectedSubZone by remember(group.areaName) {
        mutableStateOf(groupArea.subZones.firstOrNull() ?: group.areaName)
    }
    var addressNote by remember(group.areaName) {
        mutableStateOf("${groupArea.subZones.firstOrNull() ?: group.areaName} No. 15")
    }
    var selectedPayment by remember { mutableStateOf(PatunganPaymentOption.QRIS) }

    val bagPrice = product.discountPrice
    val patunganShipping = group.patunganShippingRp
    val normalShipping = group.normalShippingRp
    val appliedShipping = if (isSoloFullMode) normalShipping else patunganShipping
    val totalPay = bagPrice + appliedShipping
    val walletBalance = uiState.userPrefs.walletBalanceRp
    val saldoEnough = selectedPayment != PatunganPaymentOption.SALDO_LUDES || walletBalance >= totalPay
    val existingMembers = group.members
    val canSubmit = isAreaCompatible && saldoEnough && product.stock > 0 && (isSoloFullMode || group.remainingSlots > 0)

    Scaffold(
        containerColor = BgLightGray,
        topBar = {
            SubScreenTopBar(
                title = if (isSoloFullMode) "Antar Langsung (Bayar Full)" else "Patungan Gojek - Hemat Ongkir",
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(
                color = BgWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(0.5.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PrimaryOrangeButton(
                        text = "BAYAR ${formatRupiah(totalPay)}",
                        onClick = { onPayPatungan(selectedSubZone, addressNote.trim(), selectedPayment) },
                        enabled = canSubmit,
                        testTag = "patungan_pay_button",
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    )
                }
            }
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
                // Card Toko & Produk
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FoodProductImage(
                            imageKey = product.imageKey,
                            customImageUri = product.customImageUri,
                            contentDescription = product.title,
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(shape = RoundedCornerShape(50), color = FreshGreenSoft) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = null,
                                        tint = FreshGreenDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${product.storeName} - ${product.storeAddress}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = FreshGreenDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextBlack
                            )
                            Text(
                                text = "Area ${group.areaName} • Sisa ${group.remainingSlots} orang lagi",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ActionOrangeDark
                            )
                        }
                    }
                }

                // Rincian Biaya & Info Ongkir Hemat
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Rincian Biaya",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Harga Kantong", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                            Text(
                                text = formatRupiah(bagPrice),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextBlack
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isSoloFullMode) "Ongkir Gojek Penuh (Langsung)" else "Ongkir Patungan (Bagi 5)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextGray
                                )
                                Text(
                                    text = if (isSoloFullMode) {
                                        "Driver antar langsung ke rumahmu tanpa tunggu kuota"
                                    } else {
                                        "1 driver antar 5 kantong ke komplek ${group.areaName}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FreshGreenDark
                                )
                            }
                            if (isSoloFullMode) {
                                Text(
                                    text = formatRupiah(normalShipping),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ActionOrangeDark
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatRupiah(normalShipping),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = TextGray
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatRupiah(patunganShipping),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = FreshGreenDark
                                    )
                                }
                            }
                        }

                        if (!isSoloFullMode) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FreshGreenSoft,
                                border = BorderStroke(1.dp, FreshGreenBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Hemat ongkir ${formatRupiah(normalShipping - patunganShipping)}! Dari ${formatRupiah(normalShipping)} jadi cuma ${formatRupiah(patunganShipping)}.",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FreshGreenDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = SurfaceBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Bayar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextBlack
                            )
                            Text(
                                text = formatRupiah(totalPay),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = ActionOrange
                            )
                        }
                    }
                }

                // Info Patungan: Daftar 5 Orang (Slot 1..5)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isSoloFullMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Group,
                                        contentDescription = null,
                                        tint = ActionOrangeDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Detail Patungan • Sisa ${group.remainingSlots} Orang Lagi",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextBlack
                                    )
                                }
                                Surface(shape = RoundedCornerShape(50), color = ActionOrangeSoft) {
                                    Text(
                                        text = "${group.filledSlots}/${group.maxSlots} Ikut",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ActionOrangeDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            val nextUserSlotIndex = existingMembers.size + 1
                            for (slot in 1..group.maxSlots) {
                                val member = existingMembers.getOrNull(slot - 1)
                                val isKamuSlot = member == null && slot == nextUserSlotIndex
                                val rowBg = when {
                                    member?.isCurrentUser == true || isKamuSlot -> ActionOrangeSoft
                                    member != null -> FreshGreenSoft
                                    else -> BgLightGray
                                }
                                val rowBorder = when {
                                    member?.isCurrentUser == true || isKamuSlot -> ActionOrangeBorder
                                    member != null -> FreshGreenBorder
                                    else -> SurfaceBorder
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = rowBg,
                                    border = BorderStroke(1.dp, rowBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = when {
                                                    isKamuSlot || member?.isCurrentUser == true -> ActionOrange
                                                    member != null -> FreshGreen
                                                    else -> TextGray.copy(alpha = 0.25f)
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$slot",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when {
                                                    member != null -> "$slot. ${member.aliasName} - ${member.areaDetail}"
                                                    isKamuSlot -> "$slot. Kamu ($selectedSubZone)"
                                                    else -> "$slot. Slot Kosong"
                                                },
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (member != null || isKamuSlot) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (member != null || isKamuSlot) TextBlack else TextGray
                                            )
                                        }
                                        if (isKamuSlot) {
                                            Text(
                                                text = "Slotmu",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = ActionOrangeDark
                                            )
                                        } else if (member != null) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Sudah ikut",
                                                tint = FreshGreenDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Pilih Sub-Area Komplek Kamu
                        Text(
                            text = "Alamat Antar Kamu (${group.areaName})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextBlack
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groupArea.subZones.forEach { zone ->
                                val selected = selectedSubZone == zone
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) FreshGreenSoft else BgLightGray,
                                    border = BorderStroke(1.dp, if (selected) FreshGreen else SurfaceBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            selectedSubZone = zone
                                            addressNote = "$zone No. 15"
                                        }
                                ) {
                                    Text(
                                        text = zone,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) FreshGreenDark else TextBlack,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = addressNote,
                            onValueChange = { addressNote = it.take(50) },
                            label = { Text("Patokan Rumah / Blok Komplek") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FreshGreen,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isAreaCompatible) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LiveRedSoft,
                                border = BorderStroke(1.dp, LiveRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.WarningAmber,
                                            contentDescription = null,
                                            tint = LiveRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Area ${userArea.areaName} di luar radius 1km ${group.areaName}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = LiveRed
                                        )
                                    }
                                    Text(
                                        text = "Aturan area: Jangan campur ${group.areaName} dengan ${userArea.areaName}. Ubah lokasi acuan ke ${group.areaName} untuk ikut patungan ini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextBlack
                                    )
                                    SecondaryGreenButton(
                                        text = "Pindah Lokasi ke ${group.areaName}",
                                        onClick = { onSwitchUserLocationToArea(groupArea) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Aturan Patungan
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BgLightGray,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Patungan hanya untuk 1 toko & 1 area (${group.areaName}, radius max 1km), maksimal ${group.maxSlots} orang.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }

                // Pilihan Pembayaran (Tanpa COD)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
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
                            Text(
                                text = "Pilihan Pembayaran",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextBlack
                            )
                            Surface(shape = RoundedCornerShape(50), color = LiveRedSoft) {
                                Text(
                                    text = "TIDAK ADA COD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = LiveRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        PatunganPaymentOption.entries.forEach { option ->
                            val isSelected = selectedPayment == option
                            val sub = if (option == PatunganPaymentOption.SALDO_LUDES) {
                                "Saldo tersedia: ${formatRupiah(walletBalance)}"
                            } else {
                                option.subtitle
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) ActionOrangeSoft else BgLightGray,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) ActionOrange else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedPayment = option }
                                    .testTag("patungan_pay_${option.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) ActionOrangeDark else TextGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextBlack
                                        )
                                        Text(
                                            text = sub,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (option == PatunganPaymentOption.SALDO_LUDES && !saldoEnough) LiveRed else TextGray
                                        )
                                    }
                                }
                            }
                        }

                        // Escrow Notice
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FreshGreenSoft,
                            border = BorderStroke(1.dp, FreshGreenBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = FreshGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Setelah bayar, uang ditahan aman di LUDES dan belum diteruskan ke warung maupun driver sebelum pesanan selesai.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FreshGreenDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Layar 3: Detail Patungan & Sisa Slot Peserta (tanpa pengaturan berangkat)
 */
@Composable
fun PatunganWaitingScreen(
    group: PatunganGroupEntity,
    onBack: () -> Unit,
    onShareSuccess: (String) -> Unit,
    onCancelPatungan: () -> Unit
) {
    val context = LocalContext.current
    val filled = group.filledSlots
    val maxSlots = group.maxSlots
    val missing = (maxSlots - filled).coerceAtLeast(0)
    val progress = (filled.toFloat() / maxSlots.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        containerColor = BgLightGray,
        topBar = {
            SubScreenTopBar(
                title = "Detail Patungan",
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
                // Hero Progress Card - Sisa Berapa Orang Lagi
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = ActionOrangeSoft) {
                                Text(
                                    text = if (missing > 0) "Sisa $missing orang lagi" else "Kuota Penuh",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ActionOrangeDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Surface(shape = RoundedCornerShape(50), color = FreshGreenSoft) {
                                Text(
                                    text = "Area ${group.areaName}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FreshGreenDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = if (missing > 0) {
                                "Sisa $missing orang lagi dari $maxSlots slot!"
                            } else {
                                "Kuota $filled/$maxSlots terpenuhi!"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextBlack
                        )

                        LinearProgressIndicator(
                            progress = { progress },
                            color = FreshGreen,
                            trackColor = SurfaceBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(50))
                        )

                        Text(
                            text = "$filled/$maxSlots orang sudah ikut patungan",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = FreshGreenDark
                        )
                    }
                }

                // Ringkasan Toko & Dana Ditahan
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = FreshGreenSoft,
                    border = BorderStroke(1.dp, FreshGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = FreshGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${group.storeName} • ${group.productTitle}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = FreshGreenDark
                            )
                            Text(
                                text = "Dana ${formatRupiah(group.userTotalPaidRp.takeIf { it > 0 } ?: (group.bagPriceRp + group.patunganShippingRp))} aman ditahan di LUDES.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FreshGreenDark
                            )
                        }
                    }
                }

                // Daftar Slot Peserta Saat Ini
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Peserta Patungan (${group.areaName}) • Sisa $missing Slot",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                        val members = group.members
                        for (slot in 1..maxSlots) {
                            val member = members.getOrNull(slot - 1)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = if (member?.isCurrentUser == true) ActionOrange else if (member != null) FreshGreen else TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (member != null) {
                                            "$slot. ${member.aliasName} - ${member.areaDetail}"
                                        } else {
                                            "$slot. Slot Kosong"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (member != null) FontWeight.SemiBold else FontWeight.Normal
                                        ),
                                        color = if (member != null) TextBlack else TextGray
                                    )
                                }
                                if (member?.isCurrentUser == true) {
                                    Surface(shape = RoundedCornerShape(50), color = ActionOrangeSoft) {
                                        Text(
                                            text = "Kamu",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ActionOrangeDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tombol BAGIKAN KE TEMAN - Hijau
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgWhite,
                    border = BorderStroke(1.dp, FreshGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ajak tetangga di ${group.areaName} biar ongkir makin murah",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = FreshGreenDark
                        )
                        Button(
                            onClick = {
                                val shareText = "Yuk ikut Patungan Gojek LUDES dari ${group.storeName} (${group.productTitle})! Ongkir cuma Rp3.000 ke area ${group.areaName}. Sisa $missing orang lagi!"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(intent, "Bagikan Patungan ke Teman"))
                                }.onFailure {
                                    onShareSuccess("Link undangan Patungan Gojek siap dibagikan!")
                                }
                                onShareSuccess("Undangan patungan untuk tetangga ${group.areaName} dibagikan!")
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FreshGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("patungan_share_button")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BAGIKAN KE TEMAN",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }

                // Tombol BATALKAN PATUNGAN - Abu-abu
                Button(
                    onClick = onCancelPatungan,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5E7EB),
                        contentColor = TextBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("patungan_cancel_button")
                ) {
                    Text(
                        text = "BATALKAN PATUNGAN (Uang Kembali ke Saldo LUDES)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
