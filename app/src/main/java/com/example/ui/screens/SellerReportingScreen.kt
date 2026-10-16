package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChanChanRepository
import com.example.data.repository.ReportingSummary
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerReportingScreen(
    reportingSummary: ReportingSummary,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream),
        contentPadding = PaddingValues(16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        title = "Total Omset",
                        value = ChanChanRepository.formatRupiah(reportingSummary.totalRevenue),
                        subtext = "+14.8% vs periode lalu",
                        icon = Icons.Default.TrendingUp,
                        cardColor = ChanWarmSurface,
                        textColor = ChanOrangePrimary,
                        modifier = Modifier.weight(1f)
                    )

                    ReportingKpiCard(
                        title = "Total Pesanan",
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
                                text = "Tertinggi: Rp 3.8Jt",
                                color = ChanOrangePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Canvas Bar Chart
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

                                // Bar
                                drawRoundRect(
                                    color = if (isToday) ChanOrangePrimary else ChanOrangeLight.copy(alpha = 0.45f),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }

                        // Day Labels beneath bars
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

        // Top 5 Best Selling Products (Menu Terlaris)
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
                        text = "Kontributor omset terbesar minggu ini",
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
                                    text = "${topItem.quantitySold} porsi terjual",
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
                            Divider(color = ChanBorder, modifier = Modifier.padding(vertical = 4.dp))
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
