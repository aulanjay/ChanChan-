package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.ChanChanRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCustomizationSheet(
    product: ProductItem,
    onDismiss: () -> Unit,
    onAddToCart: (CartItem) -> Unit
) {
    var selectedSize by remember { mutableStateOf(product.sizeOptions.first()) }
    var selectedSugar by remember { mutableStateOf(product.sugarLevels.first()) }
    var selectedIce by remember { mutableStateOf(product.iceLevels.first()) }
    var selectedToppings by remember { mutableStateOf<Set<VariantOption>>(emptySet()) }
    var notes by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }

    val toppingsExtra = selectedToppings.sumOf { it.extraPrice }
    val itemUnitPrice = product.price + selectedSize.extraPrice + toppingsExtra
    val totalPrice = itemUnitPrice * quantity

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ChanWarmSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Product Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChanSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = product.emoji, fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = ChanChanRepository.formatRupiah(product.price),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = ChanOrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = product.description,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Size Options
            Text(
                text = "Pilih Ukuran (Size)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                product.sizeOptions.forEach { size ->
                    val isSelected = selectedSize.name == size.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSize = size },
                        label = {
                            Text(
                                text = if (size.extraPrice > 0) "${size.name} (+${ChanChanRepository.formatRupiah(size.extraPrice)})" else size.name,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChanOrangePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = ChanSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Sugar Level
            if (product.category == FoodCategory.KOPI || product.category == FoodCategory.NON_COFFEE) {
                Text(
                    text = "Tingkat Gula (Sugar Level)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    product.sugarLevels.forEach { sugar ->
                        val isSelected = selectedSugar == sugar
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSugar = sugar },
                            label = { Text(sugar.split(" ").first(), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChanOrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = ChanSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Ice Level
                Text(
                    text = "Tingkat Es (Ice Level)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    product.iceLevels.forEach { ice ->
                        val isSelected = selectedIce == ice
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIce = ice },
                            label = { Text(ice, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChanOrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = ChanSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4. Toppings
            Text(
                text = "Tambah Topping Spesial",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                product.availableToppings.forEach { topping ->
                    val isChecked = selectedToppings.contains(topping)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedToppings = if (isChecked) {
                                    selectedToppings - topping
                                } else {
                                    selectedToppings + topping
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedToppings = if (checked) selectedToppings + topping else selectedToppings - topping
                                },
                                colors = CheckboxDefaults.colors(checkedColor = ChanOrangePrimary)
                            )
                            Text(
                                text = topping.name,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "+${ChanChanRepository.formatRupiah(topping.extraPrice)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChanOrangePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Kitchen Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan untuk Barista / Dapur", fontSize = 12.sp) },
                placeholder = { Text("Contoh: saus dipisah, jangan pakai sedotan", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // 6. Quantity Stepper & Add to Cart Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = ChanSurfaceVariant),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = TextPrimary)
                    }

                    Text(
                        text = "$quantity",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    FilledIconButton(
                        onClick = { quantity++ },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = ChanOrangeContainer),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = ChanOrangePrimary)
                    }
                }

                Button(
                    onClick = {
                        val cartItem = CartItem(
                            product = product,
                            selectedSize = selectedSize,
                            selectedSugar = selectedSugar,
                            selectedIce = selectedIce,
                            selectedToppings = selectedToppings.toList(),
                            notes = notes,
                            quantity = quantity
                        )
                        onAddToCart(cartItem)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("add_to_cart_confirm_btn")
                ) {
                    Text(
                        text = "Tambah • ${ChanChanRepository.formatRupiah(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    state: UiState,
    onDismiss: () -> Unit,
    onOpenVouchers: () -> Unit,
    onTogglePoints: () -> Unit,
    onSelectPaymentMethod: (PaymentMethod) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onConfirmPayment: () -> Unit
) {
    val cartList = state.cart
    val subtotal = cartList.sumOf { it.totalPrice }
    val voucherDiscount = state.selectedVoucher?.calculateDiscount(subtotal) ?: 0L
    val deliveryFee = if (state.orderType == OrderType.DELIVERY) 12000L else 0L
    val tax = ((subtotal - voucherDiscount).coerceAtLeast(0L) * 0.10).toLong()

    val availablePoints = state.userProfile.points
    val pointsDiscount = if (state.usePoints) {
        val maxAllowed = (subtotal + deliveryFee + tax - voucherDiscount).coerceAtLeast(0L)
        minOf(availablePoints, maxAllowed)
    } else 0L

    val grandTotal = (subtotal + deliveryFee + tax - voucherDiscount - pointsDiscount).coerceAtLeast(0L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ChanWarmSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Konfirmasi Pesanan & Pembayaran",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Order Location Details
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ChanSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.orderType == OrderType.PICKUP) Icons.Default.Storefront else Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = ChanOrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.orderType == OrderType.PICKUP) "Pick Up di ${state.selectedOutlet.name}" else "Delivery ke ${state.userProfile.addresses.firstOrNull()?.label ?: "Alamat"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.orderType == OrderType.PICKUP) state.selectedOutlet.address else (state.userProfile.addresses.firstOrNull()?.fullAddress ?: ""),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cart Items List
            Text(
                text = "Item Pesanan (${cartList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            cartList.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.product.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${item.selectedSize.name} • ${item.selectedSugar}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Stepper
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onUpdateQuantity(item.cartId, item.quantity - 1) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${item.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { onUpdateQuantity(item.cartId, item.quantity + 1) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ChanChanRepository.formatRupiah(item.totalPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Voucher Coupon Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVouchers() },
                shape = RoundedCornerShape(10.dp),
                color = ChanOrangeContainer
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = ChanOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = state.selectedVoucher?.title ?: "Gunakan Voucher / Kupon Promo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ChanOrangeDark
                            )
                            if (voucherDiscount > 0) {
                                Text(
                                    text = "Hemat -${ChanChanRepository.formatRupiah(voucherDiscount)}",
                                    fontSize = 11.sp,
                                    color = ChanGreenSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ChanOrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Use ChanPoints Toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ChanSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = ChanGoldPoints,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pakai ChanPoints (${state.userProfile.points} Pts)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "1 Poin = Rp 1 potongan harga",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = state.usePoints,
                        onCheckedChange = { onTogglePoints() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ChanGoldPoints)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Payment Methods Selection (QRIS, E-Wallet, VA, Cash)
            Text(
                text = "Pilih Metode Pembayaran",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            PaymentMethod.values().forEach { method ->
                val isSelected = state.selectedPaymentMethod == method
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectPaymentMethod(method) },
                    color = if (isSelected) ChanOrangeContainer else ChanSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectPaymentMethod(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = ChanOrangePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = method.description,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = method.category,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // QRIS Instant QR Preview when QRIS is selected
            if (state.selectedPaymentMethod == PaymentMethod.QRIS) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "QRIS - Standar Pembayaran Nasional",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ChanCoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QRIS QR Code",
                            tint = Color.Black,
                            modifier = Modifier.size(90.dp)
                        )
                        Text(
                            text = "NMID: ID1020088921020 • ChanChan+ F&B",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Cost Summary Breakdown
            CostSummaryRow("Subtotal Produk", ChanChanRepository.formatRupiah(subtotal))
            if (voucherDiscount > 0) {
                CostSummaryRow("Diskon Voucher", "-${ChanChanRepository.formatRupiah(voucherDiscount)}", color = ChanGreenSuccess)
            }
            if (pointsDiscount > 0) {
                CostSummaryRow("Potongan ChanPoints", "-${ChanChanRepository.formatRupiah(pointsDiscount)}", color = ChanGoldPoints)
            }
            if (deliveryFee > 0) {
                CostSummaryRow("Biaya Pengantaran", ChanChanRepository.formatRupiah(deliveryFee))
            }
            CostSummaryRow("Pajak PB1 (10%)", ChanChanRepository.formatRupiah(tax))

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = ChanBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Pembayaran",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = ChanChanRepository.formatRupiah(grandTotal),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = ChanOrangePrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pay Button
            Button(
                onClick = onConfirmPayment,
                colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("checkout_confirm_pay_btn")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bayar Sekarang (${ChanChanRepository.formatRupiah(grandTotal)})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CostSummaryRow(
    label: String,
    amountText: String,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = amountText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun PaymentSuccessDialog(
    order: OrderItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChanWarmSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ChanGreenSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pembayaran Berhasil! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Pesananmu langsung diteruskan ke barista",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ChanOrangeContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO. PESANAN: ${order.orderNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChanOrangeDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PIN AMBIL DI OUTLET",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "#${order.pickupPin}",
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = ChanOrangePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Outlet: ${order.outlet.name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Total: ${ChanChanRepository.formatRupiah(order.totalPaid)} (${order.paymentMethod.title})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_success_order_btn")
                ) {
                    Text("Lacak Pesanan Saya", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    productToEdit: ProductItem?,
    onDismiss: () -> Unit,
    onSave: (ProductItem) -> Unit
) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var priceText by remember { mutableStateOf(productToEdit?.price?.toString() ?: "25000") }
    var costText by remember { mutableStateOf(productToEdit?.costPrice?.toString() ?: "14000") }
    var stockText by remember { mutableStateOf(productToEdit?.stockQuantity?.toString() ?: "50") }
    var prepTimeText by remember { mutableStateOf(productToEdit?.prepTimeMinutes?.toString() ?: "10") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(productToEdit?.category ?: FoodCategory.TOAST) }
    var emoji by remember { mutableStateOf(productToEdit?.emoji ?: "🥪") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChanWarmSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (productToEdit != null) "Edit Produk Menu" else "Tambah Menu Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji Picker shortcuts
                Text(text = "Pilih Ikon Makanan:", fontSize = 11.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🥪", "☕", "🧋", "🍛", "🥐", "🍟", "🏷️").forEach { e ->
                        Surface(
                            shape = CircleShape,
                            color = if (emoji == e) ChanOrangeContainer else ChanSurfaceVariant,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { emoji = e }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = e, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Crispy Beef Toast") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category selector
                Text(text = "Kategori Menu:", fontSize = 11.sp, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(FoodCategory.TOAST, FoodCategory.KOPI, FoodCategory.MEALS, FoodCategory.NON_COFFEE).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName.split(" ").first(), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChanCoffeeBrown,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Harga Jual (Rp)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Modal HPP (Rp)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stok Porsi", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prepTimeText,
                        onValueChange = { prepTimeText = it },
                        label = { Text("Masak (Mnt)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Makanan / Minuman", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                val product = (productToEdit ?: ProductItem(
                                    name = name,
                                    category = selectedCategory,
                                    price = priceText.toLongOrNull() ?: 25000L,
                                    costPrice = costText.toLongOrNull() ?: 14000L,
                                    description = description,
                                    emoji = emoji,
                                    stockQuantity = stockText.toIntOrNull() ?: 50,
                                    prepTimeMinutes = prepTimeText.toIntOrNull() ?: 10
                                )).copy(
                                    name = name,
                                    category = selectedCategory,
                                    price = priceText.toLongOrNull() ?: 25000L,
                                    costPrice = costText.toLongOrNull() ?: 14000L,
                                    description = description,
                                    emoji = emoji,
                                    stockQuantity = stockText.toIntOrNull() ?: 50,
                                    prepTimeMinutes = prepTimeText.toIntOrNull() ?: 10
                                )
                                onSave(product)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Produk", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VoucherSelectionDialog(
    vouchers: List<Voucher>,
    subtotal: Long,
    onDismiss: () -> Unit,
    onSelectVoucher: (Voucher) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChanWarmSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pilih Kupon Diskon Sejiwa 🎟️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Gunakan voucher untuk hemat lebih banyak",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                vouchers.forEach { voucher ->
                    val isEligible = subtotal >= voucher.minSpend
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isEligible) ChanOrangeContainer else ChanSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = voucher.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isEligible) ChanOrangeDark else TextSecondary
                                )
                                Text(
                                    text = voucher.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Button(
                                onClick = { onSelectVoucher(voucher) },
                                enabled = isEligible,
                                colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(text = "Pakai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (SavedAddress) -> Unit
) {
    var label by remember { mutableStateOf("Rumah Baru") }
    var name by remember { mutableStateOf("Dimas Prasetyo") }
    var phone by remember { mutableStateOf("0812-3456-7890") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChanWarmSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tambah Alamat Pengantaran",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label Alamat (cth: Apartemen, Kantor)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Lengkap", fontSize = 12.sp) },
                    placeholder = { Text("Jl. Sudirman No. 12, Lantai 5") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Petunjuk untuk Kurir", fontSize = 12.sp) },
                    placeholder = { Text("Pagar cokelat, depan minimarket") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (address.isNotEmpty()) {
                                onSave(
                                    SavedAddress(
                                        label = label,
                                        recipientName = name,
                                        phoneNumber = phone,
                                        fullAddress = address,
                                        notes = notes,
                                        isDefault = false
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChanOrangePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Alamat", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OutletPickerDialog(
    outlets: List<Outlet>,
    selectedOutlet: Outlet,
    onSelectOutlet: (Outlet) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChanWarmSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pilih Outlet ChanChan+ Terdekat 📍",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Pesanan akan disiapkan oleh outlet ini",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                outlets.forEach { outlet ->
                    val isSelected = outlet.id == selectedOutlet.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectOutlet(outlet) },
                        color = if (isSelected) ChanOrangeContainer else ChanSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = if (isSelected) ChanOrangePrimary else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = outlet.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = outlet.address,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Buka: ${outlet.openHours} • Jarak: ${outlet.distanceKm} km",
                                    fontSize = 10.sp,
                                    color = ChanGreenSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ChanOrangePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup", color = TextSecondary)
                }
            }
        }
    }
}
