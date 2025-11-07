package com.example.inventory.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryApp
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.data.model.repository.WarehouseRepository
import com.example.inventory.data.model.repository.InventoryRepositoryImpl // ✅ Correct import
import com.example.inventory.model.menu_config.App
import com.example.inventory.pos.PosViewModel
import com.example.inventory.ui.main.inkitchen.InKitchenViewModel
import com.example.inventory.ui.main.inkitchen.InKitchenViewModelFactory
import com.example.inventory.ui.main.warehouse.WarehouseScreen
import com.example.inventory.ui.main.warehouse.WarehouseViewModel
import com.example.inventory.ui.main.warehouse.WarehouseViewModelFactory
import com.example.inventory.ui.theme.Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabaseClient = (application as InventoryApp).supabaseClient
        val supabaseService = SupabaseService(supabaseClient)
        val warehouseRepository = WarehouseRepository(supabaseService)
        val inventoryRepository = InventoryRepositoryImpl(supabaseService) // ✅ Works now

        val warehouseFactory = WarehouseViewModelFactory(warehouseRepository, supabaseService)
        val inKitchenFactory = InKitchenViewModelFactory(warehouseRepository)

        setContent {
            Theme {
                val warehouseViewModel: WarehouseViewModel = viewModel(factory = warehouseFactory)
                val inKitchenViewModel: InKitchenViewModel = viewModel(factory = inKitchenFactory)
                val posViewModel = remember { PosViewModel(inventoryRepository) }

                App(
                    posViewModel = posViewModel,
                    repo = inventoryRepository,
                    inventoryScreen = {
                        WarehouseScreen(
                            warehouseViewModel = warehouseViewModel,
                            inKitchenViewModel = inKitchenViewModel
                        )
                    }
                )
            }
        }
    }
}
