package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductItem
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductsScreen(
    state: UiState,
    onAddProductClick: () -> Unit,
    onEditProductClick: (ProductItem) -> Unit,
    onDeleteProductClick: (String) -> Unit,
    onToggleAvailability: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = state.products.size
    val availableCount = state.products.count { it.isAvailable }
    val outOfStockCount = totalCount - availableCount

    val filteredProducts = when (state.merchantStockFilter) {
        "Tersedia" -> state.products.filter { it.isAvailable }
        "Habis" -> state.products.filter { !it.isAvailable }
        else -> state.products
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChanWarmCream,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProductClick,
                containerColor = ChanOrangePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Produk Baru", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .navigationBarsPadding()
                    .offset(y = (-80).dp)
                    .testTag("seller_fab_add_product")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Store Inventory Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InventoryMetricCard(
                        title = "Total Menu",
                        count = totalCount.toString(),
                        color = ChanCoffeeBrown,
                        modifier = Modifier.weight(1f)
                    )
                    InventoryMetricCard(
                        title = "Tersedia",
                        count = availableCount.toString(),
                        color = ChanGreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    InventoryMetricCard(
                        title = "Stok Habis",
                        count = outOfStockCount.toString(),
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Tersedia", "Habis").forEach { filter ->
                        FilterChip(
                            selected = state.merchantStockFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChanCoffeeBrown,
                                selectedLabelColor = Color.White,
                                containerColor = ChanWarmSurface,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Product List for Management
            items(filteredProducts) { product ->
                SellerProductItemRow(
                    product = product,
                    onEdit = { onEditProductClick(product) },
                    onDelete = { onDeleteProductClick(product.id) },
                    onToggleAvailability = { onToggleAvailability(product.id) }
                )
            }
        }
    }
}

@Composable
fun InventoryMetricCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ChanWarmSurface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SellerProductItemRow(
    product: ProductItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = ChanWarmSurface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ChanSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = product.emoji, fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ChanOrangeContainer
                        ) {
                            Text(
                                text = product.category.displayName,
                                color = ChanOrangePrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stok: ${product.stockQuantity}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ChanChanRepository.formatRupiah(product.price),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = ChanOrangePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Modal: ${ChanChanRepository.formatRupiah(product.costPrice)})",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // In-Stock Availability Switch
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = product.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ChanGreenSuccess
                        ),
                        modifier = Modifier.testTag("toggle_avail_${product.id}")
                    )
                    Text(
                        text = if (product.isAvailable) "Tersedia" else "Habis",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isAvailable) ChanGreenSuccess else Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Edit & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Hapus", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = ChanCoffeeBrown),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit Produk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
