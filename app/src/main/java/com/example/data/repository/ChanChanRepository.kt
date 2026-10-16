package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ChanChanRepository(private val context: Context? = null) {

    companion object {
        @Volatile
        private var INSTANCE: ChanChanRepository? = null

        fun getInstance(context: Context? = null): ChanChanRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChanChanRepository(context?.applicationContext).also { INSTANCE = it }
            }
        }

        fun formatRupiah(amount: Long): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.maximumFractionDigits = 0
            return format.format(amount).replace("Rp", "Rp ")
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // Initialize custom Firestore instance with named database ID
    val db: FirebaseFirestore? = try {
        context?.let { ctx ->
            val dbId = ctx.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        }
    } catch (e: Exception) {
        Log.e("ChanChanRepo", "Firestore init error: ${e.message}")
        null
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _isFirebaseConnected = MutableStateFlow(db != null)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()

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

    val demoAccounts = listOf(
        AppUser("usr-1", "Dimas Prasetyo", "dimas@chanchan.com", "+62 812-3456-7890", UserRole.CUSTOMER, "👤", "Jiwa Gold Member"),
        AppUser("usr-2", "Siti Rahma", "staff@chanchan.com", "+62 813-8899-1122", UserRole.EMPLOYEE, "☕", "Barista & Operational Staff"),
        AppUser("usr-3", "Hendra Wijaya", "owner@chanchan.com", "+62 811-9988-7766", UserRole.OWNER, "👑", "Store Owner & Managing Partner")
    )

    private val _currentUser = MutableStateFlow<AppUser?>(demoAccounts[0])
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _customersList = MutableStateFlow(
        listOf(
            CustomerInfo("c-1", "Dimas Prasetyo", "dimas@gmail.com", "0812-3456-7890", "Jiwa Gold Member", 14, 584000L, 3450L, "10 Jan 2026", "Hari ini"),
            CustomerInfo("c-2", "Sarah Amanda", "sarah.a@yahoo.com", "0813-7766-2211", "Jiwa Silver Member", 8, 312000L, 1200L, "15 Feb 2026", "Kemarin"),
            CustomerInfo("c-3", "Budi Santoso", "budi.s@outlook.com", "0856-1122-3344", "Jiwa Friend", 5, 185000L, 750L, "01 Mar 2026", "3 hari lalu"),
            CustomerInfo("c-4", "Rina Wijaya", "rina.w@gmail.com", "0817-9988-4455", "Jiwa Gold Member", 22, 940000L, 5100L, "05 Jan 2026", "Hari ini"),
            CustomerInfo("c-5", "Aditya Pratama", "adit.pratama@gmail.com", "0818-4455-6677", "Jiwa Friend", 3, 110000L, 400L, "28 Mar 2026", "5 hari lalu")
        )
    )
    val customersList: StateFlow<List<CustomerInfo>> = _customersList.asStateFlow()

    init {
        listenToFirestore()
    }

    private fun listenToFirestore() {
        val firestore = db ?: return

        // 1. Listen to Products Collection from Firestore
        firestore.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ChanChanRepo", "Firestore products error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    // Seed initial catalog to Firestore
                    seedInitialProducts(firestore)
                } else {
                    val loaded = snapshot.documents.mapNotNull { docToProduct(it) }
                    if (loaded.isNotEmpty()) {
                        _products.value = loaded
                    }
                }
            }
        }

        // 2. Listen to Orders Collection from Firestore
        firestore.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ChanChanRepo", "Firestore orders error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val loadedOrders = snapshot.documents.mapNotNull { docToOrder(it) }
                    if (loadedOrders.isNotEmpty()) {
                        _orders.value = loadedOrders
                    }
                }
            }

        // 3. Listen to Users Collection from Firestore
        firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ChanChanRepo", "Firestore users error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val loadedUsers = snapshot.documents.mapNotNull { docToCustomerInfo(it) }
                if (loadedUsers.isNotEmpty()) {
                    _customersList.value = loadedUsers
                }
            }
        }
    }

    private fun seedInitialProducts(firestore: FirebaseFirestore) {
        val initialList = getInitialProducts()
        for (item in initialList) {
            firestore.collection("products").document(item.id).set(productToMap(item))
                .addOnSuccessListener { Log.d("ChanChanRepo", "Seeded product: ${item.name}") }
                .addOnFailureListener { Log.w("ChanChanRepo", "Failed to seed ${item.name}: ${it.message}") }
        }
    }

    private fun productToMap(product: ProductItem): Map<String, Any?> {
        return mapOf(
            "id" to product.id,
            "name" to product.name,
            "category" to product.category.name,
            "price" to product.price,
            "costPrice" to product.costPrice,
            "description" to product.description,
            "emoji" to product.emoji,
            "isAvailable" to product.isAvailable,
            "rating" to product.rating.toDouble(),
            "reviewsCount" to product.reviewsCount,
            "isBestSeller" to product.isBestSeller,
            "isNew" to product.isNew,
            "prepTimeMinutes" to product.prepTimeMinutes,
            "stockQuantity" to product.stockQuantity,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun docToProduct(doc: DocumentSnapshot): ProductItem? {
        val name = doc.getString("name") ?: return null
        val catStr = doc.getString("category") ?: "TOAST"
        val category = try { FoodCategory.valueOf(catStr) } catch (e: Exception) { FoodCategory.TOAST }
        val price = doc.getLong("price") ?: 25000L
        val costPrice = doc.getLong("costPrice") ?: (price * 0.55).toLong()
        val desc = doc.getString("description") ?: ""
        val emoji = doc.getString("emoji") ?: "🥪"
        val isAvail = doc.getBoolean("isAvailable") ?: true
        val rating = doc.getDouble("rating")?.toFloat() ?: 4.8f
        val reviews = doc.getLong("reviewsCount")?.toInt() ?: 120
        val isBest = doc.getBoolean("isBestSeller") ?: false
        val isNew = doc.getBoolean("isNew") ?: false
        val prepTime = doc.getLong("prepTimeMinutes")?.toInt() ?: 12
        val stock = doc.getLong("stockQuantity")?.toInt() ?: 50

        return ProductItem(
            id = doc.id,
            name = name,
            category = category,
            price = price,
            costPrice = costPrice,
            description = desc,
            emoji = emoji,
            isAvailable = isAvail,
            rating = rating,
            reviewsCount = reviews,
            isBestSeller = isBest,
            isNew = isNew,
            prepTimeMinutes = prepTime,
            stockQuantity = stock
        )
    }

    private fun orderToMap(order: OrderItem): Map<String, Any?> {
        return mapOf(
            "orderId" to order.orderId,
            "orderNumber" to order.orderNumber,
            "customerName" to order.customerName,
            "status" to order.status.name,
            "orderType" to order.orderType.name,
            "subtotal" to order.subtotal,
            "taxAmount" to order.taxAmount,
            "deliveryFee" to order.deliveryFee,
            "discountAmount" to order.discountAmount,
            "pointsDiscount" to order.pointsDiscount,
            "totalPaid" to order.totalPaid,
            "paymentMethod" to order.paymentMethod.name,
            "pickupPin" to order.pickupPin,
            "timestamp" to order.timestamp,
            "outletName" to order.outlet.name,
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    private fun docToOrder(doc: DocumentSnapshot): OrderItem? {
        val orderId = doc.getString("orderId") ?: doc.id
        val orderNumber = doc.getString("orderNumber") ?: "CC-${doc.id.take(6).uppercase()}"
        val customerName = doc.getString("customerName") ?: "Dimas Prasetyo"
        val statusStr = doc.getString("status") ?: "PENDING"
        val status = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.PENDING }
        val typeStr = doc.getString("orderType") ?: "PICKUP"
        val orderType = try { OrderType.valueOf(typeStr) } catch (e: Exception) { OrderType.PICKUP }
        val subtotal = doc.getLong("subtotal") ?: 35000L
        val totalPaid = doc.getLong("totalPaid") ?: subtotal
        val pickupPin = doc.getString("pickupPin") ?: "742"
        val paymentStr = doc.getString("paymentMethod") ?: "QRIS"
        val paymentMethod = try { PaymentMethod.valueOf(paymentStr) } catch (e: Exception) { PaymentMethod.QRIS }
        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        val outletName = doc.getString("outletName") ?: "ChanChan+ Central Park"
        val outlet = _outlets.value.firstOrNull { it.name == outletName } ?: _outlets.value.first()

        val sampleProd = _products.value.firstOrNull() ?: getInitialProducts().first()
        val items = listOf(
            CartItem(product = sampleProd, selectedSize = VariantOption(name = "Regular"), quantity = 1)
        )

        return OrderItem(
            orderId = orderId,
            orderNumber = orderNumber,
            items = items,
            orderType = orderType,
            status = status,
            outlet = outlet,
            deliveryAddress = null,
            subtotal = subtotal,
            taxAmount = (subtotal * 0.1).toLong(),
            deliveryFee = if (orderType == OrderType.DELIVERY) 12000L else 0L,
            discountAmount = 0L,
            pointsDiscount = 0L,
            totalPaid = totalPaid,
            paymentMethod = paymentMethod,
            timestamp = timestamp,
            pickupPin = pickupPin,
            customerName = customerName
        )
    }

    private fun docToCustomerInfo(doc: DocumentSnapshot): CustomerInfo? {
        val name = doc.getString("name") ?: return null
        val email = doc.getString("email") ?: ""
        val phone = doc.getString("phone") ?: ""
        val tier = doc.getString("tier") ?: "Jiwa Friend"
        val totalOrders = doc.getLong("totalOrders")?.toInt() ?: 1
        val totalSpent = doc.getLong("totalSpent") ?: 0L
        val points = doc.getLong("pointsBalance") ?: 0L
        val joined = doc.getString("joinedDate") ?: "12 Jan 2026"
        val lastOrder = doc.getString("lastOrderDate") ?: "Hari ini"

        return CustomerInfo(
            id = doc.id,
            name = name,
            email = email,
            phone = phone,
            tier = tier,
            totalOrders = totalOrders,
            totalSpent = totalSpent,
            pointsBalance = points,
            joinedDate = joined,
            lastOrderDate = lastOrder
        )
    }

    fun login(role: UserRole, email: String = "", name: String = ""): AppUser {
        val user = when (role) {
            UserRole.CUSTOMER -> demoAccounts[0].copy(
                email = if (email.isNotEmpty()) email else demoAccounts[0].email,
                name = if (name.isNotEmpty()) name else demoAccounts[0].name
            )
            UserRole.EMPLOYEE -> demoAccounts[1]
            UserRole.OWNER -> demoAccounts[2]
        }
        _currentUser.value = user

        // Persist user to Firestore
        db?.collection("users")?.document(user.id)?.set(
            mapOf(
                "userId" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role.name,
                "tier" to user.title,
                "totalSpent" to 584000L,
                "totalOrders" to 14,
                "pointsBalance" to 3450L,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        return user
    }

    fun setFirebaseUser(name: String, email: String, photoUrl: String = "") {
        val user = AppUser(
            id = auth.currentUser?.uid ?: UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = "+62 812-3456-7890",
            role = UserRole.CUSTOMER,
            avatarEmoji = "👤",
            title = "Jiwa Gold Member"
        )
        _currentUser.value = user
        _userProfile.update { it.copy(name = name, email = email) }

        db?.collection("users")?.document(user.id)?.set(
            mapOf(
                "userId" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role.name,
                "tier" to "Jiwa Gold Member",
                "pointsBalance" to _userProfile.value.points,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    fun restockProduct(productId: String, addedQuantity: Int) {
        _products.update { current ->
            current.map {
                if (it.id == productId) {
                    val newQty = (it.stockQuantity + addedQuantity).coerceAtLeast(0)
                    it.copy(stockQuantity = newQty, isAvailable = newQty > 0)
                } else it
            }
        }
        // Sync to Firestore
        db?.collection("products")?.document(productId)?.update(
            "stockQuantity", FieldValue.increment(addedQuantity.toLong()),
            "isAvailable", true,
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    fun addVoucher(voucher: Voucher) {
        _vouchers.update { listOf(voucher) + it }
    }

    fun deleteVoucher(voucherId: String) {
        _vouchers.update { it.filterNot { v -> v.id == voucherId } }
    }

    fun addProduct(product: ProductItem) {
        _products.update { current ->
            listOf(product) + current.filterNot { it.id == product.id }
        }
        // Write to Firestore
        db?.collection("products")?.document(product.id)?.set(productToMap(product))
    }

    fun updateProduct(product: ProductItem) {
        _products.update { current ->
            current.map { if (it.id == product.id) product else it }
        }
        // Update in Firestore
        db?.collection("products")?.document(product.id)?.set(productToMap(product))
    }

    fun deleteProduct(productId: String) {
        _products.update { current ->
            current.filterNot { it.id == productId }
        }
        // Delete in Firestore
        db?.collection("products")?.document(productId)?.delete()
    }

    fun toggleProductAvailability(productId: String) {
        var updatedAvail = false
        _products.update { current ->
            current.map {
                if (it.id == productId) {
                    updatedAvail = !it.isAvailable
                    it.copy(isAvailable = updatedAvail)
                } else it
            }
        }
        // Sync availability to Firestore
        db?.collection("products")?.document(productId)?.update(
            "isAvailable", updatedAvail,
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    // --- Cart Management ---
    fun addToCart(cartItem: CartItem) {
        _cart.update { current ->
            val index = current.indexOfFirst {
                it.product.id == cartItem.product.id &&
                        it.selectedSize.name == cartItem.selectedSize.name &&
                        it.selectedSugar == cartItem.selectedSugar &&
                        it.selectedIce == cartItem.selectedIce &&
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

    fun updateCartQuantity(cartId: String, newQuantity: Int) {
        _cart.update { current ->
            if (newQuantity <= 0) {
                current.filterNot { it.cartId == cartId }
            } else {
                current.map { if (it.cartId == cartId) it.copy(quantity = newQuantity) else it }
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // --- Order Checkout & Order Placement ---
    fun createOrder(
        outlet: Outlet,
        orderType: OrderType,
        deliveryAddress: SavedAddress?,
        paymentMethod: PaymentMethod,
        appliedVoucher: Voucher?,
        usePoints: Boolean
    ): OrderItem {
        val currentCart = _cart.value
        val subtotal = currentCart.sumOf { it.totalPrice }
        val tax = (subtotal * 0.10).toLong()
        val deliveryFee = if (orderType == OrderType.DELIVERY) 12000L else 0L
        val voucherDiscount = appliedVoucher?.calculateDiscount(subtotal) ?: 0L
        val userPts = _userProfile.value.points
        val pointsDiscount = if (usePoints) userPts.coerceAtMost(subtotal) else 0L

        val total = (subtotal + tax + deliveryFee - voucherDiscount - pointsDiscount).coerceAtLeast(0L)
        val orderNum = "CC-" + (100000 + Random().nextInt(900000)).toString()
        val pin = (100 + Random().nextInt(900)).toString()

        val order = OrderItem(
            orderId = UUID.randomUUID().toString(),
            orderNumber = orderNum,
            items = currentCart,
            orderType = orderType,
            status = OrderStatus.PENDING,
            outlet = outlet,
            deliveryAddress = deliveryAddress,
            subtotal = subtotal,
            taxAmount = tax,
            deliveryFee = deliveryFee,
            discountAmount = voucherDiscount,
            pointsDiscount = pointsDiscount,
            totalPaid = total,
            paymentMethod = paymentMethod,
            timestamp = System.currentTimeMillis(),
            pickupPin = pin,
            customerName = _currentUser.value?.name ?: _userProfile.value.name
        )

        _orders.update { listOf(order) + it }

        // Deduct points if used, or reward new points (10% cashback in points)
        _userProfile.update { current ->
            val earnedPoints = (total * 0.05).toLong()
            val newPoints = if (usePoints) (current.points - pointsDiscount) + earnedPoints else current.points + earnedPoints
            current.copy(points = newPoints)
        }

        // Save order to Firestore Database
        db?.collection("orders")?.document(order.orderId)?.set(orderToMap(order))
            ?.addOnSuccessListener { Log.d("ChanChanRepo", "Order saved to Firestore: ${order.orderNumber}") }
            ?.addOnFailureListener { Log.w("ChanChanRepo", "Failed to save order to Firestore: ${it.message}") }

        clearCart()
        return order
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.update { current ->
            current.map {
                if (it.orderId == orderId) it.copy(status = newStatus) else it
            }
        }
        // Sync status change to Firestore
        db?.collection("orders")?.document(orderId)?.update(
            "status", newStatus.name,
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    fun toggleFavorite(productId: String) {
        _userProfile.update { current ->
            val favs = current.favoriteProductIds.toMutableSet()
            if (favs.contains(productId)) favs.remove(productId) else favs.add(productId)
            current.copy(favoriteProductIds = favs)
        }
    }

    fun addSavedAddress(address: SavedAddress) {
        _userProfile.update { current ->
            current.copy(addresses = current.addresses + address)
        }
    }

    // --- Seller Reporting Analytics ---
    fun getReportingStats(period: String = "Hari Ini"): ReportingSummary {
        val multiplier = when (period) {
            "Minggu Ini" -> 5.8
            "Bulan Ini" -> 22.4
            else -> 1.0
        }

        val baseRev = 2845000L
        val totalRevenue = (baseRev * multiplier).toLong()
        val totalOrders = (94 * multiplier).toInt()
        val aov = if (totalOrders > 0) totalRevenue / totalOrders else 32000L
        val grossProfit = (totalRevenue * 0.45).toLong()

        val currentProds = _products.value
        val topSellers = listOf(
            TopSellingProduct(currentProds.getOrNull(0) ?: getInitialProducts()[0], (42 * multiplier).toInt(), (1050000L * multiplier).toLong()),
            TopSellingProduct(currentProds.getOrNull(1) ?: getInitialProducts()[1], (38 * multiplier).toInt(), (1216000L * multiplier).toLong()),
            TopSellingProduct(currentProds.getOrNull(2) ?: getInitialProducts()[2], (29 * multiplier).toInt(), (1131000L * multiplier).toLong()),
            TopSellingProduct(currentProds.getOrNull(3) ?: getInitialProducts()[3], (24 * multiplier).toInt(), (576000L * multiplier).toLong()),
            TopSellingProduct(currentProds.getOrNull(4) ?: getInitialProducts()[4], (18 * multiplier).toInt(), (504000L * multiplier).toLong())
        )

        val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val trend = days.mapIndexed { i, day ->
            val rev = (1800000L + (i * 320000L) + (if (i >= 5) 850000L else 0L))
            DailySalesPoint(dayLabel = "$day (${i + 10} Okt)", revenue = rev, orderCount = (rev / 32000).toInt())
        }

        return ReportingSummary(
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            averageOrderValue = aov,
            grossProfit = grossProfit,
            topSellingProducts = topSellers,
            dailySalesTrend = trend
        )
    }

    private fun getInitialProducts(): List<ProductItem> {
        return listOf(
            ProductItem(
                id = "p-1",
                name = "Kopi Susu Senja Gula Aren",
                category = FoodCategory.KOPI,
                price = 22000L,
                originalPrice = 28000L,
                description = "Espresso premium robusta & arabika dengan susu segar creamy dan gula aren asli Sukabumi.",
                emoji = "☕",
                rating = 4.9f,
                reviewsCount = 1840,
                isBestSeller = true,
                prepTimeMinutes = 5,
                stockQuantity = 85
            ),
            ProductItem(
                id = "p-2",
                name = "Crispy Beef Bulgogi Toast",
                category = FoodCategory.TOAST,
                price = 36000L,
                originalPrice = 42000L,
                description = "Roti brioche panggang wangi butter dengan isian daging sapi bulgogi gurih, omelette telur lembut, dan keju lumer.",
                emoji = "🥪",
                rating = 4.9f,
                reviewsCount = 2100,
                isBestSeller = true,
                prepTimeMinutes = 12,
                stockQuantity = 45
            ),
            ProductItem(
                id = "p-3",
                name = "Combo Hemat: Toast + Kopi Susu",
                category = FoodCategory.PROMO,
                price = 49000L,
                originalPrice = 58000L,
                description = "Paket sarapan & nongkrong favorit: 1 Jiwa Toast Beef/Egg + 1 Kopi Susu Senja Regular.",
                emoji = "🏷️",
                rating = 4.8f,
                reviewsCount = 3450,
                isBestSeller = true,
                prepTimeMinutes = 14,
                stockQuantity = 50
            ),
            ProductItem(
                id = "p-4",
                name = "Creamy Matcha Latte",
                category = FoodCategory.NON_COFFEE,
                price = 26000L,
                description = "Teh hijau matcha Uji Kyoto asli berpadu dengan susu murni segar dan tekstur velvety creamy.",
                emoji = "🧋",
                rating = 4.8f,
                reviewsCount = 920,
                prepTimeMinutes = 5,
                stockQuantity = 60
            ),
            ProductItem(
                id = "p-5",
                name = "Egg and Cheese Toast Supreme",
                category = FoodCategory.TOAST,
                price = 28000L,
                description = "Roti panggang tebal isi fluffy scrambled egg, cheddar cheese slice leleh, dan saus spicy mayo gurih manis.",
                emoji = "🥪",
                rating = 4.7f,
                reviewsCount = 1100,
                prepTimeMinutes = 10,
                stockQuantity = 40
            ),
            ProductItem(
                id = "p-6",
                name = "Americano Double Shot",
                category = FoodCategory.KOPI,
                price = 18000L,
                description = "Dua shot espresso segar dengan aroma floral citrus dan crema tebal keemasan.",
                emoji = "☕",
                rating = 4.7f,
                reviewsCount = 680,
                prepTimeMinutes = 4,
                stockQuantity = 90
            ),
            ProductItem(
                id = "p-7",
                name = "Earl Grey Milk Tea with Boba",
                category = FoodCategory.NON_COFFEE,
                price = 27000L,
                description = "Black tea bergamot harum dipadukan fresh milk dan topping boba kenyal manis.",
                emoji = "🧋",
                rating = 4.8f,
                reviewsCount = 840,
                prepTimeMinutes = 6,
                stockQuantity = 55
            ),
            ProductItem(
                id = "p-8",
                name = "Smoked Beef Omelette Toast",
                category = FoodCategory.TOAST,
                price = 33000L,
                description = "Daging asap panggang harum asap kayu manis diselimuti omelette lembut telur segar.",
                emoji = "🥪",
                rating = 4.8f,
                reviewsCount = 1430,
                prepTimeMinutes = 12,
                stockQuantity = 35
            ),
            ProductItem(
                id = "p-9",
                name = "Chicken Katsu Curry Bowl",
                category = FoodCategory.MEALS,
                price = 42000L,
                description = "Nasi pulen Jepang hangat dengan fillet ayam crispy renyah disiram saus kari kental gurih.",
                emoji = "🍛",
                rating = 4.8f,
                reviewsCount = 760,
                prepTimeMinutes = 15,
                stockQuantity = 30
            ),
            ProductItem(
                id = "p-10",
                name = "Beef Teriyaki Rice Bowl",
                category = FoodCategory.MEALS,
                price = 45000L,
                description = "Irisan daging sapi empuk tumis saus teriyaki manis gurih dengan taburan wijen dan poached egg.",
                emoji = "🍛",
                rating = 4.9f,
                reviewsCount = 1040,
                prepTimeMinutes = 15,
                stockQuantity = 25
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
                timestamp = System.currentTimeMillis() - (12 * 60 * 1000),
                pickupPin = "742",
                estimatedMinutes = 8,
                customerName = "Dimas Prasetyo"
            ),
            OrderItem(
                orderId = "ord-hist-2",
                orderNumber = "CC-739104",
                items = listOf(
                    CartItem(
                        product = initialProds[2],
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
                timestamp = System.currentTimeMillis() - (28 * 60 * 60 * 1000),
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
