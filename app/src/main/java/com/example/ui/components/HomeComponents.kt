package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ProductEntity
import com.example.data.formatCompactCount
import com.example.data.formatRupiah
import com.example.ui.SisaEnakUiState
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
import com.example.ui.theme.StarYellow
import com.example.ui.theme.StatusAmberSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextGray
import kotlinx.coroutines.delay

fun computeDiscountPercent(product: ProductEntity): Int {
    return if (product.originalPrice > 0 && product.discountPrice < product.originalPrice) {
        (((product.originalPrice - product.discountPrice) * 100.0) / product.originalPrice)
            .toInt()
            .coerceIn(1, 99)
    } else {
        0
    }
}

/**
 * GoFood-inspired Top Emerald Backdrop with Floating Search Pill & Overlapping Hero Promo Slider
 * (replaces the previous Saldo LUDES area now housed in the top navbar).
 */
@Composable
fun GoFoodHeroHeaderWithSlider(
    uiState: SisaEnakUiState,
    onOpenSearch: () -> Unit,
    onClickPromoSlide: () -> Unit,
    onClickWalletSlide: () -> Unit,
    onClickLiveSlide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Curved emerald gradient backdrop behind greeting, search pill, and top half of hero slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(198.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FreshGreenDark,
                            Color(0xFF16A34A),
                            FreshGreen
                        )
                    )
                )
        ) {
            // Subtle decorative atmosphere circles
            Box(
                modifier = Modifier
                    .size(134.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 36.dp, y = (-32).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            )
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-24).dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            // Contextual greeting & pickup-only badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stok lebih mitra, hemat sebelum tutup!",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.20f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "0 Ongkir",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GoFood-style Floating Search Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = BgWhite,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 50.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { onOpenSearch() }
                    .testTag("home_search_trigger")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buka pencarian",
                        tint = ActionOrange,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Lagi pengen roti, nasi, atau jajanan?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ActionOrangeSoft,
                        border = BorderStroke(1.dp, ActionOrangeBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Filter pencarian",
                                tint = ActionOrangeDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Filter",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ActionOrangeDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Slider Geser overlapping the curved emerald header bottom
            GoFoodPromoBannerCarousel(
                uiState = uiState,
                onClickPromoSlide = onClickPromoSlide,
                onClickWalletSlide = onClickWalletSlide,
                onClickLiveSlide = onClickLiveSlide
            )
        }
    }
}

/**
 * GoFood-style Promo Carousel Banner with persuasive partner surplus-stock branding
 * (mitra berstok lebih, jelang tutup toko & sebelum expire).
 */
@Composable
fun GoFoodPromoBannerCarousel(
    uiState: SisaEnakUiState,
    onClickPromoSlide: () -> Unit,
    onClickWalletSlide: () -> Unit,
    onClickLiveSlide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            delay(5000)
            val next = (pagerState.currentPage + 1) % pageCount
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_banner_pager")
        ) { page ->
            when (page) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 164.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ActionOrange, ActionOrangeDark, Color(0xFFC2410C))
                                )
                            )
                            .clickable { onClickPromoSlide() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(118.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 32.dp, y = (-32).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = "MITRA STOK LEBIH • JELANG TUTUP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Fresh Jelang Tutup Toko,\nDiskon s.d. 70%!",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 22.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = "Kerjasama resmi dengan penjual yang punya stok lebih sebelum tutup. Tetap fresh, 0 mubazir!",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                    color = Color.White.copy(alpha = 0.94f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(shape = RoundedCornerShape(50), color = Color.White) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Ambil Sebelum Ludes",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = ActionOrangeDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = ActionOrangeDark,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(94.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_food_bakery),
                                    contentDescription = "Promo Mitra Jelang Tutup",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = FreshGreen,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(5.dp)
                                ) {
                                    Text(
                                        text = "70% OFF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val liveCount = uiState.liveProducts.count { it.stock > 0 }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 164.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                                )
                            )
                            .clickable { onClickLiveSlide() }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(shape = RoundedCornerShape(50), color = LiveRed) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LiveTv,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "$liveCount MITRA LIVE • SEBELUM EXPIRE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Stok Mendekati Batas Jual,\nKualitas Masih Prima!",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 22.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = "Cek langsung kondisi makanan di dapur penjual sebelum masa simpan habis hari ini.",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(shape = RoundedCornerShape(50), color = ActionOrange) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Intip Live Mitra",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(94.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_food_nasi),
                                    contentDescription = "Live Warung Mitra",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = LiveRed,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(5.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.55f)),
                        shadowElevation = 4.dp,
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 164.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF065F46), FreshGreenDark, Color(0xFF16A34A))
                                    )
                                )
                                .clickable { onClickWalletSlide() }
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "KOLABORASI PENJUAL LOKAL • ANTI RUGI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bantu Warung Habiskan Stok,\nDompetmu Ikut Hemat!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 22.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Setiap transaksi menyelamatkan stok berlebih mitra jelang tutup. Saldo ${formatRupiah(uiState.userPrefs.walletBalanceRp)} siap pakai.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                color = Color.White.copy(alpha = 0.92f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(shape = RoundedCornerShape(50), color = Color.White) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pakai Saldo LUDES",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = FreshGreenDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = FreshGreenDark,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 7.dp,
                    label = "promoBannerIndicator"
                )
                Box(
                    modifier = Modifier
                        .height(7.dp)
                        .width(width)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) ActionOrange else SurfaceBorder)
                )
            }
        }
    }
}

/**
 * Horizontal Swipe Card 1: Featured GoFood-style Wide Card (224.dp) for Live / Hot Deals.
 */
@Composable
fun HorizontalFeaturedSwipeCard(
    product: ProductEntity,
    onCardClick: () -> Unit,
    onBuyClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountPct = computeDiscountPercent(product)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgWhite,
        border = BorderStroke(1.dp, SurfaceBorder),
        shadowElevation = 2.dp,
        modifier = modifier
            .width(224.dp)
            .testTag("swipe_live_card_${product.id}")
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCardClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
            ) {
                FoodProductImage(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Live + Discount badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (product.isLive) {
                        Surface(shape = RoundedCornerShape(50), color = LiveRed) {
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (discountPct > 0) {
                        Surface(shape = RoundedCornerShape(50), color = ActionOrange) {
                            Text(
                                text = "-$discountPct%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Top-right Favorite button
                Surface(
                    shape = CircleShape,
                    color = BgWhite.copy(alpha = 0.92f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (product.isFavorite) LiveRed else TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom-left Remaining stock
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xCC111827),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (product.stock > 0) "Sisa ${product.stock} kantong" else "Habis",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(11.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.storeName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = FreshGreenDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", product.rating),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextBlack,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "${product.distanceText} • Ambil ${product.pickupWindow}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(9.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatRupiah(product.originalPrice),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = TextGray
                        )
                        Text(
                            text = formatRupiah(product.discountPrice),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ActionOrange
                        )
                    }

                    Button(
                        onClick = onBuyClick,
                        enabled = product.stock > 0,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionOrange,
                            contentColor = Color.White,
                            disabledContainerColor = SurfaceBorder,
                            disabledContentColor = TextGray
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 36.dp)
                    ) {
                        Text(
                            text = if (product.stock > 0) "Beli" else "Habis",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Swipe Card 2: Compact GoFood-style Square-Top Card (164.dp) for Nearby Picks.
 */
@Composable
fun HorizontalCompactSwipeCard(
    product: ProductEntity,
    onCardClick: () -> Unit,
    onBuyClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountPct = computeDiscountPercent(product)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgWhite,
        border = BorderStroke(1.dp, SurfaceBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .width(164.dp)
            .testTag("swipe_nearby_card_${product.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
            ) {
                FoodProductImage(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )

                if (discountPct > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ActionOrange,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(7.dp)
                    ) {
                        Text(
                            text = "Hemat $discountPct%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = BgWhite.copy(alpha = 0.90f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (product.isFavorite) LiveRed else TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = FreshGreen,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(7.dp)
                ) {
                    Text(
                        text = product.distanceText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.storeName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FreshGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextBlack,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", product.rating)} • Sisa ${product.stock}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextGray,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatRupiah(product.discountPrice),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ActionOrange
                        )
                        Text(
                            text = formatRupiah(product.originalPrice),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = TextGray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (product.stock > 0) ActionOrangeSoft else BgLightGray,
                        border = BorderStroke(
                            1.dp,
                            if (product.stock > 0) ActionOrange else SurfaceBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = product.stock > 0) { onBuyClick() }
                    ) {
                        Text(
                            text = if (product.stock > 0) "Pesan" else "Habis",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (product.stock > 0) ActionOrangeDark else TextGray,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rigid Listing Mode A (Default GoFood Merchant Row):
 * Fixed uniform horizontal split row with 108.dp square photo on left and strict metadata hierarchy on right.
 */
@Composable
fun RigidListProductCard(
    product: ProductEntity,
    onCardClick: () -> Unit,
    onBuyClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountPct = computeDiscountPercent(product)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgWhite,
        border = BorderStroke(1.dp, SurfaceBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("food_card_${product.id}")
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed rigid square food thumbnail
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                FoodProductImage(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Discount or LIVE pill
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.isLive) {
                        Surface(shape = RoundedCornerShape(50), color = LiveRed) {
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (discountPct > 0) {
                        Surface(shape = RoundedCornerShape(50), color = ActionOrange) {
                            Text(
                                text = "-$discountPct%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Bottom-left stock indicator
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (product.stock > 0) Color(0xCC111827) else TextGray,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (product.stock > 0) "Sisa ${product.stock}" else "Habis",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Fixed rigid right column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.storeName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = FreshGreenDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(shape = RoundedCornerShape(50), color = StatusAmberSoft) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = StarYellow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", product.rating),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = TextBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .testTag("favorite_heart_${product.id}")
                            .clickable { onToggleFavorite() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (product.isFavorite) LiveRed else TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextBlack,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = product.distanceText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextGray,
                            maxLines = 1
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = product.pickupWindow,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextGray,
                            maxLines = 1
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatCompactCount(product.viewCount),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextGray,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.8.dp, color = SurfaceBorder.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatRupiah(product.discountPrice),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ActionOrange
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatRupiah(product.originalPrice),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = TextGray
                        )
                    }

                    Button(
                        onClick = onBuyClick,
                        enabled = product.stock > 0,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionOrange,
                            contentColor = Color.White,
                            disabledContainerColor = SurfaceBorder,
                            disabledContentColor = TextGray
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .testTag("buy_button_${product.id}")
                    ) {
                        Text(
                            text = if (product.stock > 0) "BELI" else "HABIS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rigid Listing Mode B (2-Column Rigid Uniform Grid Card):
 * Strict equal-height 2-column grid cells with consistent aspect ratio, 2-line title, and bottom CTA.
 */
@Composable
fun RigidGridProductCard(
    product: ProductEntity,
    onCardClick: () -> Unit,
    onBuyClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountPct = computeDiscountPercent(product)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgWhite,
        border = BorderStroke(1.dp, SurfaceBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("food_card_${product.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                FoodProductImage(
                    imageKey = product.imageKey,
                    customImageUri = product.customImageUri,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(7.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.isLive) {
                        Surface(shape = RoundedCornerShape(50), color = LiveRed) {
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (discountPct > 0) {
                        Surface(shape = RoundedCornerShape(50), color = ActionOrange) {
                            Text(
                                text = "-$discountPct%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = BgWhite.copy(alpha = 0.90f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(34.dp)
                        .testTag("favorite_heart_${product.id}")
                        .clip(CircleShape)
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (product.isFavorite) LiveRed else TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = FreshGreen,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(7.dp)
                ) {
                    Text(
                        text = "${product.distanceText} • Sisa ${product.stock}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.storeName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FreshGreenDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", product.rating),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextBlack,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ambil ${product.pickupWindow}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatRupiah(product.originalPrice),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = TextGray
                        )
                        Text(
                            text = formatRupiah(product.discountPrice),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = ActionOrange
                        )
                    }

                    Button(
                        onClick = onBuyClick,
                        enabled = product.stock > 0,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionOrange,
                            contentColor = Color.White,
                            disabledContainerColor = SurfaceBorder,
                            disabledContentColor = TextGray
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .testTag("buy_button_${product.id}")
                    ) {
                        Text(
                            text = if (product.stock > 0) "BELI" else "HABIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }
    }
}

