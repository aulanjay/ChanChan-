package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategory
import com.example.data.model.ProductItem
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: UiState,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (FoodCategory) -> Unit,
    onProductClick: (ProductItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredProducts = state.products.filter { product ->
        val matchesCategory = (state.selectedCategory == FoodCategory.ALL) || (product.category == state.selectedCategory)
        val matchesSearch = state.searchQuery.isEmpty() ||
                product.name.contains(state.searchQuery, ignoreCase = true) ||
                product.description.contains(state.searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream)
    ) {
        // Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(12.dp),
            color = ChanWarmSurface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Cari kopi, toast, atau rice bowl...", fontSize = 13.sp, color = TextMuted) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("catalog_search_input")
                )
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus pencarian",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Category Filter Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FoodCategory.values()) { category ->
                val isSelected = category == state.selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryChange(category) },
                    label = {
                        Text(
                            text = "${category.icon} ${category.displayName}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChanOrangePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = ChanWarmSurface,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_${category.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Product Catalog List
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Menu tidak ditemukan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Coba kata kunci lain atau pilih kategori berbeda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { product ->
                    CatalogProductRow(
                        product = product,
                        isFavorite = state.userProfile.favoriteProductIds.contains(product.id),
                        onClick = { onProductClick(product) },
                        onFavoriteClick = { onToggleFavorite(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogProductRow(
    product: ProductItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("catalog_row_${product.id}"),
        color = ChanWarmSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChanSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.emoji, fontSize = 42.sp)

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
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
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
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ChanGoldPoints,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${product.rating} (${product.reviewsCount}+)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "⏱️ ${product.prepTimeMinutes} mnt",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ChanChanRepository.formatRupiah(product.price),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
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

                    if (product.isAvailable) {
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "+ Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = "Habis",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
