package com.example

import com.example.data.model.*
import com.example.data.repository.ChanChanRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: ChanChanRepository

    @Before
    fun setUp() {
        repository = ChanChanRepository()
    }

    @Test
    fun testInitialProductsLoaded() {
        val products = repository.products.value
        assertTrue("Products should be populated", products.isNotEmpty())
        assertTrue("Should contain best sellers", products.any { it.isBestSeller })
    }

    @Test
    fun testProductManagementCrud() {
        val newProduct = ProductItem(
            name = "Rendang Brioche Toast",
            category = FoodCategory.TOAST,
            price = 45000L,
            description = "Brioche toast isi daging rendang padang empuk",
            emoji = "🥪"
        )
        repository.addProduct(newProduct)
        assertTrue(repository.products.value.any { it.name == "Rendang Brioche Toast" })

        // Toggle availability
        repository.toggleProductAvailability(newProduct.id)
        val found = repository.products.value.first { it.id == newProduct.id }
        assertFalse(found.isAvailable)

        // Delete
        repository.deleteProduct(newProduct.id)
        assertFalse(repository.products.value.any { it.id == newProduct.id })
    }

    @Test
    fun testCartCalculationsAndOrderCreation() {
        val product = repository.products.value.first()
        val cartItem = CartItem(
            product = product,
            selectedSize = VariantOption(name = "Large", extraPrice = 4000L),
            quantity = 2
        )
        repository.addToCart(cartItem)

        val cart = repository.cart.value
        assertEquals(1, cart.size)
        assertEquals(cartItem.unitPrice * 2, cart[0].totalPrice)

        val outlet = repository.outlets.value.first()
        val order = repository.createOrder(
            outlet = outlet,
            orderType = OrderType.PICKUP,
            deliveryAddress = null,
            paymentMethod = PaymentMethod.QRIS,
            appliedVoucher = null,
            usePoints = false
        )

        assertNotNull(order)
        assertTrue(order.orderNumber.startsWith("CC-"))
        assertEquals(OrderStatus.PENDING, order.status)
        assertTrue(repository.cart.value.isEmpty())
    }

    @Test
    fun testVoucherDiscountCalculation() {
        val voucher = Voucher(
            id = "v-test",
            code = "TEST30",
            title = "Diskon 30%",
            description = "Diskon 30% maks 15k",
            minSpend = 30000L,
            discountPercent = 30,
            maxDiscount = 15000L
        )

        assertEquals(0L, voucher.calculateDiscount(20000L)) // Under minSpend
        assertEquals(12000L, voucher.calculateDiscount(40000L)) // 30% of 40k = 12k
        assertEquals(15000L, voucher.calculateDiscount(100000L)) // Capped at 15k
    }

    @Test
    fun testReportingSummaryCalculations() {
        val stats = repository.getReportingStats("Hari Ini")
        assertTrue(stats.totalRevenue > 0)
        assertTrue(stats.totalOrders > 0)
        assertTrue(stats.topSellingProducts.isNotEmpty())
        assertEquals(7, stats.dailySalesTrend.size)
    }
}
