package com.example.inventory

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class InventoryApp : Application() {

    lateinit var supabaseClient: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://eevrlqnhmmcauhyassuz.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVldnJscW5obW1jYXVoeWFzc3V6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk2NzkwMjQsImV4cCI6MjA3NTI1NTAyNH0.eDPrdeUeIYysSnAA16J601y5simVmyQg3wm_jBlNunY"
        ) {
            install(Postgrest)
        }
    }
}