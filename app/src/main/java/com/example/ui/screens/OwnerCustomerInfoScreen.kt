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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerInfo
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerCustomerInfoScreen(
    customers: List<CustomerInfo>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredCustomers = customers.filter {
        searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery) ||
                it.email.contains(searchQuery, ignoreCase = true)
    }

    val totalSpentAll = customers.sumOf { it.totalSpent }
    val totalOrdersAll = customers.sumOf { it.totalOrders }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ChanWarmCream),
        contentPadding = PaddingValues(16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Owner Badge & Summary Header
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
                                text = "Data & Loyalitas Pelanggan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Pelanggan", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "${customers.size} Orang", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Column {
                            Text(text = "Total Transaksi", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "$totalOrdersAll Order", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Column {
                            Text(text = "Nilai Belanja (LTV)", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = ChanChanRepository.formatRupiah(totalSpentAll), color = ChanGoldPoints, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Search Input
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ChanWarmSurface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Cari nama, email, atau no HP...", fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("owner_customer_search_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Customer Cards
        items(filteredCustomers) { customer ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = ChanWarmSurface,
                shadowElevation = 1.dp
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ChanOrangeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customer.name.take(1),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = ChanOrangePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = customer.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${customer.phone} • ${customer.email}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (customer.tier) {
                                "Jiwa Gold Member" -> Color(0xFFFFF8E1)
                                "Jiwa Silver Member" -> Color(0xFFECEFF1)
                                else -> ChanSurfaceVariant
                            }
                        ) {
                            Text(
                                text = customer.tier,
                                color = if (customer.tier.contains("Gold")) ChanGoldPoints else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ChanBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Belanja", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = ChanChanRepository.formatRupiah(customer.totalSpent),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = ChanOrangePrimary
                            )
                        }

                        Column {
                            Text(text = "Pesanan Dibuat", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = "${customer.totalOrders} kali",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Text(text = "Koin Poin", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = "${customer.pointsBalance} Pts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ChanGoldPoints
                            )
                        }

                        Column {
                            Text(text = "Order Terakhir", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = customer.lastOrderDate,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
