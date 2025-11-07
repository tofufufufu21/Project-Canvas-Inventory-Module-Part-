package com.example.inventory.model.menu_config

import android.util.Log
import com.example.inventory.data.model.repository.InventoryRepositoryImpl
import com.example.inventory.model.RecipeIngredient
import com.example.inventory.model.WarehouseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.inventory.model.menu_config.VariantData
import com.example.inventory.model.menu_config.IngredientData
import com.example.inventory.model.menu_config.Category
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
// Simple data holders used by the UI
/**
 * AddProductViewModel interface (UI-facing state + actions)
 * Note: selectedIngredients is List<RecipeIngredient> because we will send those directly to Supabase.
 */
interface AddProductViewModel {
    // Step navigation
    var currentStep: Int
    fun nextStep()
    fun previousStep()

    // Category
    var useExistingCategory: Boolean
    var newCategoryName: String
    var selectedExistingCategory: Category?

    // Product info
    var productName: String
    var productPrice: String
    var productVisibility: Boolean
    var productImageUri: String?

    // Variants
    var hasVariants: Boolean
    var newVariantName: String
    var newVariantExtraPrice: String
    var variants: SnapshotStateList<VariantData>
    fun addVariant()
    fun removeVariant(variant: VariantData)

    // Ingredients (we store RecipeIngredient directly so it's ready for repo.createProductRecipe)
    var trackStock: Boolean
    var selectedIngredients: List<RecipeIngredient>

    // Warehouse items for selection
    var warehouseItems: List<WarehouseItem>
    suspend fun loadWarehouseIngredients()
    fun addIngredient(item: WarehouseItem, amount: Double)

    // Availability
    var isDineInAvailable: Boolean
    var isTakeOutAvailable: Boolean

    var existingCategories: List<Category>
    suspend fun loadExistingCategories()

    // Save product
    suspend fun saveProduct(): Result<Long>
}

/**
 * Implementation that talks to InventoryRepositoryImpl
 */
class AddProductViewModelImpl(
    private val repo: InventoryRepositoryImpl
) : AddProductViewModel {

    // Step navigation
    override var currentStep by mutableStateOf(1)
    override fun nextStep() {
        if (currentStep < 7) currentStep++
    }
    override fun previousStep() {
        if (currentStep > 1) currentStep--
    }

    // Category
    override var useExistingCategory by mutableStateOf(false)
    override var newCategoryName by mutableStateOf("")
    override var selectedExistingCategory: Category? = null

    // Product info
    override var productName by mutableStateOf("")
    override var productPrice by mutableStateOf("") // string for text field; parsed when saving
    override var productVisibility by mutableStateOf(true)
    override var productImageUri: String? = null

    // Variants
    override var hasVariants by mutableStateOf(false)
    override var newVariantName by mutableStateOf("")
    override var newVariantExtraPrice by mutableStateOf("")
    override var variants = mutableStateListOf<VariantData>()

    override fun addVariant() {
        val extra = newVariantExtraPrice.toDoubleOrNull() ?: 0.0
        val v = VariantData(name = newVariantName.trim(), extraPrice = extra)
        variants.add(v) // 👈 modifies the state list directly
        newVariantName = ""
        newVariantExtraPrice = ""
    }

    override fun removeVariant(variant: VariantData) {
        variants.remove(variant) // 👈 works with state list
    }

    // Ingredients & warehouse list
    override var trackStock by mutableStateOf(false)
    override var selectedIngredients: List<RecipeIngredient> = emptyList()

    override var warehouseItems: List<WarehouseItem> = emptyList()
    override suspend fun loadWarehouseIngredients() {
        warehouseItems = withContext(Dispatchers.IO) {
            try {
                repo.getWarehouseIngredients()
            } catch (e: Exception) {
                Log.e("AddProductVM", "loadWarehouseIngredients failed: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * Adds a RecipeIngredient to selectedIngredients.
     * Converts nullable WarehouseItem.id safely into Long using `?: 0L`
     */
    override fun addIngredient(item: WarehouseItem, amount: Double) {
        // ensure we use the same shape your RecipeIngredient expects
        val ri = RecipeIngredient(
            id = null,
            ingredient_id = item.id ?: 0L,          // safe conversion from Long?
            variant_id = null,                      // repo.createProductRecipe will set variant_id later
            measurement_value = amount,
            measurement_unit = item.unit ?: "g"
        )
        selectedIngredients = selectedIngredients + ri
    }

    // ✅ Fetch all existing menu categories from Supabase


    override var existingCategories by mutableStateOf<List<Category>>(emptyList())

    // 🧠 Function to load categories
    override suspend fun loadExistingCategories() {
        existingCategories = withContext(Dispatchers.IO) {
            try {
                val categories = repo.getMenuCategories()
                Log.d("AddProductVM", "Fetched categories: $categories")
                categories
            } catch (e: Exception) {
                Log.e("AddProductVM", "Failed to load categories: ${e.message}")
                emptyList()
            }
        }
    }

    // Availability
    override var isDineInAvailable by mutableStateOf(false)
    override var isTakeOutAvailable by mutableStateOf(false)


    // Save product: creates category -> product -> variant(s) -> product_recipes (ingredients)
    override suspend fun saveProduct(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // 1) Category
            val categoryId = if (useExistingCategory && selectedExistingCategory != null) {
                selectedExistingCategory!!.id
            } else {
                repo.createCategory(newCategoryName.ifBlank { "Uncategorized" })
            }

            // 2) Product
            val productId = repo.createProduct(
                categoryId = categoryId,
                productName = productName,
                imageUrl = productImageUri,
                description = null
            )

            // 3) Variants: create each variant, collect their ids
            val variantIds: List<Long> = if (hasVariants && variants.isNotEmpty()) {
                variants.map { repo.createVariant(productId, it.name, it.extraPrice) }
            } else {
                // Single default variant using base price
                val basePrice = productPrice.toDoubleOrNull() ?: 0.0
                listOf(repo.createVariant(productId, "Default", basePrice))
            }

            // 4) If there are selectedIngredients (RecipeIngredient objects), attach them to each variant.
            //    InventoryRepositoryImpl.createProductRecipe expects a list of RecipeIngredient; it
            //    will set variant_id via payload map (or we set variant_id here manually).
            if (selectedIngredients.isNotEmpty()) {
                // Make a copy of ingredients per variant with variant_id set
                variantIds.forEach { vid ->
                    val payload = selectedIngredients.map { ri ->
                        ri.copy(variant_id = vid)
                    }
                    repo.createProductRecipe(vid, payload)
                }
            }

            Log.d("AddProductVM", "Saved product (id=$productId) '$productName' successfully.")
            Result.success(productId)
        } catch (t: Throwable) {
            Log.e("AddProductVM", "Failed saving product: ${t.message}", t)
            Result.failure(t)
        }
    }
}
