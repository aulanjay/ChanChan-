package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BuyerBottomNav
import com.example.ui.components.ChanChanTopBar
import com.example.ui.components.SellerBottomNav
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChanChanTheme {
                ChanChanApp()
            }
        }
    }
}

@Composable
fun ChanChanApp(
    viewModel: ChanChanViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle System Back button
    BackHandler(enabled = state.isCheckoutVisible || state.appMode == AppMode.SELLER || state.buyerTab != BuyerTab.HOME) {
        if (state.isCheckoutVisible) {
            viewModel.showCheckout(false)
        } else if (state.appMode == AppMode.SELLER) {
            viewModel.toggleAppMode()
        } else if (state.buyerTab != BuyerTab.HOME) {
            viewModel.setBuyerTab(BuyerTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChanChanTopBar(
                appMode = state.appMode,
                selectedOutlet = state.selectedOutlet,
                cartItemCount = state.cart.sumOf { it.quantity },
                currentUser = state.currentUser,
                onOutletClick = { viewModel.showOutletPicker(true) },
                onToggleMode = { viewModel.toggleAppMode() },
                onCartClick = { viewModel.showCheckout(true) },
                onAccountClick = { viewModel.showLoginDialog(true) }
            )
        },
        bottomBar = {
            if (state.appMode == AppMode.BUYER) {
                BuyerBottomNav(
                    currentTab = state.buyerTab,
                    onTabSelected = { viewModel.setBuyerTab(it) }
                )
            } else {
                SellerBottomNav(
                    currentTab = state.sellerTab,
                    isOwner = state.isOwner,
                    onTabSelected = { viewModel.setSellerTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.appMode == AppMode.BUYER) {
                when (state.buyerTab) {
                    BuyerTab.HOME -> {
                        HomeScreen(
                            state = state,
                            onOrderTypeChange = { viewModel.setOrderType(it) },
                            onProductClick = { viewModel.openProductDetail(it) },
                            onNavigateToTab = { viewModel.setBuyerTab(it) },
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onOpenVouchers = { viewModel.showVoucherDialog(true) },
                            onOutletClick = { viewModel.showOutletPicker(true) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    BuyerTab.CATALOG -> {
                        CatalogScreen(
                            state = state,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onCategoryChange = { viewModel.selectCategory(it) },
                            onProductClick = { viewModel.openProductDetail(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    BuyerTab.ORDERS -> {
                        OrdersScreen(
                            orders = state.orders,
                            onReorder = { viewModel.reorderOrder(it) }
                        )
                    }
                    BuyerTab.PROFILE -> {
                        ProfileScreen(
                            user = state.userProfile,
                            currentUser = state.currentUser,
                            vouchers = state.vouchers,
                            onOpenAddAddress = { viewModel.showAddressDialog(true) },
                            onOpenVouchers = { viewModel.showVoucherDialog(true) },
                            onSwitchToMerchant = { viewModel.toggleAppMode() },
                            onOpenLogin = { viewModel.showLoginDialog(true) }
                        )
                    }
                }
            } else {
                // Merchant Hub / Seller Mode (Owner & Employee)
                when (state.sellerTab) {
                    SellerTab.PRODUCTS -> {
                        SellerProductsScreen(
                            state = state,
                            onAddProductClick = { viewModel.openAddProduct() },
                            onEditProductClick = { viewModel.openEditProduct(it) },
                            onDeleteProductClick = { viewModel.deleteProduct(it) },
                            onToggleAvailability = { viewModel.toggleProductAvailability(it) },
                            onRestockClick = { viewModel.openRestockDialog(it) },
                            onFilterChange = { viewModel.setMerchantStockFilter(it) }
                        )
                    }
                    SellerTab.ORDERS -> {
                        SellerOrdersScreen(
                            orders = state.orders,
                            onAdvanceStatus = { id, currentStatus -> viewModel.advanceOrderStatus(id, currentStatus) },
                            onCancelOrder = { viewModel.cancelOrder(it) }
                        )
                    }
                    SellerTab.REPORTING -> {
                        if (state.isOwner) {
                            SellerReportingScreen(
                                reportingSummary = viewModel.getReportingStats(),
                                selectedPeriod = state.reportingPeriod,
                                products = state.products,
                                onPeriodSelected = { viewModel.setReportingPeriod(it) },
                                onRestockClick = { viewModel.openRestockDialog(it) },
                                onNavigateToCustomers = { viewModel.setSellerTab(SellerTab.CUSTOMERS) }
                            )
                        } else {
                            // Restriction screen for employee
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ChanWarmSurface,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🔒", fontSize = 42.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Akses Khusus Owner",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Laporan omset penjualan, estimasi laba, dan grafik analitik hanya dapat diakses oleh Akun Pemilik (Owner).",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.showLoginDialog(true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ChanCoffeeBrown)
                                        ) {
                                            Text("Login sebagai Owner", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    SellerTab.CUSTOMERS -> {
                        if (state.isOwner) {
                            OwnerCustomerInfoScreen(
                                customers = state.customersList,
                                searchQuery = state.customerSearchQuery,
                                onSearchChange = { viewModel.setCustomerSearchQuery(it) }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ChanWarmSurface,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🔒", fontSize = 42.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Akses Khusus Owner",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Data privasi & riwayat loyalitas pelanggan hanya dapat diakses oleh Akun Pemilik (Owner).",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
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

    // --- Bottom Sheets & Dialogs ---

    // 1. Product Detail Customization Sheet
    state.selectedProductForDetail?.let { product ->
        ProductCustomizationSheet(
            product = product,
            onDismiss = { viewModel.closeProductDetail() },
            onAddToCart = { cartItem -> viewModel.addToCart(cartItem) }
        )
    }

    // 2. Checkout Bottom Sheet
    if (state.isCheckoutVisible) {
        CheckoutBottomSheet(
            state = state,
            onDismiss = { viewModel.showCheckout(false) },
            onOpenVouchers = { viewModel.showVoucherDialog(true) },
            onTogglePoints = { viewModel.toggleUsePoints() },
            onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
            onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
            onConfirmPayment = { viewModel.processCheckout() }
        )
    }

    // 3. Payment Success Dialog
    if (state.isSuccessDialogVisible && state.latestCreatedOrder != null) {
        PaymentSuccessDialog(
            order = state.latestCreatedOrder!!,
            onDismiss = { viewModel.dismissSuccessDialog() }
        )
    }

    // 4. Add / Edit Product Dialog (Seller)
    if (state.isAddEditProductVisible) {
        AddEditProductDialog(
            productToEdit = state.productToEdit,
            onDismiss = { viewModel.closeAddEditProduct() },
            onSave = { viewModel.saveProduct(it) }
        )
    }

    // 5. Voucher Selection Dialog
    if (state.isVoucherDialogVisible) {
        val subtotal = state.cart.sumOf { it.totalPrice }
        VoucherSelectionDialog(
            vouchers = state.vouchers,
            subtotal = subtotal,
            onDismiss = { viewModel.showVoucherDialog(false) },
            onSelectVoucher = { viewModel.selectVoucher(it) }
        )
    }

    // 6. Add Address Dialog
    if (state.isAddressDialogVisible) {
        AddAddressDialog(
            onDismiss = { viewModel.showAddressDialog(false) },
            onSave = { viewModel.saveAddress(it) }
        )
    }

    // 7. Outlet Picker Dialog
    if (state.isOutletPickerVisible) {
        OutletPickerDialog(
            outlets = state.outlets,
            selectedOutlet = state.selectedOutlet,
            onSelectOutlet = { viewModel.selectOutlet(it) },
            onDismiss = { viewModel.showOutletPicker(false) }
        )
    }

    // 8. Login & Role Switcher Dialog (Customer, Employee, Owner)
    if (state.isLoginDialogVisible) {
        LoginDialog(
            currentUser = state.currentUser,
            isFirebaseConnected = state.isFirebaseConnected,
            onDismiss = { viewModel.showLoginDialog(false) },
            onLogin = { role, email, name -> viewModel.login(role, email, name) },
            onGoogleSignInSuccess = { name, email, photo -> viewModel.signInWithGoogleSuccess(name, email, photo) },
            onLogout = { viewModel.logout() }
        )
    }

    // 9. Restock Product Dialog (Owner & Employee)
    if (state.isRestockDialogVisible && state.productToRestock != null) {
        RestockDialog(
            product = state.productToRestock!!,
            onDismiss = { viewModel.closeRestockDialog() },
            onConfirmRestock = { qty -> viewModel.restockProduct(state.productToRestock!!.id, qty) }
        )
    }
}
