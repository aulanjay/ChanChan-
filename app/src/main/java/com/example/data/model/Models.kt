package com.example.data.model

import java.util.UUID

enum class FoodCategory(val displayName: String, val icon: String) {
    ALL("Semua Menu", "🍽️"),
    PROMO("Promo Hemat", "🏷️"),
    TOAST("Jiwa Toast", "🥪"),
    KOPI("Kopi Sejiwa", "☕"),
    NON_COFFEE("Tea & Choco", "🧋"),
    MEALS("Rice Bowls", "🍛"),
    BAKERY("Bakery & Sides", "🥐")
}

data class VariantOption(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val extraPrice: Long = 0L
)

data class ProductItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: FoodCategory,
    val price: Long,
    val originalPrice: Long? = null,
    val description: String,
    val emoji: String,
    val isAvailable: Boolean = true,
    val rating: Float = 4.8f,
    val reviewsCount: Int = 120,
    val isBestSeller: Boolean = false,
    val isNew: Boolean = false,
    val prepTimeMinutes: Int = 12,
    val stockQuantity: Int = 50,
    val costPrice: Long = (price * 0.55).toLong(),
    val sizeOptions: List<VariantOption> = listOf(
        VariantOption(name = "Regular", extraPrice = 0L),
        VariantOption(name = "Large", extraPrice = 4000L)
    ),
    val sugarLevels: List<String> = listOf("Normal (100%)", "Less Sugar (70%)", "Slight Sugar (50%)", "No Sugar (0%)"),
    val iceLevels: List<String> = listOf("Normal Ice", "Less Ice", "No Ice"),
    val availableToppings: List<VariantOption> = listOf(
        VariantOption(name = "Extra Shot Espresso", extraPrice = 5000L),
        VariantOption(name = "Brown Sugar Boba", extraPrice = 4000L),
        VariantOption(name = "Grass Jelly", extraPrice = 4000L),
        VariantOption(name = "Melted Cheddar Cheese", extraPrice = 6000L),
        VariantOption(name = "Extra Fried Egg", extraPrice = 5000L)
    )
)

data class CartItem(
    val cartId: String = UUID.randomUUID().toString(),
    val product: ProductItem,
    val selectedSize: VariantOption,
    val selectedSugar: String = "Normal (100%)",
    val selectedIce: String = "Normal Ice",
    val selectedToppings: List<VariantOption> = emptyList(),
    val notes: String = "",
    val quantity: Int = 1
) {
    val unitPrice: Long
        get() {
            val toppingsTotal = selectedToppings.sumOf { it.extraPrice }
            return product.price + selectedSize.extraPrice + toppingsTotal
        }

    val totalPrice: Long
        get() = unitPrice * quantity
}

enum class OrderStatus(val displayName: String, val badgeColorHex: Long) {
    PENDING("Menunggu Konfirmasi", 0xFFFFA000),
    PREPARING("Sedang Disiapkan", 0xFF1976D2),
    READY("Siap Diambil / Diantar", 0xFF7B1FA2),
    COMPLETED("Selesai", 0xFF2E7D32),
    CANCELLED("Dibatalkan", 0xFFD32F2F)
}

enum class OrderType(val label: String, val subtitle: String) {
    PICKUP("Pick Up", "Ambil sendiri di outlet"),
    DELIVERY("Delivery", "Diantar kurir ke alamat")
}

enum class PaymentMethod(val title: String, val description: String, val category: String) {
    QRIS("QRIS Instant", "Scan QR via BCA, GoPay, Shopee, OVO", "Instant QR"),
    GOPAY("GoPay", "Saldo langsung terpotong", "E-Wallet"),
    OVO("OVO", "Konfirmasi via aplikasi OVO", "E-Wallet"),
    SHOPEEPAY("ShopeePay", "Cashback koin hingga 30%", "E-Wallet"),
    DANA("DANA", "Dompet digital serbaguna", "E-Wallet"),
    VA_BCA("BCA Virtual Account", "Transfer gratis via m-BCA", "Virtual Account"),
    VA_MANDIRI("Mandiri Virtual Account", "Transfer via Livin' by Mandiri", "Virtual Account"),
    CASH("Tunai di Outlet", "Bayar saat mengambil pesanan", "Cash")
}

data class SavedAddress(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val recipientName: String,
    val phoneNumber: String,
    val fullAddress: String,
    val notes: String = "",
    val isDefault: Boolean = false
)

data class Voucher(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val title: String,
    val description: String,
    val minSpend: Long,
    val discountAmount: Long? = null,
    val discountPercent: Int? = null,
    val maxDiscount: Long? = null,
    val expiryText: String = "Berlaku s/d 31 Des 2026"
) {
    fun calculateDiscount(subtotal: Long): Long {
        if (subtotal < minSpend) return 0L
        return if (discountAmount != null) {
            discountAmount
        } else if (discountPercent != null) {
            val calculated = (subtotal * discountPercent) / 100
            if (maxDiscount != null && calculated > maxDiscount) maxDiscount else calculated
        } else 0L
    }
}

data class Outlet(
    val id: String,
    val name: String,
    val address: String,
    val distanceKm: Double,
    val openHours: String = "07:00 - 22:00",
    val isOpen: Boolean = true
)

data class OrderItem(
    val orderId: String,
    val orderNumber: String,
    val items: List<CartItem>,
    val orderType: OrderType,
    val status: OrderStatus,
    val outlet: Outlet,
    val deliveryAddress: SavedAddress?,
    val subtotal: Long,
    val taxAmount: Long,
    val deliveryFee: Long,
    val discountAmount: Long,
    val pointsDiscount: Long,
    val totalPaid: Long,
    val paymentMethod: PaymentMethod,
    val timestamp: Long = System.currentTimeMillis(),
    val pickupPin: String,
    val estimatedMinutes: Int = 15,
    val customerName: String = "Dimas Prasetyo"
)

data class UserProfile(
    val name: String = "Dimas Prasetyo",
    val phone: String = "+62 812-3456-7890",
    val email: String = "dimas.prasetyo@email.com",
    val tier: String = "Jiwa Gold Member",
    val points: Long = 3450L,
    val memberCode: String = "CC-8829-0192",
    val addresses: List<SavedAddress> = listOf(
        SavedAddress(
            label = "Kantor (Default)",
            recipientName = "Dimas Prasetyo",
            phoneNumber = "0812-3456-7890",
            fullAddress = "Menara Sudirman Lt. 14, Jl. Jend. Sudirman Kav 60, Jakarta Selatan",
            notes = "Titip di resepsionis lobby utama",
            isDefault = true
        ),
        SavedAddress(
            label = "Rumah",
            recipientName = "Dimas Prasetyo",
            phoneNumber = "0812-3456-7890",
            fullAddress = "Jl. Kemang Timur No. 42B, Mampang Prapatan, Jakarta Selatan",
            notes = "Pagar hitam depan pos satpam",
            isDefault = false
        )
    ),
    val favoriteProductIds: Set<String> = emptySet()
)

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    CUSTOMER("Pelanggan", 0xFFEE4D2D),
    EMPLOYEE("Karyawan / Staff", 0xFF1976D2),
    OWNER("Owner / Pemilik", 0xFF3E2723)
}

data class AppUser(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val avatarEmoji: String = "👤",
    val title: String = ""
)

data class CustomerInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val tier: String = "Jiwa Friend",
    val totalOrders: Int = 1,
    val totalSpent: Long = 0L,
    val pointsBalance: Long = 0L,
    val joinedDate: String = "12 Jan 2026",
    val lastOrderDate: String = "Hari ini"
)

