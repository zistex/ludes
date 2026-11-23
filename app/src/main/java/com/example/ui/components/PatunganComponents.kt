package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PatunganGroupEntity
import com.example.data.PatunganStatus
import com.example.data.ProductEntity
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.ActionOrangeBorder
import com.example.ui.theme.ActionOrangeDark
import com.example.ui.theme.ActionOrangeSoft
import com.example.ui.theme.BgLightGray
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextGray

/**
 * Dual delivery choice helper (without departure schedule or maps).
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
                            userJoined -> "DETAIL PATUNGAN"
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
                            userJoined -> "$filled/$maxSlots Ikut • Sisa $remaining"
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
                        "$filled/$maxSlots orang sudah ikut • Patungan penuh"
                    } else {
                        "$filled/$maxSlots orang sudah ikut • Sisa $remaining orang lagi"
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
