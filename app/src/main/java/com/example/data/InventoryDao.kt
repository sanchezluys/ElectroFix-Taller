package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.InventoryItem
import com.example.model.InventoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE type = :type ORDER BY name ASC")
    fun getItemsByType(type: InventoryType): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR compatibleModels LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET stock = :newStock, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, lastUpdated: Long)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAllItems()
}
