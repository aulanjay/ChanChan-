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
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    orders: List<OrderItem>,
    onAdvanceStatus: (String, OrderStatus) -> Unit,
    onCancelOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf("Aktif") }

    val filteredOrders = when (statusFilter) {
        "Aktif" -> orders.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
        "Selesai" -> orders.filter { it.status == OrderStatus.COMPLETED }
        "Dibatalkan" -> orders.filter { it.status == OrderStatus.CANCELLED }
        else -> orders
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream)
    ) {
        // Status Filter Chips
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ChanWarmSurface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Aktif", "Semua", "Selesai", "Dibatalkan").forEach { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { statusFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChanCoffeeBrown,
                            selectedLabelColor = Color.White,
                            containerColor = ChanWarmCream,
                            labelColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🍳", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak Ada Pesanan Dapur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pesanan baru dari pelanggan akan otomatis muncul di sini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredOrders) { order ->
                    SellerOrderCard(
                        order = order,
                        onAdvance = { onAdvanceStatus(order.orderId, order.status) },
                        onCancel = { onCancelOrder(order.orderId) }
                    )
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    order: OrderItem,
    onAdvance: () -> Unit,
    onCancel: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale("id", "ID")) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = ChanWarmSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (order.orderType == OrderType.PICKUP) ChanOrangeContainer else Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = order.orderType.label.uppercase(),
                                color = if (order.orderType == OrderType.PICKUP) ChanOrangePrimary else ChanBlueInfo,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Pemesan: ${order.customerName} • Masuk ${dateFormat.format(Date(order.timestamp))}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(order.status.badgeColorHex)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Order Items & Instructions for Kitchen/Barista
            Text(
                text = "Daftar Pesanan Dapur:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            for (item in order.items) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${item.quantity}x",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = ChanOrangePrimary,
                        modifier = Modifier.width(32.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        val specs = mutableListOf<String>()
                        specs.add("Ukuran: ${item.selectedSize.name}")
                        specs.add("Gula: ${item.selectedSugar}")
                        specs.add("Es: ${item.selectedIce}")
                        if (item.selectedToppings.isNotEmpty()) {
                            specs.add("Topping: " + item.selectedToppings.joinToString { it.name })
                        }
                        Text(
                            text = specs.joinToString(" • "),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        if (item.notes.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFF3E0),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "⚠️ Catatan: \"${item.notes}\"",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChanOrangeDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer with Total & Status Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total Omset:", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = ChanChanRepository.formatRupiah(order.totalPaid),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = ChanCoffeeBrown
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (order.status == OrderStatus.PENDING) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Tolak", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    when (order.status) {
                        OrderStatus.PENDING -> {
                            Button(
                                onClick = onAdvance,
                                colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mulai Racik", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = onAdvance,
                                colors = ButtonDefaults.buttonColors(containerColor = ChanCoffeeBrown),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Siap Ambil", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.READY -> {
                            Button(
                                onClick = onAdvance,
                                colors = ButtonDefaults.buttonColors(containerColor = ChanGreenSuccess),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Selesaikan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ChanSurfaceVariant
                            ) {
                                Text(
                                    text = "Pesanan Beres",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
