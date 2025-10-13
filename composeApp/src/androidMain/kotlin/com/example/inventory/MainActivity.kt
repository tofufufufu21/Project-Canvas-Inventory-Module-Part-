package com.example.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class MainActivity : ComponentActivity() {

    private lateinit var supabase: SupabaseClient
    private lateinit var repository: WarehouseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Supabase (same as your existing config)
        supabase = createSupabaseClient(
            supabaseUrl = "https://eevrlqnhmmcauhyassuz.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVldnJscW5obW1jYXVoeWFzc3V6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk2NzkwMjQsImV4cCI6MjA3NTI1NTAyNH0.eDPrdeUeIYysSnAA16J601y5simVmyQg3wm_jBlNunY"
        ) {
            install(Postgrest)
        }

        repository = WarehouseRepository(supabase)

        setContent {
            MaterialTheme {
                // Start at your combined WarehouseScreen (has tab for Warehouse and In-Kitchen)
                WarehouseScreen(repository)
            }
        }
    }
}
