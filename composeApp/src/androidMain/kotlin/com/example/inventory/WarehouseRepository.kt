package com.example.inventory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
class WarehouseRepository(private val client: SupabaseClient) {

    suspend fun getAllItems(): List<WarehouseItem> {
        return try {
            client.from("warehouse_inventory")
                .select()
                .decodeList<WarehouseItem>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertItem(item: WarehouseItem) {
        try {
            client.from("warehouse_inventory")
                .insert(item)
        } catch (e: Exception) {
            throw e
        }
    }
}