package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BuyerBottomNav
import com.example.ui.components.ChanChanTopBar
import com.example.ui.components.SellerBottomNav
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.ChanChanTheme
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
                onOutletClick = { viewModel.showOutletPicker(true) },
                onToggleMode = { viewModel.toggleAppMode() },
                onCartClick = { viewModel.showCheckout(true) }
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
                            vouchers = state.vouchers,
                            onOpenAddAddress = { viewModel.showAddressDialog(true) },
                            onOpenVouchers = { viewModel.showVoucherDialog(true) },
                            onSwitchToMerchant = { viewModel.toggleAppMode() }
                        )
                    }
                }
            } else {
                // Merchant Hub / Seller Mode
                when (state.sellerTab) {
                    SellerTab.PRODUCTS -> {
                        SellerProductsScreen(
                            state = state,
                            onAddProductClick = { viewModel.openAddProduct() },
                            onEditProductClick = { viewModel.openEditProduct(it) },
                            onDeleteProductClick = { viewModel.deleteProduct(it) },
                            onToggleAvailability = { viewModel.toggleProductAvailability(it) },
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
                        SellerReportingScreen(
                            reportingSummary = viewModel.getReportingStats(),
                            selectedPeriod = state.reportingPeriod,
                            onPeriodSelected = { viewModel.setReportingPeriod(it) }
                        )
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
}
