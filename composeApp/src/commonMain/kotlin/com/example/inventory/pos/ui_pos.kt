package com.example.inventory.pos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio // <-- Fix
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults // <-- Fix
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PosMainScreen(vm: PosViewModel) {
    val showPayment by vm.showPayment.collectAsState()

    Row(Modifier.fillMaxSize()) {
        // Left Panel: Product Grid (60%)
        Column(Modifier.fillMaxHeight().weight(0.6f).padding(16.dp)) {
            val searchQuery by vm.searchQuery.collectAsState()
            val category by vm.selectedCategory.collectAsState()
            val products by vm.products.collectAsState()

            SearchBar(query = searchQuery, onQueryChange = vm::setSearchQuery)
            Spacer(Modifier.height(8.dp))
            DateDisplay() // Reintroduced DateDisplay directly
            Spacer(Modifier.height(8.dp))
            CategoryTabs(category = category, onSelect = vm::selectCategory)
            Spacer(Modifier.height(8.dp))
            ProductGrid(products = products, onTap = { vm.addProduct(it) })
        }

        // Right Panel: Order and Payment Overlay
        Box(Modifier.fillMaxHeight().weight(0.4f)) {
            val orderPanelAlpha = if (showPayment) 0.3f else 1f
            OrderPanel(vm = vm, modifier = Modifier.alpha(orderPanelAlpha))
            PaymentPanel(vm = vm, isVisible = showPayment)
        }
    }

    val insufficient by vm.insufficientModal.collectAsState()
    if (insufficient != null) {
        InsufficientStockDialog(
            productName = insufficient!!,
            onRestock = { vm.restockAndAdd(insufficient!!) },
            onCancel = { vm.dismissModal() }
        )
    }
}

@Composable
fun OrderPanel(vm: PosViewModel, modifier: Modifier = Modifier) {
    val orderId by vm.orderId.collectAsState()
    val online by vm.online.collectAsState()
    val orderType by vm.orderType.collectAsState()
    val cart by vm.cart.collectAsState()
    val orderNotes by vm.orderNotes.collectAsState()

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0A192F)) // Dark Blue Background
            .padding(16.dp)
    ) {
        OrderHeader(
            orderId = orderId,
            online = online,
            orderType = orderType,
            onOrderTypeChange = vm::setOrderType
        )
        CartList(
            cart = cart,
            onInc = { vm.adjustQty(it, +1) },
            onDec = { vm.adjustQty(it, -1) },
            onRemove = { vm.removeItem(it) },
            modifier = Modifier.weight(1f)
        )
        OrderNotes(
            notes = orderNotes,
            onNotesChange = vm::setOrderNotes
        )
        OrderSummary(
            subtotal = vm.subtotal(),
            total = vm.total(),
            onPay = { vm.goToPayment() },
            payEnabled = cart.isNotEmpty()
        )
    }
}

@Composable
fun PaymentPanel(vm: PosViewModel, isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.zIndex(1f),
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim
            Box(
                modifier = Modifier.matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = vm::goBackToOrder
                    )
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                color = Color.White.copy(alpha = 0.7f), // Semi-transparent white background
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                shadowElevation = 8.dp
            ) {
                PaymentPanelContent(vm = vm)
            }
        }
    }
}

@Composable
private fun PaymentPanelContent(vm: PosViewModel) {
    val payment by vm.payment.collectAsState()
    val total = vm.total()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = vm::goBackToOrder) {
                Text("<", color = Color.Black) // Placeholder for ArrowBack icon, text color for readability
            }
            Spacer(Modifier.width(8.dp))
            Text("Payment", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(Modifier.height(16.dp))

        Text("Total Amount Due", fontSize = 18.sp, color = Color.Black)
        Text("₱${total.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(16.dp))

        PaymentTabs(payment.method) { vm.setPaymentMethod(it) }
        Spacer(Modifier.height(16.dp))

        when (payment.method) {
            PaymentMethod.Cash -> CashPaymentArea(payment = payment, onTendered = vm::setCashTendered)
            PaymentMethod.OnlineQR -> OnlinePaymentArea()
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { vm.finalizeAndSendToKitchen { /* Handle success if needed */ } },
            enabled = payment.isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Finalize & Send to Kitchen")
        }
    }
}

// Reintroduced DateDisplay directly into ui_pos.kt
@Composable
fun DateDisplay() {
    val currentDate = Date()
    val formatter = SimpleDateFormat("EEEE, d MMM yyyy", Locale.ENGLISH)
    val formattedDate = formatter.format(currentDate)
    Text(
        text = formattedDate,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
}

// region Reusable Components

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(value = query, onValueChange = onQueryChange, label = { Text("Search food...") }, modifier = Modifier.fillMaxWidth())
}

@Composable
fun CategoryTabs(category: PosCategory?, onSelect: (PosCategory?) -> Unit) {
    val cats = PosCategory.entries
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 8.dp)) {
        val allSelected = category == null
        AssistChip(
            onClick = { onSelect(null) },
            label = { Text("All") },
            modifier = Modifier.padding(end = 8.dp),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (allSelected) Color(0xFF2D6A4F) else Color(0xFFE9ECEF),
                labelColor = if (allSelected) Color.White else Color.Black
            )
        )
        cats.forEach { c ->
            val selected = c == category
            AssistChip(
                onClick = { onSelect(c) },
                label = { Text(c.name) },
                modifier = Modifier.padding(end = 8.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) Color(0xFF2D6A4F) else Color(0xFFE9ECEF),
                    labelColor = if (selected) Color.White else Color.Black
                )
            )
        }
    }
}

@Composable
fun ProductGrid(products: List<Product>, onTap: (Product) -> Unit) {
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        products.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { p ->
                    ProductCard(p, Modifier.weight(1f)) { scope.launch { onTap(p) } }
                }
                if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProductCard(product: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(Modifier.height(90.dp).fillMaxWidth().background(Color(0xFFDDE3E9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("₱${product.price.toInt()}", fontSize = 14.sp, color = Color(0xFF495057))
        }
    }
}

@Composable
fun OrderHeader(orderId: String, online: Boolean, orderType: OrderType, onOrderTypeChange: (OrderType) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Order $orderId", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            val statusColor = if (online) Color(0xFF81C784) else Color(0xFFE57373)
            Text(text = if (online) "Online" else "Offline (queued)", color = statusColor, fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = orderType == OrderType.DineIn, onClick = { onOrderTypeChange(OrderType.DineIn) })
            Text("Dine In", color = Color.White)
            Spacer(Modifier.width(8.dp))
            RadioButton(selected = orderType == OrderType.TakeOut, onClick = { onOrderTypeChange(OrderType.TakeOut) })
            Text("Take Out", color = Color.White)
        }
    }
    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
}

@Composable
fun CartList(cart: List<CartItem>, onInc: (String) -> Unit, onDec: (String) -> Unit, onRemove: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        if (cart.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cart is empty", color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                cart.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.product.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            Text("₱${item.product.price.toInt()} x ${item.qty}", color = Color(0xFFCCCCCC))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onDec(item.product.id) }) { Text("-", color = Color.White, fontSize = 22.sp) }
                            Text("${item.qty}", Modifier.padding(horizontal = 8.dp), color = Color.White, fontSize = 16.sp)
                            IconButton(onClick = { onInc(item.product.id) }) { Text("+", color = Color.White, fontSize = 22.sp) }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onRemove(item.product.id) }) { Text("X", color = Color.White) } // Placeholder for Delete Icon
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun OrderNotes(notes: String, onNotesChange: (String) -> Unit) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Order Notes...", color = Color.White) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    )
}

@Composable
fun OrderSummary(subtotal: Double, total: Double, onPay: () -> Unit, payEnabled: Boolean) {
    Column {
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", color = Color.White)
            Text("₱${subtotal.toInt()}", color = Color.White)
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", fontWeight = FontWeight.Bold, color = Color.White)
            Text("₱${total.toInt()}", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onPay, enabled = payEnabled, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Continue to Payment")
        }
    }
}

@Composable
fun InsufficientStockDialog(productName: String, onRestock: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Insufficient Stock for '$productName'.") },
        text = { Text("You can fast-track restock or cancel.") },
        confirmButton = { TextButton(onClick = onRestock) { Text("Restock Ingredient") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
fun PaymentTabs(method: PaymentMethod, onSelect: (PaymentMethod) -> Unit) {
    TabRow(selectedTabIndex = if (method == PaymentMethod.Cash) 0 else 1) {
        Tab(selected = method == PaymentMethod.Cash, onClick = { onSelect(PaymentMethod.Cash) }, text = { Text("Cash") })
        Tab(selected = method == PaymentMethod.OnlineQR, onClick = { onSelect(PaymentMethod.OnlineQR) }, text = { Text("Online/QR") })
    }
}

@Composable
fun CashPaymentArea(payment: PaymentState, onTendered: (Double) -> Unit) {
    Column {
        Text("Enter Amount Tendered", color = Color.Black) // Text color for readability
        Spacer(Modifier.height(8.dp))
        val quick = listOf(50, 100, 200, 500, 1000)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Consistent spacing
        ) {
            quick.forEach { amt ->
                OutlinedButton(
                    onClick = { onTendered(amt.toDouble()) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f) // Make it square
                        .padding(4.dp), // Uniform padding
                    shape = CircleShape, // Make it circular
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black) // Ensure text is black
                ) {
                    Text("₱$amt", color = Color.Black) // Explicit text color
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        var manual by remember { mutableStateOf("") }
        OutlinedTextField(
            value = manual,
            onValueChange = {
                manual = it
                it.toDoubleOrNull()?.let(onTendered)
            },
            label = { Text("Keypad", color = Color.Black) }, // Text color for readability
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black) // Ensure text is black
        )
        Spacer(Modifier.height(8.dp))
        Text("Change Due: ₱${payment.cash.change.toInt()}", color = Color.Black) // Text color for readability
    }
}

@Composable
fun OnlinePaymentArea() {
    Column {
        Text("Scan customer QR or tap to confirm online payment.", color = Color.Black) // Text color for readability
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* Assuming success externally */ }, enabled = false) {
            Text("Awaiting QR Confirmation")
        }
    }
}

// endregion