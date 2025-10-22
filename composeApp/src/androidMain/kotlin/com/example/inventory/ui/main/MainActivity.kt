package com.example.inventory.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.App
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.data.model.repository.WarehouseRepository
import com.example.inventory.ui.main.inkitchen.InKitchenViewModel
import com.example.inventory.ui.main.inkitchen.InKitchenViewModelFactory
import com.example.inventory.ui.main.warehouse.WarehouseScreen
import com.example.inventory.ui.main.warehouse.WarehouseViewModel
import com.example.inventory.ui.main.warehouse.WarehouseViewModelFactory
import com.example.inventory.ui.theme.Theme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize Supabase client
        val client = createSupabaseClient(
            supabaseUrl = "https://eevrlqnhmmcauhyassuz.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVldnJscW5obW1jYXVoeWFzc3V6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk2NzkwMjQsImV4cCI6MjA3NTI1NTAyNH0.eDPrdeUeIYysSnAA16J601y5simVmyQg3wm_jBlNunY"
        ) {
            install(Postgrest)
        }

        // ✅ Initialize Supabase service and repository
        val supabaseService = SupabaseService(client)
        val repository = WarehouseRepository(supabaseService)

        // ✅ Create ViewModel factories
        val warehouseFactory = WarehouseViewModelFactory(
            repository = repository,
            supabaseService = supabaseService
        )

        val inKitchenFactory = InKitchenViewModelFactory(repository)

        // ✅ Compose UI
        setContent {
            Theme {
                val warehouseViewModel: WarehouseViewModel = viewModel(factory = warehouseFactory)
                val inKitchenViewModel: InKitchenViewModel = viewModel(factory = inKitchenFactory)

                App(inventoryScreen = {
                    WarehouseScreen(
                        warehouseViewModel = warehouseViewModel,
                        inKitchenViewModel = inKitchenViewModel
                    )
                })
            }
        }
    }
}
