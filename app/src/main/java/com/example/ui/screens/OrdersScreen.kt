package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(
    orders: List<OrderItem>,
    onReorder: (OrderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOrderTab by remember { mutableStateOf(0) } // 0: Active, 1: History

    val activeOrders = orders.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
    val historyOrders = orders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream)
    ) {
        // Tab Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChanWarmSurface,
            shadowElevation = 1.dp
        ) {
            TabRow(
                selectedTabIndex = selectedOrderTab,
                containerColor = ChanWarmSurface,
                contentColor = ChanOrangePrimary
            ) {
                Tab(
                    selected = selectedOrderTab == 0,
                    onClick = { selectedOrderTab = 0 },
                    text = {
                        Text(
                            text = "Sedang Berjalan (${activeOrders.size})",
                            fontWeight = if (selectedOrderTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_active_orders")
                )
                Tab(
                    selected = selectedOrderTab == 1,
                    onClick = { selectedOrderTab = 1 },
                    text = {
                        Text(
                            text = "Riwayat Pesanan",
                            fontWeight = if (selectedOrderTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_history_orders")
                )
            }
        }

        if (selectedOrderTab == 0) {
            if (activeOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "☕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Pesanan Berjalan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Yuk pesan kopi favorit dan toast lezat sekarang!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeOrders) { order ->
                        ActiveOrderCard(order = order)
                    }
                }
            }
        } else {
            if (historyOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📜", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Riwayat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyOrders) { order ->
                        HistoryOrderCard(order = order, onReorder = { onReorder(order) })
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveOrderCard(order: OrderItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = ChanWarmSurface,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header info & Order type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (order.orderType == OrderType.PICKUP) ChanOrangeContainer else Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = order.orderType.label.uppercase(),
                                color = if (order.orderType == OrderType.PICKUP) ChanOrangePrimary else ChanBlueInfo,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = order.outlet.name,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Pick-up PIN / Counter Code
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChanOrangePrimary
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PIN AMBIL",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "#${order.pickupPin}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Status Stepper
            OrderProgressStepper(status = order.status)

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Items in order
            Text(
                text = "Rincian Menu (${order.items.size} item)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            for (item in order.items) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity}x",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ChanOrangePrimary,
                        modifier = Modifier.width(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        val details = mutableListOf<String>()
                        details.add("Size: ${item.selectedSize.name}")
                        if (item.selectedSugar != "Normal (100%)") details.add(item.selectedSugar)
                        if (item.selectedIce != "Normal Ice") details.add(item.selectedIce)
                        if (item.selectedToppings.isNotEmpty()) {
                            details.add("Top: " + item.selectedToppings.joinToString { it.name })
                        }
                        if (details.isNotEmpty()) {
                            Text(
                                text = details.joinToString(" • "),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        if (item.notes.isNotEmpty()) {
                            Text(
                                text = "Catatan: \"${item.notes}\"",
                                fontSize = 11.sp,
                                color = ChanOrangeDark
                            )
                        }
                    }
                    Text(
                        text = ChanChanRepository.formatRupiah(item.totalPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Total Paid and Payment method
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.paymentMethod.title,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Bayar: ",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = ChanChanRepository.formatRupiah(order.totalPaid),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = ChanOrangePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun OrderProgressStepper(status: OrderStatus) {
    val steps = listOf(
        Pair("Diterima", OrderStatus.PENDING),
        Pair("Disiapkan", OrderStatus.PREPARING),
        Pair("Siap", OrderStatus.READY),
        Pair("Selesai", OrderStatus.COMPLETED)
    )

    val currentStepIndex = when (status) {
        OrderStatus.PENDING -> 0
        OrderStatus.PREPARING -> 1
        OrderStatus.READY -> 2
        OrderStatus.COMPLETED -> 3
        OrderStatus.CANCELLED -> -1
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, (label, _) ->
                val isDone = currentStepIndex >= index
                val isCurrent = currentStepIndex == index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) ChanOrangePrimary else ChanBorder
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Default.HourglassTop else Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isDone) ChanOrangePrimary else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryOrderCard(
    order: OrderItem,
    onReorder: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = dateFormat.format(Date(order.timestamp)),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = ChanGreenSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Summary of items
            Text(
                text = order.items.joinToString(", ") { "${it.quantity}x ${it.product.name}" },
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ChanChanRepository.formatRupiah(order.totalPaid),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = ChanOrangePrimary
                )

                OutlinedButton(
                    onClick = onReorder,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChanOrangePrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(ChanOrangePrimary)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pesan Lagi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
