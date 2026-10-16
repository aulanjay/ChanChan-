package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.Outlet
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.BuyerTab
import com.example.ui.viewmodel.SellerTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChanChanTopBar(
    appMode: AppMode,
    selectedOutlet: Outlet,
    cartItemCount: Int,
    currentUser: AppUser?,
    onOutletClick: () -> Unit,
    onToggleMode: () -> Unit,
    onCartClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChanWarmSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // First Row: Brand, Role Badge & Account Login/Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ChanOrangePrimary
                    ) {
                        Text(
                            text = "ChanChan+",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔥", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Cloud DB",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChanGreenSuccess
                            )
                        }
                    }

                    if (appMode == AppMode.SELLER) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (currentUser?.role == UserRole.OWNER) ChanCoffeeBrown else Color(0xFF1976D2)
                        ) {
                            Text(
                                text = if (currentUser?.role == UserRole.OWNER) "OWNER HUB" else "STAFF HUB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Account / Role Pill Button (Click to Switch Account)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (currentUser?.role) {
                        UserRole.OWNER -> Color(0xFFEFEBE9)
                        UserRole.EMPLOYEE -> Color(0xFFE3F2FD)
                        else -> ChanOrangeContainer
                    },
                    modifier = Modifier
                        .clickable { onAccountClick() }
                        .testTag("topbar_account_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = currentUser?.avatarEmoji ?: "👤", fontSize = 12.sp)
                        Text(
                            text = currentUser?.let { "${it.name.split(" ").first()} (${it.role.displayName.split(" ").first()})" } ?: "Masuk",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(currentUser?.role?.badgeColorHex ?: 0xFFEE4D2D)
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ganti peran",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Second Row: Outlet Location Selector + Mode Toggle + Cart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Outlet Location Selector
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOutletClick() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Outlet location",
                        tint = ChanOrangePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedOutlet.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih outlet",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mode Switch Button (Pelanggan <-> Mitra / Dapur)
                    FilterChip(
                        selected = appMode == AppMode.SELLER,
                        onClick = onToggleMode,
                        label = {
                            Text(
                                text = if (appMode == AppMode.BUYER) "Kelola Toko" else "Lihat Menu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (appMode == AppMode.BUYER) Icons.Default.Storefront else Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChanCoffeeBrown,
                            selectedLabelColor = Color.White,
                            containerColor = ChanSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("toggle_app_mode_btn")
                    )

                    // Cart Icon (Only in Buyer Mode)
                    if (appMode == AppMode.BUYER) {
                        Box {
                            IconButton(
                                onClick = onCartClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("cart_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = "Keranjang belanja",
                                    tint = ChanOrangePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            if (cartItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(ChanOrangePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cartItemCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuyerBottomNav(
    currentTab: BuyerTab,
    onTabSelected: (BuyerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = ChanWarmSurface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentTab == BuyerTab.HOME,
            onClick = { onTabSelected(BuyerTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BuyerTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Beranda", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ChanOrangePrimary,
                selectedTextColor = ChanOrangePrimary,
                indicatorColor = ChanOrangeContainer
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentTab == BuyerTab.CATALOG,
            onClick = { onTabSelected(BuyerTab.CATALOG) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BuyerTab.CATALOG) Icons.Filled.Fastfood else Icons.Outlined.Fastfood,
                    contentDescription = "Katalog Menu"
                )
            },
            label = { Text("Menu", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ChanOrangePrimary,
                selectedTextColor = ChanOrangePrimary,
                indicatorColor = ChanOrangeContainer
            ),
            modifier = Modifier.testTag("nav_catalog")
        )

        NavigationBarItem(
            selected = currentTab == BuyerTab.ORDERS,
            onClick = { onTabSelected(BuyerTab.ORDERS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BuyerTab.ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                    contentDescription = "Pesanan"
                )
            },
            label = { Text("Pesanan", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ChanOrangePrimary,
                selectedTextColor = ChanOrangePrimary,
                indicatorColor = ChanOrangeContainer
            ),
            modifier = Modifier.testTag("nav_orders")
        )

        NavigationBarItem(
            selected = currentTab == BuyerTab.PROFILE,
            onClick = { onTabSelected(BuyerTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BuyerTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profil & Poin"
                )
            },
            label = { Text("Akun", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ChanOrangePrimary,
                selectedTextColor = ChanOrangePrimary,
                indicatorColor = ChanOrangeContainer
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

@Composable
fun SellerBottomNav(
    currentTab: SellerTab,
    isOwner: Boolean,
    onTabSelected: (SellerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = ChanWarmSurface,
        tonalElevation = 4.dp
    ) {
        // Tab 1: Menu & Stok (Both Owner & Employee)
        NavigationBarItem(
            selected = currentTab == SellerTab.PRODUCTS,
            onClick = { onTabSelected(SellerTab.PRODUCTS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SellerTab.PRODUCTS) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                    contentDescription = "Manajemen Produk"
                )
            },
            label = { Text("Menu & Stok", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ChanCoffeeBrown,
                selectedTextColor = ChanCoffeeBrown,
                indicatorColor = Color(0xFFEBE0D5)
            ),
            modifier = Modifier.testTag("seller_nav_products")
        )

        // Tab 2: Dapur Masuk (Both Owner & Employee)
        NavigationBarItem(
            selected = currentTab == SellerTab.ORDERS,
            onClick = { onTabSelected(SellerTab.ORDERS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SellerTab.ORDERS) Icons.Filled.SoupKitchen else Icons.Outlined.SoupKitchen,
                    contentDescription = "Pesanan Dapur"
                )
            },
            label = { Text("Dapur", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ChanCoffeeBrown,
                selectedTextColor = ChanCoffeeBrown,
                indicatorColor = Color(0xFFEBE0D5)
            ),
            modifier = Modifier.testTag("seller_nav_orders")
        )

        // Tab 3: Laporan Omset & Penjualan (Owner Exclusive)
        if (isOwner) {
            NavigationBarItem(
                selected = currentTab == SellerTab.REPORTING,
                onClick = { onTabSelected(SellerTab.REPORTING) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == SellerTab.REPORTING) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                        contentDescription = "Laporan & Analitik"
                    )
                },
                label = { Text("Laporan", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ChanCoffeeBrown,
                    selectedTextColor = ChanCoffeeBrown,
                    indicatorColor = Color(0xFFEBE0D5)
                ),
                modifier = Modifier.testTag("seller_nav_reporting")
            )

            // Tab 4: Customer Info & Database (Owner Exclusive)
            NavigationBarItem(
                selected = currentTab == SellerTab.CUSTOMERS,
                onClick = { onTabSelected(SellerTab.CUSTOMERS) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == SellerTab.CUSTOMERS) Icons.Filled.Groups else Icons.Outlined.Groups,
                        contentDescription = "Data Pelanggan"
                    )
                },
                label = { Text("Pelanggan", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ChanCoffeeBrown,
                    selectedTextColor = ChanCoffeeBrown,
                    indicatorColor = Color(0xFFEBE0D5)
                ),
                modifier = Modifier.testTag("seller_nav_customers")
            )
        }
    }
}
