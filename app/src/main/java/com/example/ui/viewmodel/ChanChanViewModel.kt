package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ChanChanRepository
import com.example.data.repository.ReportingSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppMode {
    BUYER, SELLER
}

enum class BuyerTab {
    HOME, CATALOG, ORDERS, PROFILE
}

enum class SellerTab {
    PRODUCTS, ORDERS, REPORTING, CUSTOMERS
}

data class UiState(
    val appMode: AppMode = AppMode.BUYER,
    val buyerTab: BuyerTab = BuyerTab.HOME,
    val sellerTab: SellerTab = SellerTab.PRODUCTS,
    val selectedOutlet: Outlet,
    val orderType: OrderType = OrderType.PICKUP,
    val selectedCategory: FoodCategory = FoodCategory.ALL,
    val searchQuery: String = "",
    val products: List<ProductItem> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val orders: List<OrderItem> = emptyList(),
    val vouchers: List<Voucher> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val selectedProductForDetail: ProductItem? = null,
    val selectedVoucher: Voucher? = null,
    val usePoints: Boolean = false,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.QRIS,
    val isCheckoutVisible: Boolean = false,
    val isOutletPickerVisible: Boolean = false,
    val isVoucherDialogVisible: Boolean = false,
    val isAddressDialogVisible: Boolean = false,
    val isAddEditProductVisible: Boolean = false,
    val productToEdit: ProductItem? = null,
    val latestCreatedOrder: OrderItem? = null,
    val isSuccessDialogVisible: Boolean = false,
    val reportingPeriod: String = "Hari Ini",
    val merchantStockFilter: String = "Semua",
    val outlets: List<Outlet> = emptyList(),
    val currentUser: AppUser? = null,
    val isLoginDialogVisible: Boolean = false,
    val customersList: List<CustomerInfo> = emptyList(),
    val isRestockDialogVisible: Boolean = false,
    val productToRestock: ProductItem? = null,
    val customerSearchQuery: String = "",
    val isAddVoucherDialogVisible: Boolean = false,
    val isFirebaseConnected: Boolean = true
) {
    val isOwner: Boolean get() = currentUser?.role == UserRole.OWNER
    val isEmployee: Boolean get() = currentUser?.role == UserRole.EMPLOYEE
    val isCustomer: Boolean get() = currentUser?.role == UserRole.CUSTOMER || currentUser == null
    val canViewFinancialReports: Boolean get() = isOwner
    val canViewCustomerDatabase: Boolean get() = isOwner
    val canCrudMenuAndStock: Boolean get() = isOwner || isEmployee
}

class ChanChanViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: ChanChanRepository = ChanChanRepository.getInstance(application)

    private val _uiState = MutableStateFlow(
        UiState(
            selectedOutlet = repository.outlets.value.first(),
            outlets = repository.outlets.value,
            products = repository.products.value,
            orders = repository.orders.value,
            vouchers = repository.vouchers.value,
            userProfile = repository.userProfile.value,
            currentUser = repository.currentUser.value,
            customersList = repository.customersList.value,
            isFirebaseConnected = repository.isFirebaseConnected.value
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.products.collect { prods ->
                _uiState.update { it.copy(products = prods) }
            }
        }
        viewModelScope.launch {
            repository.cart.collect { cartItems ->
                _uiState.update { it.copy(cart = cartItems) }
            }
        }
        viewModelScope.launch {
            repository.orders.collect { ordersList ->
                _uiState.update { it.copy(orders = ordersList) }
            }
        }
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
        viewModelScope.launch {
            repository.outlets.collect { outletList ->
                _uiState.update { it.copy(outlets = outletList) }
            }
        }
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _uiState.update {
                    val appMode = if (user?.role == UserRole.OWNER || user?.role == UserRole.EMPLOYEE) {
                        AppMode.SELLER
                    } else {
                        AppMode.BUYER
                    }
                    it.copy(currentUser = user, appMode = appMode)
                }
            }
        }
        viewModelScope.launch {
            repository.customersList.collect { list ->
                _uiState.update { it.copy(customersList = list) }
            }
        }
        viewModelScope.launch {
            repository.vouchers.collect { voucherList ->
                _uiState.update { it.copy(vouchers = voucherList) }
            }
        }
        viewModelScope.launch {
            repository.isFirebaseConnected.collect { connected ->
                _uiState.update { it.copy(isFirebaseConnected = connected) }
            }
        }
    }

    // --- Authentication & Role Switching ---
    fun showLoginDialog(show: Boolean) {
        _uiState.update { it.copy(isLoginDialogVisible = show) }
    }

    fun login(role: UserRole, email: String = "", name: String = "") {
        val user = repository.login(role, email, name)
        _uiState.update {
            it.copy(
                currentUser = user,
                isLoginDialogVisible = false,
                appMode = if (role == UserRole.CUSTOMER) AppMode.BUYER else AppMode.SELLER,
                buyerTab = BuyerTab.HOME,
                sellerTab = SellerTab.PRODUCTS
            )
        }
    }

    fun signInWithGoogleSuccess(name: String, email: String, photoUrl: String = "") {
        repository.setFirebaseUser(name, email, photoUrl)
        _uiState.update {
            it.copy(
                isLoginDialogVisible = false,
                appMode = AppMode.BUYER,
                buyerTab = BuyerTab.HOME
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update {
            it.copy(
                currentUser = null,
                appMode = AppMode.BUYER,
                buyerTab = BuyerTab.HOME
            )
        }
    }

    // --- Mode & Navigation ---
    fun toggleAppMode() {
        _uiState.update {
            val nextMode = if (it.appMode == AppMode.BUYER) AppMode.SELLER else AppMode.BUYER
            it.copy(appMode = nextMode)
        }
    }

    fun setBuyerTab(tab: BuyerTab) {
        _uiState.update { it.copy(buyerTab = tab) }
    }

    fun setSellerTab(tab: SellerTab) {
        _uiState.update { it.copy(sellerTab = tab) }
    }

    // --- Buyer Actions ---
    fun setOrderType(type: OrderType) {
        _uiState.update { it.copy(orderType = type) }
    }

    fun selectOutlet(outlet: Outlet) {
        _uiState.update { it.copy(selectedOutlet = outlet, isOutletPickerVisible = false) }
    }

    fun showOutletPicker(show: Boolean) {
        _uiState.update { it.copy(isOutletPickerVisible = show) }
    }

    fun selectCategory(category: FoodCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openProductDetail(product: ProductItem) {
        _uiState.update { it.copy(selectedProductForDetail = product) }
    }

    fun closeProductDetail() {
        _uiState.update { it.copy(selectedProductForDetail = null) }
    }

    fun addToCart(cartItem: CartItem) {
        repository.addToCart(cartItem)
        closeProductDetail()
    }

    fun updateCartQuantity(cartId: String, newQty: Int) {
        repository.updateCartQuantity(cartId, newQty)
    }

    fun showCheckout(show: Boolean) {
        _uiState.update { it.copy(isCheckoutVisible = show) }
    }

    fun selectVoucher(voucher: Voucher?) {
        _uiState.update { it.copy(selectedVoucher = voucher, isVoucherDialogVisible = false) }
    }

    fun showVoucherDialog(show: Boolean) {
        _uiState.update { it.copy(isVoucherDialogVisible = show) }
    }

    fun toggleUsePoints() {
        _uiState.update { it.copy(usePoints = !it.usePoints) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun processCheckout() {
        val state = _uiState.value
        val defaultAddress = state.userProfile.addresses.firstOrNull { it.isDefault } ?: state.userProfile.addresses.firstOrNull()
        val order = repository.createOrder(
            outlet = state.selectedOutlet,
            orderType = state.orderType,
            deliveryAddress = if (state.orderType == OrderType.DELIVERY) defaultAddress else null,
            paymentMethod = state.selectedPaymentMethod,
            appliedVoucher = state.selectedVoucher,
            usePoints = state.usePoints
        )

        _uiState.update {
            it.copy(
                isCheckoutVisible = false,
                latestCreatedOrder = order,
                isSuccessDialogVisible = true,
                selectedVoucher = null,
                usePoints = false,
                buyerTab = BuyerTab.ORDERS
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isSuccessDialogVisible = false) }
    }

    fun toggleFavorite(productId: String) {
        repository.toggleFavorite(productId)
    }

    fun showAddressDialog(show: Boolean) {
        _uiState.update { it.copy(isAddressDialogVisible = show) }
    }

    fun saveAddress(address: SavedAddress) {
        repository.addSavedAddress(address)
        _uiState.update { it.copy(isAddressDialogVisible = false) }
    }

    fun reorderOrder(order: OrderItem) {
        for (item in order.items) {
            repository.addToCart(item)
        }
        _uiState.update { it.copy(buyerTab = BuyerTab.CATALOG, isCheckoutVisible = true) }
    }

    // --- Product & Stock Management Actions (Owner & Employee) ---
    fun openAddProduct() {
        _uiState.update { it.copy(isAddEditProductVisible = true, productToEdit = null) }
    }

    fun openEditProduct(product: ProductItem) {
        _uiState.update { it.copy(isAddEditProductVisible = true, productToEdit = product) }
    }

    fun closeAddEditProduct() {
        _uiState.update { it.copy(isAddEditProductVisible = false, productToEdit = null) }
    }

    fun saveProduct(product: ProductItem) {
        if (_uiState.value.productToEdit != null) {
            repository.updateProduct(product)
        } else {
            repository.addProduct(product)
        }
        closeAddEditProduct()
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
    }

    fun toggleProductAvailability(productId: String) {
        repository.toggleProductAvailability(productId)
    }

    fun setMerchantStockFilter(filter: String) {
        _uiState.update { it.copy(merchantStockFilter = filter) }
    }

    fun openRestockDialog(product: ProductItem) {
        _uiState.update { it.copy(isRestockDialogVisible = true, productToRestock = product) }
    }

    fun closeRestockDialog() {
        _uiState.update { it.copy(isRestockDialogVisible = false, productToRestock = null) }
    }

    fun restockProduct(productId: String, addedQty: Int) {
        repository.restockProduct(productId, addedQty)
        closeRestockDialog()
    }

    // --- Seller Order Management ---
    fun advanceOrderStatus(orderId: String, currentStatus: OrderStatus) {
        val nextStatus = when (currentStatus) {
            OrderStatus.PENDING -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> OrderStatus.COMPLETED
            OrderStatus.CANCELLED -> OrderStatus.CANCELLED
        }
        repository.updateOrderStatus(orderId, nextStatus)
    }

    fun cancelOrder(orderId: String) {
        repository.updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    // --- Reporting & Customer Directory (Owner) ---
    fun setReportingPeriod(period: String) {
        _uiState.update { it.copy(reportingPeriod = period) }
    }

    fun getReportingStats(): ReportingSummary {
        return repository.getReportingStats(_uiState.value.reportingPeriod)
    }

    fun setCustomerSearchQuery(query: String) {
        _uiState.update { it.copy(customerSearchQuery = query) }
    }

    fun addVoucher(voucher: Voucher) {
        repository.addVoucher(voucher)
        _uiState.update { it.copy(isAddVoucherDialogVisible = false) }
    }

    fun deleteVoucher(voucherId: String) {
        repository.deleteVoucher(voucherId)
    }

    fun showAddVoucherDialog(show: Boolean) {
        _uiState.update { it.copy(isAddVoucherDialogVisible = show) }
    }
}
