package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DriverStage
import com.example.data.PatunganGroupEntity
import com.example.data.PatunganMember
import com.example.data.PatunganStatus
import com.example.data.ProductEntity
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
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextGray

/**
 * Dual delivery choice buttons under Beranda product cards:
 * 1. AMBIL SENDIRI - GRATIS (Green, walk icon)
 * 2. PATUNGAN GOJEK - Rp3.000 (Orange, motorcycle icon; turns gray "PATUNGAN PENUH" when 0 slots left)
 * Plus slot status line below: "4/5 orang sudah ikut - Sisa 1 Slot - Berangkat Jam 19.30"
 */
@Composable
fun ProductDeliveryChoiceSection(
    product: ProductEntity,
    patunganGroup: PatunganGroupEntity?,
    onAmbilSendiriClick: () -> Unit,
    onPatunganClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filled = patunganGroup?.filledSlots ?: 3
    val maxSlots = patunganGroup?.maxSlots ?: 5
    val remaining = (maxSlots - filled).coerceAtLeast(0)
    val departHour = patunganGroup?.departHour ?: "19.30"
    val userJoined = patunganGroup?.userJoined == true
    val isFull = remaining <= 0 || (patunganGroup != null && patunganGroup.status != PatunganStatus.OPEN.name && !userJoined)
    val stockAvailable = product.stock > 0
    val patunganEnabled = (stockAvailable || userJoined) && (!isFull || userJoined)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tombol 1: AMBIL SENDIRI - GRATIS (Hijau, ikon jalan kaki)
            Button(
                onClick = onAmbilSendiriClick,
                enabled = stockAvailable,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FreshGreen,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceBorder,
                    disabledContentColor = TextGray
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("ambil_sendiri_button_${product.id}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = "Ambil Sendiri",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "AMBIL SENDIRI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "GRATIS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1
                    )
                }
            }

            // Tombol 2: PATUNGAN GOJEK - Rp3.000 (Oranye, ikon motor; Abu-abu "PATUNGAN PENUH" jika penuh)
            Button(
                onClick = onPatunganClick,
                enabled = patunganEnabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFull && !userJoined) Color(0xFFD1D5DB) else ActionOrange,
                    contentColor = if (isFull && !userJoined) TextGray else Color.White,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = TextGray
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("patungan_gojek_button_${product.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.TwoWheeler,
                    contentDescription = "Patungan Gojek",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = when {
                            userJoined -> "STATUS PATUNGAN"
                            isFull -> "PATUNGAN PENUH"
                            else -> "PATUNGAN GOJEK"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            userJoined -> "$filled/$maxSlots Ikut • Lihat"
                            isFull -> "Slot 0/$maxSlots"
                            else -> "Rp3.000"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // Status slot Patungan di bawah tombol
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isFull && !userJoined) BgLightGray else ActionOrangeSoft,
            border = BorderStroke(
                1.dp,
                if (isFull && !userJoined) SurfaceBorder else ActionOrangeBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = patunganEnabled) { onPatunganClick() }
                .testTag("patungan_slot_status_${product.id}")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isFull && !userJoined) {
                        "$filled/$maxSlots orang sudah ikut - Slot 0/$maxSlots (PATUNGAN PENUH) - Berangkat Jam $departHour"
                    } else {
                        "$filled/$maxSlots orang sudah ikut - Sisa $remaining Slot - Berangkat Jam $departHour"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isFull && !userJoined) TextGray else ActionOrangeDark,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Interactive visual mini-map showing Store -> moving Driver scooter dot -> Complex delivery stops.
 */
@Composable
fun PatunganRouteMiniMap(
    storeName: String,
    areaName: String,
    members: List<PatunganMember>,
    driverStage: DriverStage,
    modifier: Modifier = Modifier
) {
    val sortedStops = members.sortedBy { it.distanceKmFromStore }
    val deliveredRatio = if (sortedStops.isEmpty()) 0f else {
        sortedStops.count { it.isDelivered }.toFloat() / sortedStops.size.toFloat()
    }
    val driverProgress = when (driverStage) {
        DriverStage.MENUJU_TOKO -> 0.14f
        DriverStage.AMBIL_KANTONG -> 0.32f
        DriverStage.DALAM_PERJALANAN -> (0.45f + deliveredRatio * 0.45f).coerceIn(0.45f, 0.92f)
        DriverStage.SAMPAI -> 1.0f
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(1.dp, FreshGreenBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Peta Rute • $storeName → Komplek $areaName",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FreshGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = RoundedCornerShape(50), color = BgWhite) {
                    Text(
                        text = "Searah ≤1 km",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = FreshGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F5E9))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle street grid
                    val gridColor = Color(0xFFD1FAE5)
                    for (i in 1..4) {
                        val y = h * (i / 5f)
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
                    }
                    for (i in 1..5) {
                        val x = w * (i / 6f)
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 2f)
                    }

                    val start = Offset(w * 0.12f, h * 0.68f)
                    val mid1 = Offset(w * 0.36f, h * 0.42f)
                    val mid2 = Offset(w * 0.62f, h * 0.56f)
                    val end = Offset(w * 0.88f, h * 0.28f)

                    val routePath = Path().apply {
                        moveTo(start.x, start.y)
                        lineTo(mid1.x, mid1.y)
                        lineTo(mid2.x, mid2.y)
                        lineTo(end.x, end.y)
                    }

                    // Road outline
                    drawPath(
                        path = routePath,
                        color = Color.White,
                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                    )
                    // Dashed green route
                    drawPath(
                        path = routePath,
                        color = FreshGreen,
                        style = Stroke(
                            width = 7f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                        )
                    )

                    // Stop dots
                    val stopPoints = listOf(
                        Offset(w * 0.46f, h * 0.48f),
                        Offset(w * 0.60f, h * 0.55f),
                        Offset(w * 0.72f, h * 0.44f),
                        Offset(w * 0.80f, h * 0.35f),
                        end
                    )
                    sortedStops.forEachIndexed { idx, member ->
                        val p = stopPoints[idx.coerceAtMost(stopPoints.lastIndex)]
                        val dotColor = when {
                            member.isDelivered -> FreshGreenDark
                            member.isCurrentUser -> ActionOrange
                            else -> Color(0xFF3B82F6)
                        }
                        drawCircle(Color.White, radius = 12f, center = p)
                        drawCircle(dotColor, radius = 8f, center = p)
                    }

                    // Store marker
                    drawCircle(Color.White, radius = 16f, center = start)
                    drawCircle(FreshGreenDark, radius = 11f, center = start)

                    // Moving driver dot interpolated along route
                    val driverOffset = when {
                        driverProgress <= 0.33f -> {
                            val t = driverProgress / 0.33f
                            Offset(
                                start.x + (mid1.x - start.x) * t,
                                start.y + (mid1.y - start.y) * t
                            )
                        }
                        driverProgress <= 0.66f -> {
                            val t = (driverProgress - 0.33f) / 0.33f
                            Offset(
                                mid1.x + (mid2.x - mid1.x) * t,
                                mid1.y + (mid2.y - mid1.y) * t
                            )
                        }
                        else -> {
                            val t = ((driverProgress - 0.66f) / 0.34f).coerceIn(0f, 1f)
                            Offset(
                                mid2.x + (end.x - mid2.x) * t,
                                mid2.y + (end.y - mid2.y) * t
                            )
                        }
                    }
                    drawCircle(ActionOrange.copy(alpha = 0.28f), radius = 24f, center = driverOffset)
                    drawCircle(Color.White, radius = 15f, center = driverOffset)
                    drawCircle(ActionOrange, radius = 11f, center = driverOffset)
                }

                // Store label badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = BgWhite.copy(alpha = 0.94f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = FreshGreenDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = storeName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                    }
                }

                // Driver moving badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ActionOrange,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TwoWheeler,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = driverStage.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Destination Komplek badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = BgWhite.copy(alpha = 0.94f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = ActionOrangeDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Komplek $areaName",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                    }
                }
            }
        }
    }
}
