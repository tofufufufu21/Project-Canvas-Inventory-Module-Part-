package com.example.inventory.pos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PosViewModel {

    // ---------- Product Data ----------
    private val _allProducts = MutableStateFlow(
        listOf(
            Product("1", "Coffee", 5.0, Category.Drinks, 10),
            Product("2", "Tea", 4.0, Category.Drinks, 10),
            Product("3", "Coke", 3.0, Category.Drinks, 10),
            Product("4", "Burger", 10.0, Category.Food, 10),
            Product("5", "Fries", 5.0, Category.Food, 10),
            Product("6", "Pizza", 12.0, Category.Food, 10),
            Product("7", "Chips", 2.0, Category.Snacks, 10),
            Product("8", "Chocolate", 3.0, Category.Snacks, 10),
            Product("9", "Candy", 1.0, Category.Snacks, 10)
        )
    )

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // ---------- Cart & Order Data ----------
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _orderId = MutableStateFlow("#0001")
    val orderId: StateFlow<String> = _orderId.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.DineIn)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    private val _insufficientModal = MutableStateFlow<String?>(null)
    val insufficientModal: StateFlow<String?> = _insufficientModal.asStateFlow()

    private val _totalPulse = MutableStateFlow(false)
    val totalPulse: StateFlow<Boolean> = _totalPulse.asStateFlow()

    private val _online = MutableStateFlow(true)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    // ---------- Payment Data ----------
    private val _payment = MutableStateFlow(PaymentState())
    val payment: StateFlow<PaymentState> = _payment.asStateFlow()

    private val _showPayment = MutableStateFlow(false)
    val showPayment: StateFlow<Boolean> = _showPayment.asStateFlow()

    // ---------- Initialization ----------
    init {
        filterProducts()
    }

    // ---------- Search & Category ----------
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterProducts()
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        filterProducts()
    }

    private fun filterProducts() {
        val query = _searchQuery.value
        val category = _selectedCategory.value
        _products.value = _allProducts.value.filter { product ->
            val matchesCategory = category == null || product.category == category
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    // ---------- Cart Management ----------
    fun addProduct(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            val updatedItem = existingItem.copy(qty = existingItem.qty + 1)
            currentCart[currentCart.indexOf(existingItem)] = updatedItem
        } else {
            currentCart.add(CartItem(product, 1))
        }

        _cart.value = currentCart
    }

    fun adjustQty(productId: String, delta: Int) {
        val currentCart = _cart.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == productId }

        if (existingItem != null) {
            val newQty = existingItem.qty + delta
            if (newQty > 0) {
                currentCart[currentCart.indexOf(existingItem)] = existingItem.copy(qty = newQty)
            } else {
                currentCart.remove(existingItem)
            }
        }

        _cart.value = currentCart
    }

    fun removeItem(productId: String) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    // ---------- Order ----------
    fun setOrderType(orderType: OrderType) {
        _orderType.value = orderType
    }

    fun setOrderNotes(notes: String) {
        _orderNotes.value = notes
    }

    fun subtotal(): Double = _cart.value.sumOf { it.product.price * it.qty }
    fun total(): Double = subtotal() // For now, total is the same as subtotal

    // ---------- Payment ----------
    fun goToPayment() {
        _showPayment.value = true
    }

    fun goBackToOrder() {
        _showPayment.value = false
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _payment.value = _payment.value.copy(method = method)
    }

    fun setCashTendered(amount: Double) {
        val total = total()
        val change = if (amount >= total) amount - total else 0.0
        _payment.value = _payment.value.copy(
            cash = _payment.value.cash.copy(tendered = amount, change = change),
            isValid = amount >= total
        )
    }

    fun finalizeAndSendToKitchen(onSuccess: () -> Unit) {
        // TODO: Send order to kitchen
        _cart.value = emptyList()
        _orderNotes.value = ""
        _payment.value = PaymentState()
        goBackToOrder()
        onSuccess()
    }

    // ---------- Other ----------
    fun restockAndAdd(productName: String) {
        // TODO: Implement restock logic
    }

    fun dismissModal() {
        _insufficientModal.value = null
    }
}