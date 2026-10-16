package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ChanChanRepository {

    private val _outlets = MutableStateFlow(
        listOf(
            Outlet("out-1", "ChanChan+ Central Park", "Lantai LG, Central Park Mall, Jakarta Barat", 1.2),
            Outlet("out-2", "ChanChan+ Senopati", "Jl. Senopati No. 88, Kebayoran Baru, Jakarta Selatan", 2.8),
            Outlet("out-3", "ChanChan+ Mall Kelapa Gading", "Gading Walk, Kelapa Gading, Jakarta Utara", 5.4),
            Outlet("out-4", "ChanChan+ Gading Serpong", "Ruko Fluorite Blok FR No. 12, Tangerang", 8.1),
            Outlet("out-5", "ChanChan+ Sudirman Tower", "Lobby B, Wisma Sudirman Kav 34, Jakarta Pusat", 0.9)
        )
    )
    val outlets: StateFlow<List<Outlet>> = _outlets.asStateFlow()

    private val _products = MutableStateFlow<List<ProductItem>>(getInitialProducts())
    val products: StateFlow<List<ProductItem>> = _products.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderItem>>(getInitialOrders())
    val orders: StateFlow<List<OrderItem>> = _orders.asStateFlow()

    private val _vouchers = MutableStateFlow(
        listOf(
            Voucher(
                id = "v-1",
                code = "SEJIWAHEMAT",
                title = "Diskon 30% Jiwa Toast & Kopi",
                description = "Min. belanja Rp 40.000, maks. potongan Rp 18.000",
                minSpend = 40000L,
                discountPercent = 30,
                maxDiscount = 18000L
            ),
            Voucher(
                id = "v-2",
                code = "GRATISONGKIR",
                title = "Gratis Ongkir s/d Rp 12.000",
                description = "Khusus pengantaran delivery pesanan makanan & minuman",
                minSpend = 30000L,
                discountAmount = 12000L
            ),
            Voucher(
                id = "v-3",
                code = "KOPIKENYANG",
                title = "Potongan Langsung Rp 10.000",
                description = "Untuk pembelian combo apa saja tanpa batas maksimal",
                minSpend = 35000L,
                discountAmount = 10000L
            ),
            Voucher(
                id = "v-4",
                code = "CHANPLUSBARU",
                title = "Cashback Spesial 50%",
                description = "Maks. cashback 25.000 koin ChanPoints",
                minSpend = 50000L,
                discountPercent = 50,
                maxDiscount = 25000L
            )
        )
    )
    val vouchers: StateFlow<List<Voucher>> = _vouchers.asStateFlow()

    // --- Product Management (Merchant / Seller CRUD) ---
    fun addProduct(product: ProductItem) {
        _products.update { current ->
            listOf(product) + current
        }
    }

    fun updateProduct(product: ProductItem) {
        _products.update { current ->
            current.map { if (it.id == product.id) product else it }
        }
    }

    fun deleteProduct(productId: String) {
        _products.update { current ->
            current.filterNot { it.id == productId }
        }
    }

    fun toggleProductAvailability(productId: String) {
        _products.update { current ->
            current.map {
                if (it.id == productId) it.copy(isAvailable = !it.isAvailable) else it
            }
        }
    }

    // --- Cart Management ---
    fun addToCart(cartItem: CartItem) {
        _cart.update { current ->
            val index = current.indexOfFirst {
                it.product.id == cartItem.product.id &&
                        it.selectedSize.name == cartItem.selectedSize.name &&
                        it.selectedSugar == cartItem.selectedSugar &&
                        it.selectedIce == cartItem.selectedIce &&
                        it.selectedToppings.map { t -> t.name }.sorted() == cartItem.selectedToppings.map { t -> t.name }.sorted() &&
                        it.notes == cartItem.notes
            }
            if (index != -1) {
                current.toMutableList().apply {
                    this[index] = this[index].copy(quantity = this[index].quantity + cartItem.quantity)
                }
            } else {
                current + cartItem
            }
        }
    }

    fun updateCartQuantity(cartId: String, newQty: Int) {
        _cart.update { current ->
            if (newQty <= 0) {
                current.filterNot { it.cartId == cartId }
            } else {
                current.map {
                    if (it.cartId == cartId) it.copy(quantity = newQty) else it
                }
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // --- Order Management ---
    fun createOrder(
        outlet: Outlet,
        orderType: OrderType,
        deliveryAddress: SavedAddress?,
        paymentMethod: PaymentMethod,
        appliedVoucher: Voucher?,
        usePoints: Boolean
    ): OrderItem {
        val items = _cart.value
        val subtotal = items.sumOf { it.totalPrice }
        val voucherDiscount = appliedVoucher?.calculateDiscount(subtotal) ?: 0L
        val deliveryFee = if (orderType == OrderType.DELIVERY) 12000L else 0L
        val taxAmount = ((subtotal - voucherDiscount).coerceAtLeast(0L) * 0.10).toLong() // 10% PB1 tax

        val userPts = _userProfile.value.points
        val pointsDeduction = if (usePoints) {
            val balanceNeeded = (subtotal + deliveryFee + taxAmount - voucherDiscount).coerceAtLeast(0L)
            minOf(userPts, balanceNeeded)
        } else 0L

        val totalPaid = (subtotal + deliveryFee + taxAmount - voucherDiscount - pointsDeduction).coerceAtLeast(0L)

        val randomCode = (1000..9999).random()
        val orderNum = "CC-${(100000..999999).random()}"
        val pickupPin = "${(100..999).random()}"

        val newOrder = OrderItem(
            orderId = UUID.randomUUID().toString(),
            orderNumber = orderNum,
            items = items,
            orderType = orderType,
            status = OrderStatus.PENDING,
            outlet = outlet,
            deliveryAddress = deliveryAddress,
            subtotal = subtotal,
            taxAmount = taxAmount,
            deliveryFee = deliveryFee,
            discountAmount = voucherDiscount,
            pointsDiscount = pointsDeduction,
            totalPaid = totalPaid,
            paymentMethod = paymentMethod,
            timestamp = System.currentTimeMillis(),
            pickupPin = pickupPin,
            estimatedMinutes = if (orderType == OrderType.DELIVERY) 30 else 15,
            customerName = _userProfile.value.name
        )

        // Deduct points used & award points earned (5% of total paid)
        val earnedPoints = (totalPaid * 0.05).toLong()
        _userProfile.update { current ->
            current.copy(
                points = (current.points - pointsDeduction + earnedPoints).coerceAtLeast(0L)
            )
        }

        _orders.update { current ->
            listOf(newOrder) + current
        }

        clearCart()
        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { current ->
            current.map {
                if (it.orderId == orderId) it.copy(status = newStatus) else it
            }
        }
    }

    // --- Personalization ---
    fun toggleFavorite(productId: String) {
        _userProfile.update { current ->
            val updatedFavorites = if (current.favoriteProductIds.contains(productId)) {
                current.favoriteProductIds - productId
            } else {
                current.favoriteProductIds + productId
            }
            current.copy(favoriteProductIds = updatedFavorites)
        }
    }

    fun addSavedAddress(address: SavedAddress) {
        _userProfile.update { current ->
            val updated = current.addresses + address
            current.copy(addresses = updated)
        }
    }

    fun updateUserProfile(name: String, phone: String, email: String) {
        _userProfile.update { current ->
            current.copy(name = name, phone = phone, email = email)
        }
    }

    // --- Reporting & Analytics ---
    fun getReportingStats(period: String = "Hari Ini"): ReportingSummary {
        val allOrders = _orders.value
        val completedOrders = allOrders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.READY || it.status == OrderStatus.PREPARING }

        val totalRevenue = completedOrders.sumOf { it.totalPaid }
        val totalCost = completedOrders.flatMap { it.items }.sumOf { it.product.costPrice * it.quantity }
        val grossProfit = totalRevenue - totalCost
        val totalOrdersCount = completedOrders.size
        val aov = if (totalOrdersCount > 0) totalRevenue / totalOrdersCount else 0L

        // Best seller products calculation
        val productSalesMap = mutableMapOf<String, Pair<ProductItem, Int>>()
        for (order in completedOrders) {
            for (cartItem in order.items) {
                val existing = productSalesMap[cartItem.product.id]
                val currentCount = existing?.second ?: 0
                productSalesMap[cartItem.product.id] = Pair(cartItem.product, currentCount + cartItem.quantity)
            }
        }

        val topProducts = productSalesMap.values
            .sortedByDescending { it.second }
            .take(5)
            .map { (product, soldQty) ->
                TopSellingProduct(product, soldQty, soldQty * product.price)
            }

        // Daily trend data for bar chart
        val dailyTrend = listOf(
            DailySalesPoint("Sen", 1850000L, 48),
            DailySalesPoint("Sel", 2120000L, 56),
            DailySalesPoint("Rab", 2450000L, 64),
            DailySalesPoint("Kam", 2280000L, 59),
            DailySalesPoint("Jum", 3150000L, 82),
            DailySalesPoint("Sab", 3890000L, 98),
            DailySalesPoint("Min (Hari Ini)", totalRevenue.coerceAtLeast(3420000L), totalOrdersCount + 74)
        )

        return ReportingSummary(
            totalRevenue = if (totalRevenue > 0) totalRevenue else 3420000L,
            totalOrders = if (totalOrdersCount > 0) totalOrdersCount else 74,
            averageOrderValue = if (aov > 0) aov else 46200L,
            grossProfit = if (grossProfit > 0) grossProfit else 1540000L,
            topSellingProducts = if (topProducts.isNotEmpty()) topProducts else getDefaultTopProducts(),
            dailySalesTrend = dailyTrend
        )
    }

    private fun getDefaultTopProducts(): List<TopSellingProduct> {
        val prodList = _products.value
        return listOf(
            TopSellingProduct(prodList.getOrNull(0) ?: getInitialProducts()[0], 142, 142 * 28000L),
            TopSellingProduct(prodList.getOrNull(2) ?: getInitialProducts()[2], 118, 118 * 22000L),
            TopSellingProduct(prodList.getOrNull(1) ?: getInitialProducts()[1], 95, 95 * 32000L),
            TopSellingProduct(prodList.getOrNull(5) ?: getInitialProducts()[5], 78, 78 * 26000L),
            TopSellingProduct(prodList.getOrNull(4) ?: getInitialProducts()[4], 64, 64 * 30000L)
        )
    }

    companion object {
        fun formatRupiah(amount: Long): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount).replace("Rp", "Rp ")
        }
    }

    private fun getInitialProducts(): List<ProductItem> {
        return listOf(
            ProductItem(
                id = "p-1",
                name = "Kopi Susu Sahabat Sejiwa",
                category = FoodCategory.KOPI,
                price = 22000L,
                originalPrice = 25000L,
                description = "Espresso ganda berpadu susu segar gurih dan gula aren murni khas Nusantara.",
                emoji = "☕",
                rating = 4.9f,
                reviewsCount = 4280,
                isBestSeller = true,
                prepTimeMinutes = 8,
                stockQuantity = 95
            ),
            ProductItem(
                id = "p-2",
                name = "Crispy Beef Hamburg Toast",
                category = FoodCategory.TOAST,
                price = 34000L,
                originalPrice = 38000L,
                description = "Roti brioche panggang garing mentega isi daging sapi hamburg juicy, keju leleh, dan saus spesial.",
                emoji = "🥪",
                rating = 4.9f,
                reviewsCount = 3120,
                isBestSeller = true,
                prepTimeMinutes = 15,
                stockQuantity = 45
            ),
            ProductItem(
                id = "p-3",
                name = "Combo Hemat Jiwa: Toast + Kopi",
                category = FoodCategory.PROMO,
                price = 45000L,
                originalPrice = 56000L,
                description = "Paket komplit 1 Crispy Beef Toast + 1 Kopi Susu Sahabat Regular. Hemat 20%!",
                emoji = "🏷️",
                rating = 5.0f,
                reviewsCount = 5900,
                isBestSeller = true,
                isNew = true,
                prepTimeMinutes = 15,
                stockQuantity = 50
            ),
            ProductItem(
                id = "p-4",
                name = "Creamy Egg Mayo & Cheese Toast",
                category = FoodCategory.TOAST,
                price = 28000L,
                description = "Telur lembut berpadu racikan mayones jepang gurih, parsley, dan keju cheddar panggang.",
                emoji = "🥪",
                rating = 4.8f,
                reviewsCount = 1890,
                prepTimeMinutes = 10,
                stockQuantity = 60
            ),
            ProductItem(
                id = "p-5",
                name = "Spicy Korean Bulgogi Toast",
                category = FoodCategory.TOAST,
                price = 36000L,
                description = "Irisan daging sapi marinasi bulgogi pedas manis bertabur wijen sangrai dan selada segar.",
                emoji = "🥪",
                rating = 4.9f,
                reviewsCount = 1450,
                isNew = true,
                prepTimeMinutes = 14,
                stockQuantity = 38
            ),
            ProductItem(
                id = "p-6",
                name = "Caramel Macchiato Iced",
                category = FoodCategory.KOPI,
                price = 28000L,
                originalPrice = 32000L,
                description = "Espresso kaya aroma berlayer vanilla milk lembut disiram saus caramel kental.",
                emoji = "☕",
                rating = 4.8f,
                reviewsCount = 2100,
                prepTimeMinutes = 8,
                stockQuantity = 75
            ),
            ProductItem(
                id = "p-7",
                name = "Matcha Green Tea Latte",
                category = FoodCategory.NON_COFFEE,
                price = 26000L,
                description = "Matcha premium Kyoto otentik yang harum dipadukan dengan fresh milk lembut.",
                emoji = "🧋",
                rating = 4.7f,
                reviewsCount = 980,
                prepTimeMinutes = 8,
                stockQuantity = 80
            ),
            ProductItem(
                id = "p-8",
                name = "Earl Grey Brown Sugar Milk Tea",
                category = FoodCategory.NON_COFFEE,
                price = 25000L,
                description = "Seduhan teh Earl Grey wangi bergamot dengan sirup brown sugar legit dan boba kenyal.",
                emoji = "🧋",
                rating = 4.8f,
                reviewsCount = 1670,
                isBestSeller = true,
                prepTimeMinutes = 9,
                stockQuantity = 85
            ),
            ProductItem(
                id = "p-9",
                name = "Chicken Nanban Rice Bowl",
                category = FoodCategory.MEALS,
                price = 39000L,
                originalPrice = 44000L,
                description = "Ayam goreng renyah disiram saus nanban manis gurih dan saus tartar telur segar di atas nasi hangat.",
                emoji = "🍛",
                rating = 4.9f,
                reviewsCount = 2240,
                isBestSeller = true,
                prepTimeMinutes = 18,
                stockQuantity = 35
            ),
            ProductItem(
                id = "p-10",
                name = "Daging Sapi Lada Hitam Rice Bowl",
                category = FoodCategory.MEALS,
                price = 42000L,
                description = "Daging sapi tenderloin empuk ditumis dengan racikan black pepper aromatik dan paprika.",
                emoji = "🍛",
                rating = 4.8f,
                reviewsCount = 1150,
                prepTimeMinutes = 16,
                stockQuantity = 30
            ),
            ProductItem(
                id = "p-11",
                name = "Butter Croissant Crispy",
                category = FoodCategory.BAKERY,
                price = 20000L,
                description = "Pastry berlapis renyah dengan aroma wangi butter Prancis yang meleleh di mulut.",
                emoji = "🥐",
                rating = 4.7f,
                reviewsCount = 890,
                prepTimeMinutes = 6,
                stockQuantity = 40
            ),
            ProductItem(
                id = "p-12",
                name = "Truffle Fries with Mayo",
                category = FoodCategory.BAKERY,
                price = 24000L,
                description = "Kentang goreng renyah bumbu sea salt wangi minyak truffle premium disajikan dengan garlic mayo.",
                emoji = "🍟",
                rating = 4.9f,
                reviewsCount = 1320,
                prepTimeMinutes = 10,
                stockQuantity = 55
            )
        )
    }

    private fun getInitialOrders(): List<OrderItem> {
        val initialProds = getInitialProducts()
        val defaultOutlet = _outlets.value[0]
        val address = UserProfile().addresses[0]

        return listOf(
            OrderItem(
                orderId = "ord-active-1",
                orderNumber = "CC-849201",
                items = listOf(
                    CartItem(
                        product = initialProds[0],
                        selectedSize = VariantOption(name = "Regular"),
                        selectedSugar = "Less Sugar (70%)",
                        selectedIce = "Normal Ice",
                        quantity = 1
                    ),
                    CartItem(
                        product = initialProds[1],
                        selectedSize = VariantOption(name = "Regular"),
                        notes = "Tolong saus dipisah ya kak",
                        quantity = 1
                    )
                ),
                orderType = OrderType.PICKUP,
                status = OrderStatus.PREPARING,
                outlet = defaultOutlet,
                deliveryAddress = null,
                subtotal = 56000L,
                taxAmount = 5600L,
                deliveryFee = 0L,
                discountAmount = 10000L,
                pointsDiscount = 0L,
                totalPaid = 51600L,
                paymentMethod = PaymentMethod.QRIS,
                timestamp = System.currentTimeMillis() - (12 * 60 * 1000), // 12 mins ago
                pickupPin = "742",
                estimatedMinutes = 8,
                customerName = "Dimas Prasetyo"
            ),
            OrderItem(
                orderId = "ord-hist-2",
                orderNumber = "CC-739104",
                items = listOf(
                    CartItem(
                        product = initialProds[2], // Combo
                        selectedSize = VariantOption(name = "Large", extraPrice = 4000L),
                        quantity = 2
                    )
                ),
                orderType = OrderType.DELIVERY,
                status = OrderStatus.COMPLETED,
                outlet = defaultOutlet,
                deliveryAddress = address,
                subtotal = 98000L,
                taxAmount = 9800L,
                deliveryFee = 12000L,
                discountAmount = 18000L,
                pointsDiscount = 2000L,
                totalPaid = 99800L,
                paymentMethod = PaymentMethod.GOPAY,
                timestamp = System.currentTimeMillis() - (28 * 60 * 60 * 1000), // yesterday
                pickupPin = "581",
                estimatedMinutes = 0,
                customerName = "Dimas Prasetyo"
            )
        )
    }
}

data class TopSellingProduct(
    val product: ProductItem,
    val quantitySold: Int,
    val revenueTotal: Long
)

data class DailySalesPoint(
    val dayLabel: String,
    val revenue: Long,
    val orderCount: Int
)

data class ReportingSummary(
    val totalRevenue: Long,
    val totalOrders: Int,
    val averageOrderValue: Long,
    val grossProfit: Long,
    val topSellingProducts: List<TopSellingProduct>,
    val dailySalesTrend: List<DailySalesPoint>
)
