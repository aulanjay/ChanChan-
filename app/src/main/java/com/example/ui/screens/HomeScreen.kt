package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.BuyerTab
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    state: UiState,
    onOrderTypeChange: (OrderType) -> Unit,
    onProductClick: (ProductItem) -> Unit,
    onNavigateToTab: (BuyerTab) -> Unit,
    onSelectCategory: (FoodCategory) -> Unit,
    onOpenVouchers: () -> Unit,
    onOutletClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Order Type Switcher (Pick Up vs Delivery - JIWA+ signature)
        item {
            OrderTypeHeader(
                orderType = state.orderType,
                selectedOutlet = state.selectedOutlet,
                defaultAddress = state.userProfile.addresses.firstOrNull { it.isDefault }
                    ?: state.userProfile.addresses.firstOrNull(),
                onOrderTypeChange = onOrderTypeChange,
                onOutletClick = onOutletClick
            )
        }

        // 2. Personalization Greeting & Membership Card
        item {
            PersonalizedMemberCard(
                user = state.userProfile,
                onOpenVouchers = onOpenVouchers
            )
        }

        // 3. Hero Promo Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToTab(BuyerTab.CATALOG) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_promo),
                    contentDescription = "Promo Jiwa Toast & Kopi",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )

                // Gradient scrim overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 60f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = ChanOrangePrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "PROMO SEJIWA 50%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sarapan Mantap Toast + Kopi",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Renyah di luar, lembut di dalam. Pesan sekarang!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 4. Quick Category Shortcuts
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "Pilih Kategori Menu",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CategoryIconItem("☕", "Kopi", FoodCategory.KOPI) {
                        onSelectCategory(FoodCategory.KOPI)
                        onNavigateToTab(BuyerTab.CATALOG)
                    }
                    CategoryIconItem("🥪", "Toast", FoodCategory.TOAST) {
                        onSelectCategory(FoodCategory.TOAST)
                        onNavigateToTab(BuyerTab.CATALOG)
                    }
                    CategoryIconItem("🏷️", "Promo", FoodCategory.PROMO) {
                        onSelectCategory(FoodCategory.PROMO)
                        onNavigateToTab(BuyerTab.CATALOG)
                    }
                    CategoryIconItem("🍛", "Meals", FoodCategory.MEALS) {
                        onSelectCategory(FoodCategory.MEALS)
                        onNavigateToTab(BuyerTab.CATALOG)
                    }
                    CategoryIconItem("🧋", "Tea", FoodCategory.NON_COFFEE) {
                        onSelectCategory(FoodCategory.NON_COFFEE)
                        onNavigateToTab(BuyerTab.CATALOG)
                    }
                }
            }
        }

        // 5. Secondary Banner (Summer Refreshers / Deals)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToTab(BuyerTab.CATALOG) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_banner_deals),
                    contentDescription = "Menu Minuman Segar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 6. Favorite / Fast Re-order Section (Personalization)
        item {
            val bestSellers = state.products.filter { it.isBestSeller }
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Paling Favorit Teman Sejiwa 🔥",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Menu pilihan yang selalu bikin kangen",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    TextButton(onClick = { onNavigateToTab(BuyerTab.CATALOG) }) {
                        Text(
                            text = "Lihat Semua",
                            color = ChanOrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bestSellers) { product ->
                        ProductCardCompact(
                            product = product,
                            isFavorite = state.userProfile.favoriteProductIds.contains(product.id),
                            onClick = { onProductClick(product) },
                            onFavoriteClick = { onToggleFavorite(product.id) }
                        )
                    }
                }
            }
        }

        // 7. Special Recommendations Grid
        item {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                Text(
                    text = "Rekomendasi Spesial Buat Kamu ✨",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        val recommended = state.products.take(6)
        items(recommended) { product ->
            ProductListItem(
                product = product,
                isFavorite = state.userProfile.favoriteProductIds.contains(product.id),
                onClick = { onProductClick(product) },
                onFavoriteClick = { onToggleFavorite(product.id) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun OrderTypeHeader(
    orderType: OrderType,
    selectedOutlet: Outlet,
    defaultAddress: SavedAddress?,
    onOrderTypeChange: (OrderType) -> Unit,
    onOutletClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = ChanWarmSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Segmented Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChanSurfaceVariant)
                    .padding(4.dp)
            ) {
                OrderTypePill(
                    title = "Pick Up",
                    subtitle = "Ambil di Outlet",
                    icon = Icons.Default.DirectionsWalk,
                    isSelected = orderType == OrderType.PICKUP,
                    modifier = Modifier.weight(1f),
                    onClick = { onOrderTypeChange(OrderType.PICKUP) }
                )

                OrderTypePill(
                    title = "Delivery",
                    subtitle = "Antar ke Tempat",
                    icon = Icons.Default.DeliveryDining,
                    isSelected = orderType == OrderType.DELIVERY,
                    modifier = Modifier.weight(1f),
                    onClick = { onOrderTypeChange(OrderType.DELIVERY) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details based on mode
            if (orderType == OrderType.PICKUP) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOutletClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChanOrangeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = ChanOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedOutlet.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "${selectedOutlet.distanceKm} km",
                                    color = ChanGreenSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Estimasi siap saji: ~15 menit",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ganti outlet",
                        tint = TextSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChanOrangeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = ChanOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = defaultAddress?.label ?: "Alamat Pengantaran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = defaultAddress?.fullAddress ?: "Jl. Jend Sudirman Kav 60, Jakarta",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ChanOrangeContainer
                    ) {
                        Text(
                            text = "Ongkir 12rb",
                            color = ChanOrangePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTypePill(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) ChanOrangePrimary else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary
                )
            }
        }
    }
}

@Composable
fun PersonalizedMemberCard(
    user: UserProfile,
    onOpenVouchers: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = ChanCoffeeBrown,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ChanGoldPoints),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1),
                            color = ChanCoffeeBrown,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Halo, Kak ${user.name.split(" ").first()}! 👋",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = ChanGoldPoints,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.tier,
                                color = ChanGoldPoints,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = user.memberCode,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ChanPoints Balance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = ChanGoldPoints,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ChanPoints",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${user.points} Pts",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }

                // Vouchers Button
                OutlinedButton(
                    onClick = onOpenVouchers,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color.White, Color.White))),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("home_vouchers_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "4 Kupon Aktif", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryIconItem(
    emoji: String,
    label: String,
    category: FoodCategory,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ChanWarmSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
fun ProductCardCompact(
    product: ProductItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("compact_product_${product.id}"),
        color = ChanWarmSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChanSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.emoji, fontSize = 48.sp)

                // Favorite Heart Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (isFavorite) Color(0xFFE91E63) else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (product.isBestSeller) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp),
                        color = ChanOrangePrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Best Seller",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = ChanGoldPoints,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${product.rating}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ChanChanRepository.formatRupiah(product.price),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = ChanOrangePrimary
                )

                Surface(
                    shape = CircleShape,
                    color = ChanOrangePrimary,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListItem(
    product: ProductItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("product_item_${product.id}"),
        color = ChanWarmSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChanSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.emoji, fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (isFavorite) Color(0xFFE91E63) else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = product.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ChanChanRepository.formatRupiah(product.price),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = ChanOrangePrimary
                        )

                        if (product.originalPrice != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ChanChanRepository.formatRupiah(product.originalPrice),
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = TextMuted
                            )
                        }
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Pesan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
