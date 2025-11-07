package com.example.inventory.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryApp
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.data.model.repository.InventoryRepositoryImpl
import com.example.inventory.data.model.repository.WarehouseRepository
import com.example.inventory.model.menu_config.App
import com.example.inventory.pos.PosViewModel
import com.example.inventory.ui.auth.DemoLoginViewModel
import com.example.inventory.ui.auth.LoginScreen
import com.example.inventory.ui.main.inkitchen.InKitchenViewModel
import com.example.inventory.ui.main.inkitchen.InKitchenViewModelFactory
import com.example.inventory.ui.main.warehouse.WarehouseScreen
import com.example.inventory.ui.main.warehouse.WarehouseViewModel
import com.example.inventory.ui.main.warehouse.WarehouseViewModelFactory
import com.example.inventory.ui.theme.Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // existing wiring
        val supabaseClient = (application as InventoryApp).supabaseClient
        val supabaseService = SupabaseService(supabaseClient)

        // repositories
        val warehouseRepository = WarehouseRepository(supabaseService)
        val inventoryRepository = InventoryRepositoryImpl(supabaseService)

        // ViewModel factories for screens that need them
        val warehouseFactory = WarehouseViewModelFactory(warehouseRepository, supabaseService)
        val inKitchenFactory = InKitchenViewModelFactory(warehouseRepository)

        setContent {
            Theme {
                var authToken by remember { mutableStateOf<String?>(null) }
                val loginViewModel: DemoLoginViewModel = viewModel()

                if (authToken == null) {
                    // Show login
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = { token -> authToken = token }
                    )
                } else {
                    // Show main app
                    val posViewModel = remember { PosViewModel(inventoryRepository) }

                    // obtain the two viewmodels used by WarehouseScreen
                    val warehouseViewModel: WarehouseViewModel = viewModel(factory = warehouseFactory)
                    val inKitchenViewModel: InKitchenViewModel = viewModel(factory = inKitchenFactory)

                    App(
                        posViewModel = posViewModel,
                        repo = inventoryRepository,
                        // <-- pass the real inventoryScreen here
                        inventoryScreen = {
                            WarehouseScreen(
                                warehouseViewModel = warehouseViewModel,
                                inKitchenViewModel = inKitchenViewModel
                            )
                        },
                        onLogout = {
                            authToken = null
                            loginViewModel.logout()
                        }
                    )
                }
            }
        }
    }
}
