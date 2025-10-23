package com.example.inventory.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.App
import com.example.inventory.InventoryApp
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.data.model.repository.WarehouseRepository
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

        // Get the Supabase client from the singleton Application class
        val supabaseClient = (application as InventoryApp).supabaseClient

        // Initialize Supabase service and repository
        val supabaseService = SupabaseService(supabaseClient)
        val repository = WarehouseRepository(supabaseService)

        // Create ViewModel factories for Android-specific ViewModels
        val warehouseFactory = WarehouseViewModelFactory(repository, supabaseService)
        val inKitchenFactory = InKitchenViewModelFactory(repository)

        setContent {
            Theme {
                // Create Android-specific ViewModels using their factories
                val warehouseViewModel: WarehouseViewModel = viewModel(factory = warehouseFactory)
                val inKitchenViewModel: InKitchenViewModel = viewModel(factory = inKitchenFactory)

                // Create the common PosViewModel as a plain, remembered class
                val posViewModel = remember { PosViewModel(repository) }

                App(
                    posViewModel = posViewModel,
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
