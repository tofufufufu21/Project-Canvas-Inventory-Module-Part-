package com.example.inventory



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {

    //bukas ko na gawin rest day muna
    private lateinit var supabase: SupabaseClient
    private lateinit var repository: WarehouseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supabase = createSupabaseClient(
            supabaseUrl = "https://eevrlqnhmmcauhyassuz.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVldnJscW5obW1jYXVoeWFzc3V6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk2NzkwMjQsImV4cCI6MjA3NTI1NTAyNH0.eDPrdeUeIYysSnAA16J601y5simVmyQg3wm_jBlNunY"
        ) {
            install(io.github.jan.supabase.postgrest.Postgrest)
        }

        repository = WarehouseRepository(supabase)

        setContent {
            MaterialTheme {
                WarehouseScreen(repository)
            }
        }
    }
}
