package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FoodCategory
import com.example.data.ProductEntity
import com.example.data.parseDistanceKm
import com.example.ui.SisaEnakUiState
import com.example.ui.components.GoFoodPromoBannerCarousel
import com.example.ui.components.GoFoodTopHeaderSection
import com.example.ui.components.HorizontalCompactSwipeCard
import com.example.ui.components.HorizontalFeaturedSwipeCard
import com.example.ui.components.RigidGridProductCard
import com.example.ui.components.RigidListProductCard
import com.example.ui.components.computeDiscountPercent
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.ActionOrangeDark
import com.example.ui.theme.ActionOrangeSoft
import com.example.ui.theme.BgLightGray
import com.example.ui.theme.BgWhite
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextGray

private enum class RigidQuickFilter(val label: String) {
    SEMUA("Semua"),
    TERDEKAT("Terdekat"),
    DISKON_BESAR("Diskon 50%+"),
    HEMAT_11RB("≤ Rp11rb"),
    SEDANG_LIVE("Sedang Live"),
    STOK_BANYAK("Stok Banyak")
}

private enum class RigidLayoutMode {
    LIST,
    GRID
}

@Composable
fun HomeScreen(
    uiState: SisaEnakUiState,
    onOpenSearch: () -> Unit,
    onToggleCategory: (FoodCategory) -> Unit,
    onOpenProductDetail: (ProductEntity) -> Unit,
    onQuickBuy: (ProductEntity) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenLiveTab: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quickFilter by rememberSaveable { mutableStateOf(RigidQuickFilter.SEMUA) }
    var rigidLayoutMode by rememberSaveable { mutableStateOf(RigidLayoutMode.LIST) }

    val baseProducts = uiState.products

    // Horizontal Carousel 1: Live & Hot Deals
    val liveAndHotProducts = remember(baseProducts) {
        val live = baseProducts.filter { it.isLive && it.stock > 0 }
        if (live.isNotEmpty()) live else baseProducts.sortedByDescending { computeDiscountPercent(it) }.take(5)
    }

    // Horizontal Carousel 2: Nearest to user's selected location
    val nearbyProducts = remember(baseProducts) {
        baseProducts.sortedWith(
            compareBy<ProductEntity> { parseDistanceKm(it.distanceText) }
                .thenByDescending { it.stock }
        )
    }

    // Rigid Listing below: applies quickFilter
    val rigidProducts = remember(baseProducts, quickFilter) {
        when (quickFilter) {
            RigidQuickFilter.SEMUA -> baseProducts
            RigidQuickFilter.TERDEKAT -> baseProducts.sortedBy { parseDistanceKm(it.distanceText) }
            RigidQuickFilter.DISKON_BESAR -> baseProducts.filter { computeDiscountPercent(it) >= 50 }
                .sortedByDescending { computeDiscountPercent(it) }
            RigidQuickFilter.HEMAT_11RB -> baseProducts.filter { it.discountPrice <= 11000 }
                .sortedBy { it.discountPrice }
            RigidQuickFilter.SEDANG_LIVE -> baseProducts.filter { it.isLive }
            RigidQuickFilter.STOK_BANYAK -> baseProducts.sortedByDescending { it.stock }
        }
    }

    val liveRowState = rememberLazyListState()
    val nearbyRowState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgLightGray),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. GoFood-Style Layered Emerald Header + Floating Search + Wallet/Impact Strip
            item(key = "header_hero") {
                GoFoodTopHeaderSection(
                    uiState = uiState,
                    onOpenSearch = onOpenSearch,
                    onOpenWallet = onOpenWallet
                )
            }

            // 2. Swipeable Persuasive Branding & Promo Banner Carousel
            item(key = "promo_carousel") {
                GoFoodPromoBannerCarousel(
                    uiState = uiState,
                    onClickPromoSlide = {
                        val firstMatch = uiState.products.firstOrNull { it.stock > 0 }
                        if (firstMatch != null) onOpenProductDetail(firstMatch)
                    },
                    onClickWalletSlide = onOpenWallet,
                    onClickLiveSlide = onOpenLiveTab
                )
            }

            // 4. Horizontal Swipe Listing #1: Sedang Live & Rebutan (Wide GoFood Featured Cards)
            if (liveAndHotProducts.isNotEmpty()) {
                item(key = "section_live_swipe") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GoFoodSectionHeader(
                            title = "Sedang Live & Rebutan",
                            subtitle = "Intip kondisi makanan real-time sebelum ludes",
                            leadingBadge = {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(LiveRed)
                                )
                            },
                            actionLabel = "Lihat Live",
                            onActionClick = onOpenLiveTab
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            state = liveRowState,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = liveRowState),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(liveAndHotProducts, key = { "live_${it.id}" }) { product ->
                                HorizontalFeaturedSwipeCard(
                                    product = product,
                                    onCardClick = { onOpenProductDetail(product) },
                                    onBuyClick = { onQuickBuy(product) },
                                    onToggleFavorite = { onToggleFavorite(product.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Horizontal Swipe Listing #2: Terdekat dari Lokasimu (Compact GoFood Swipe Cards)
            if (nearbyProducts.isNotEmpty()) {
                item(key = "section_nearby_swipe") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GoFoodSectionHeader(
                            title = "Terdekat dari Lokasimu",
                            subtitle = "Sekitar ${uiState.userPrefs.location} • Ambil langsung",
                            leadingBadge = {
                                Icon(
                                    imageVector = Icons.Filled.NearMe,
                                    contentDescription = null,
                                    tint = FreshGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            actionLabel = "Peta & Filter",
                            onActionClick = onOpenSearch
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            state = nearbyRowState,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = nearbyRowState),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(nearbyProducts, key = { "nearby_${it.id}" }) { product ->
                                HorizontalCompactSwipeCard(
                                    product = product,
                                    onCardClick = { onOpenProductDetail(product) },
                                    onBuyClick = { onQuickBuy(product) },
                                    onToggleFavorite = { onToggleFavorite(product.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 6. Rigid Listing Header + Quick Filter Chips + List/Grid Rigid Switcher
            item(key = "rigid_listing_header") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = ActionOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Jelajahi Semua Kantong",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = TextBlack
                                )
                            }
                            Text(
                                text = "${rigidProducts.size} kantong siap diambil sendiri",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }

                        // Rigid View Switcher: List vs 2-Col Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BgWhite,
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isList = rigidLayoutMode == RigidLayoutMode.LIST
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isList) ActionOrangeSoft else Color.Transparent)
                                        .clickable { rigidLayoutMode = RigidLayoutMode.LIST }
                                        .testTag("home_view_toggle_list"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = "Tampilan Daftar Rapi",
                                        tint = if (isList) ActionOrangeDark else TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (!isList) ActionOrangeSoft else Color.Transparent)
                                        .clickable { rigidLayoutMode = RigidLayoutMode.GRID }
                                        .testTag("home_view_toggle_grid"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = "Tampilan Grid 2 Kolom",
                                        tint = if (!isList) ActionOrangeDark else TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // GoFood-style Quick Filter Pills
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(RigidQuickFilter.entries, key = { it.name }) { filter ->
                            val selected = quickFilter == filter
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) FreshGreenSoft else BgWhite,
                                border = BorderStroke(
                                    width = if (selected) 1.3.dp else 1.dp,
                                    color = if (selected) FreshGreenDark else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { quickFilter = filter }
                                    .testTag("quick_filter_${filter.name.lowercase()}")
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                    ),
                                    color = if (selected) FreshGreenDark else TextBlack,
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 7. Rigid Listing Items (Empty State / Uniform Rigid List / Uniform 2-Col Rigid Grid)
            if (rigidProducts.isEmpty()) {
                item(key = "rigid_empty") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BgWhite,
                        border = BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada kantong yang cocok",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextBlack,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coba ubah filter cepat di atas untuk melihat kantong lainnya.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ActionOrangeSoft,
                                border = BorderStroke(1.dp, ActionOrange),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        quickFilter = RigidQuickFilter.SEMUA
                                    }
                            ) {
                                Text(
                                    text = "Reset Semua Filter",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ActionOrangeDark,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else if (rigidLayoutMode == RigidLayoutMode.LIST) {
                items(rigidProducts, key = { "rigid_list_${it.id}" }) { product ->
                    RigidListProductCard(
                        product = product,
                        onCardClick = { onOpenProductDetail(product) },
                        onBuyClick = { onQuickBuy(product) },
                        onToggleFavorite = { onToggleFavorite(product.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                val rows = rigidProducts.chunked(2)
                items(
                    items = rows,
                    key = { row -> "rigid_grid_${row.joinToString("_") { it.id }}" }
                ) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RigidGridProductCard(
                            product = rowItems[0],
                            onCardClick = { onOpenProductDetail(rowItems[0]) },
                            onBuyClick = { onQuickBuy(rowItems[0]) },
                            onToggleFavorite = { onToggleFavorite(rowItems[0].id) },
                            modifier = Modifier.weight(1f)
                        )
                        if (rowItems.size > 1) {
                            RigidGridProductCard(
                                product = rowItems[1],
                                onCardClick = { onOpenProductDetail(rowItems[1]) },
                                onBuyClick = { onQuickBuy(rowItems[1]) },
                                onToggleFavorite = { onToggleFavorite(rowItems[1].id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoFoodSectionHeader(
    title: String,
    subtitle: String,
    leadingBadge: @Composable () -> Unit,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingBadge()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = TextBlack
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = FreshGreenSoft,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onActionClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = FreshGreenDark
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = FreshGreenDark,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
