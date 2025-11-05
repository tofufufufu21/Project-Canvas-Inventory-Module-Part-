package com.example.inventory.pos

import com.example.inventory.model.ProductDTO
import com.example.inventory.data.model.repository.InventoryRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class PosViewModel(private val repo: InventoryRepositoryImpl) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _showPayment = MutableStateFlow(false)
    val showPayment: StateFlow<Boolean> = _showPayment.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDTO>>(emptyList())
    val products: StateFlow<List<ProductDTO>> = _products.asStateFlow()

    private val _insufficientModal = MutableStateFlow<ProductDTO?>(null)
    val insufficientModal: StateFlow<ProductDTO?> = _insufficientModal.asStateFlow()

    private val _orderId = MutableStateFlow(0L)
    val orderId: StateFlow<Long> = _orderId.asStateFlow()

    private val _online = MutableStateFlow(false)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.DineIn)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    private val _payment = MutableStateFlow(PaymentState())
    val payment: StateFlow<PaymentState> = _payment.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        scope.launch {
            _products.value = try {
                repo.getProductsForPOS()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun selectCategory(category: String) { _selectedCategory.value = category }

    fun addProduct(product: ProductDTO) {
        scope.launch {
            val existing = _cart.value.find { it.product.variantId == product.variantId }
            if (existing != null) {
                _cart.value = _cart.value.map {
                    if (it.product.variantId == product.variantId)
                        it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                val hasStock = try {
                    repo.variantHasSufficientStock(product.variantId)
                } catch (e: Exception) {
                    false
                }
                if (hasStock) {
                    _cart.value = _cart.value + CartItem(product, 0)
                } else {
                    _insufficientModal.value = product
                }
            }
        }
    }

    fun restockAndAdd(product: ProductDTO) {
        scope.launch {
            try {
                repo.fastTrackRestock(warehouseItemId = 1L, transferQuantity = 1000.0, unit = "G")
            } catch (_: Exception) { }
            _insufficientModal.value = null
            addProduct(product)
        }
    }

    fun dismissModal() { _insufficientModal.value = null }

    fun setOrderType(type: OrderType) { _orderType.value = type }

    fun adjustQty(item: CartItem, change: Int) {
        _cart.value = _cart.value.mapNotNull {
            if (it.product.variantId == item.product.variantId) {
                val newQty = it.quantity + change
                if (newQty > 0) it.copy(quantity = newQty) else null
            } else it
        }
    }

    fun removeItem(item: CartItem) {
        _cart.value = _cart.value.filterNot { it.product.variantId == item.product.variantId }
    }

    fun setOrderNotes(notes: String) { _orderNotes.value = notes }

    fun subtotal(): Double = _cart.value.sumOf { it.product.price * it.quantity }

    fun total(): Double = subtotal()

    fun goToPayment() { _showPayment.value = true }

    fun goBackToOrder() {
        _showPayment.value = false
        _payment.value = PaymentState()
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _payment.value = _payment.value.copy(method = method)
        updatePaymentValidity()
    }

    fun setCashTendered(amount: Double) {
        val change = amount - total()
        _payment.value = _payment.value.copy(tenderedAmount = amount, change = change)
        updatePaymentValidity()
    }

    private fun updatePaymentValidity() {
        val payment = _payment.value
        val isValid = when (payment.method) {
            PaymentMethod.Cash -> payment.tenderedAmount >= total()
            PaymentMethod.OnlineQR -> true
        }
        _payment.value = payment.copy(isValid = isValid)
    }

    fun finalizeAndSendToKitchen(onSuccess: () -> Unit) {
        scope.launch {
            val orderId = System.currentTimeMillis()
            _orderId.value = orderId

            _cart.value.forEach { item ->
                repo.reserveIngredientsForOrder(orderId, item.product.variantId, item.quantity)
            }

            repo.finalizeOrderDeduction(orderId)

            _cart.value = emptyList()
            _orderNotes.value = ""
            _showPayment.value = false
            _payment.value = PaymentState()
            _selectedCategory.value = "All"
            _searchQuery.value = ""
            _insufficientModal.value = null

            onSuccess()
        }
    }
}
