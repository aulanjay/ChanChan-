package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductItem
import com.example.data.repository.ChanChanRepository
import com.example.data.repository.ReportingSummary
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerReportingScreen(
    reportingSummary: ReportingSummary,
    selectedPeriod: String,
    products: List<ProductItem> = emptyList(),
    onPeriodSelected: (String) -> Unit,
    onRestockClick: (ProductItem) -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedReportTab by remember { mutableStateOf("Penjualan") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Owner Badge & Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ChanCoffeeBrown,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ChanGoldPoints
                            ) {
                                Text(
                                    text = "KHUSUS OWNER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ChanCoffeeBrown,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Laporan Analitik Bisnis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pantau perkembangan omset penjualan, pergerakan stok gudang, dan menu terlaris secara real-time.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Sub-Tab Segmented Selector (Penjualan / Stok / Menu)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ChanWarmSurface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        Triple("Penjualan", "💰", "Omset & Laba"),
                        Triple("Stok", "📦", "Inventaris"),
                        Triple("Menu", "🥪", "Performa Menu")
                    )

                    tabs.forEach { (tabName, icon, sub) ->
                        val isSelected = selectedReportTab == tabName
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedReportTab = tabName },
                            color = if (isSelected) ChanOrangePrimary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = icon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tabName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                                Text(
                                    text = sub,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: LAPORAN PENJUALAN (SALES)
        if (selectedReportTab == "Penjualan") {
            // Period Selector Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Hari Ini", "Minggu Ini", "Bulan Ini").forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { onPeriodSelected(period) },
                            label = { Text(period, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChanOrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = ChanWarmSurface,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // 4 KPI Summary Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportingKpiCard(
                            title = "Total Omset Penjualan",
                            value = ChanChanRepository.formatRupiah(reportingSummary.totalRevenue),
                            subtext = "+14.8% vs periode lalu",
                            icon = Icons.Default.TrendingUp,
                            cardColor = ChanWarmSurface,
                            textColor = ChanOrangePrimary,
                            modifier = Modifier.weight(1f)
                        )

                        ReportingKpiCard(
                            title = "Total Pesanan Masuk",
                            value = "${reportingSummary.totalOrders} Order",
                            subtext = "Tingkat sukses 98.2%",
                            icon = Icons.Default.ReceiptLong,
                            cardColor = ChanWarmSurface,
                            textColor = ChanCoffeeBrown,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportingKpiCard(
                            title = "Rata-rata Order (AOV)",
                            value = ChanChanRepository.formatRupiah(reportingSummary.averageOrderValue),
                            subtext = "Per transaksi pelanggan",
                            icon = Icons.Default.AccountBalanceWallet,
                            cardColor = ChanWarmSurface,
                            textColor = ChanCoffeeAmber,
                            modifier = Modifier.weight(1f)
                        )

                        ReportingKpiCard(
                            title = "Estimasi Laba Kotor",
                            value = ChanChanRepository.formatRupiah(reportingSummary.grossProfit),
                            subtext = "Margin kotor ~45%",
                            icon = Icons.Default.MonetizationOn,
                            cardColor = ChanWarmSurface,
                            textColor = ChanGreenSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Daily Revenue Trend Chart
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChanWarmSurface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Grafik Tren Penjualan Harian",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Performa omset 7 hari terakhir",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ChanOrangeContainer
                            ) {
                                Text(
                                    text = "Puncak: Rp 3.8Jt",
                                    color = ChanOrangePrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val trend = reportingSummary.dailySalesTrend
                        val maxRev = trend.maxOfOrNull { it.revenue }?.toFloat() ?: 1f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barWidth = size.width / (trend.size * 2f)
                                val spacing = size.width / trend.size
                                val chartHeight = size.height - 40f

                                trend.forEachIndexed { i, point ->
                                    val x = (i * spacing) + (spacing / 2f) - (barWidth / 2f)
                                    val barHeight = (point.revenue / maxRev) * chartHeight
                                    val y = chartHeight - barHeight
                                    val isToday = i == trend.size - 1

                                    drawRoundRect(
                                        color = if (isToday) ChanOrangePrimary else ChanOrangeLight.copy(alpha = 0.45f),
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                trend.forEach { point ->
                                    Text(
                                        text = point.dayLabel.split(" ").first(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Methods Breakdown
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChanWarmSurface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Metode Pembayaran Terfavorit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Distribusi pembayaran pelanggan",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        listOf(
                            Triple("QRIS Instant", 54, ChanOrangePrimary),
                            Triple("GoPay & E-Wallet", 26, Color(0xFF1976D2)),
                            Triple("Virtual Account Bank", 14, ChanCoffeeBrown),
                            Triple("Tunai di Outlet", 6, ChanGreenSuccess)
                        ).forEach { (method, pct, color) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = method, fontSize = 12.sp, color = TextPrimary)
                                    Text(text = "$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = ChanSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Link to Customer Database (Owner Exclusive)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCustomers() },
                    shape = RoundedCornerShape(16.dp),
                    color = ChanCoffeeBrown,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lihat Database Pelanggan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Buka data riwayat belanja, nomor telepon & poin loyalitas",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // TAB 2: LAPORAN STOK & INVENTARIS
        if (selectedReportTab == "Stok") {
            val totalReady = products.filter { it.isAvailable }.sumOf { it.stockQuantity }
            val lowStockItems = products.filter { it.stockQuantity in 1..14 }
            val outOfStockItems = products.filter { it.stockQuantity <= 0 || !it.isAvailable }
            val safeStockItems = products.filter { it.stockQuantity >= 15 && it.isAvailable }

            // Stock KPI Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportingKpiCard(
                        title = "Total Porsi Siap",
                        value = "$totalReady Porsi",
                        subtext = "Semua menu aktif",
                        icon = Icons.Default.Inventory,
                        cardColor = ChanWarmSurface,
                        textColor = ChanCoffeeBrown,
                        modifier = Modifier.weight(1f)
                    )
                    ReportingKpiCard(
                        title = "Stok Kritis (<15)",
                        value = "${lowStockItems.size} Menu",
                        subtext = "Perlu restock segera",
                        icon = Icons.Default.WarningAmber,
                        cardColor = ChanWarmSurface,
                        textColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    ReportingKpiCard(
                        title = "Stok Habis",
                        value = "${outOfStockItems.size} Menu",
                        subtext = "Tidak bisa dipesan",
                        icon = Icons.Default.Block,
                        cardColor = ChanWarmSurface,
                        textColor = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Urgent Restock Recommendation List
            if (lowStockItems.isNotEmpty() || outOfStockItems.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF3E0),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Peringatan Stok Menipis & Habis",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            (outOfStockItems + lowStockItems).take(4).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = prod.emoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = prod.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = if (prod.stockQuantity <= 0) "HABIS (0 porsi)" else "Tersisa ${prod.stockQuantity} porsi",
                                                fontSize = 10.sp,
                                                color = if (prod.stockQuantity <= 0) Color(0xFFD32F2F) else Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onRestockClick(prod) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("+ Restock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Full Stock Audit Table
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChanWarmSurface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Laporan Stok Seluruh Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Detail jumlah porsi dan estimasi modal per menu",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        products.forEachIndexed { idx, prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = prod.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${prod.category.displayName} • HPP: ${ChanChanRepository.formatRupiah(prod.costPrice)}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            prod.stockQuantity <= 0 || !prod.isAvailable -> Color(0xFFFFEBEE)
                                            prod.stockQuantity < 15 -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFE8F5E9)
                                        }
                                    ) {
                                        Text(
                                            text = "${prod.stockQuantity} porsi",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                prod.stockQuantity <= 0 || !prod.isAvailable -> Color(0xFFD32F2F)
                                                prod.stockQuantity < 15 -> Color(0xFFE65100)
                                                else -> ChanGreenSuccess
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onRestockClick(prod) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddBusiness,
                                        contentDescription = "Restock",
                                        tint = ChanOrangePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (idx < products.size - 1) {
                                HorizontalDivider(color = ChanBorder, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: LAPORAN MENU & KATEGORI
        if (selectedReportTab == "Menu") {
            // Top 5 Sellers
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChanWarmSurface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "5 Menu Terlaris (Top Sellers) 🏆",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Kontributor omset terbesar periode ini",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        reportingSummary.topSellingProducts.forEachIndexed { index, topItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (index == 0) ChanGoldPoints else ChanSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (index == 0) ChanCoffeeBrown else TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = topItem.product.emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topItem.product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${topItem.quantitySold} porsi terjual • Harga: ${ChanChanRepository.formatRupiah(topItem.product.price)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Text(
                                    text = ChanChanRepository.formatRupiah(topItem.revenueTotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ChanCoffeeBrown
                                )
                            }

                            if (index < reportingSummary.topSellingProducts.size - 1) {
                                HorizontalDivider(color = ChanBorder, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // Category Contribution Breakdown
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChanWarmSurface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kontribusi Penjualan per Kategori",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Persentase pangsa pasar menu ChanChan+",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        listOf(
                            Triple("🥪 Jiwa Toast", 42, ChanOrangePrimary),
                            Triple("☕ Kopi Sejiwa", 34, ChanCoffeeBrown),
                            Triple("🧋 Tea & Choco", 14, Color(0xFF00897B)),
                            Triple("🍛 Rice Bowls & Meals", 7, ChanCoffeeAmber),
                            Triple("🥐 Bakery & Sides", 3, Color(0xFF6D4C41))
                        ).forEach { (catName, percent, barColor) ->
                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = catName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(text = "$percent% omset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = barColor)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = barColor,
                                    trackColor = ChanSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Highest Margin Food Items
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ChanWarmSurface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Menu Margin Keuntungan Tertinggi 💡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Keuntungan bersih per porsi tertinggi untuk promosi",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        listOf(
                            Triple("Kopi Susu Senja", "Margin 65% (+Rp 13.000/cup)", "☕"),
                            Triple("Earl Grey Tea Latte", "Margin 62% (+Rp 13.500/cup)", "🧋"),
                            Triple("Crispy Beef Bulgogi Toast", "Margin 48% (+Rp 17.500/toast)", "🥪"),
                            Triple("Truffle Fries with Mayo", "Margin 52% (+Rp 12.500/porsi)", "🍟")
                        ).forEachIndexed { i, (menuName, marginDesc, emoji) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = menuName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = marginDesc, fontSize = 11.sp, color = ChanGreenSuccess, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ChanGreenSuccess, modifier = Modifier.size(18.dp))
                            }
                            if (i < 3) {
                                HorizontalDivider(color = ChanBorder, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportingKpiCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = textColor
            )

            Text(
                text = subtext,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
