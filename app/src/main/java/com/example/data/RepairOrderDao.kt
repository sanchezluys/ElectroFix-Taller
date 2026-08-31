package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.RepairOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairOrderDao {
    @Query("SELECT * FROM repair_orders ORDER BY entryDate DESC")
    fun getAllOrders(): Flow<List<RepairOrder>>

    @Query("SELECT * FROM repair_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): RepairOrder?

    @Query("SELECT * FROM repair_orders WHERE clientId = :clientId ORDER BY entryDate DESC")
    fun getOrdersByClient(clientId: Long): Flow<List<RepairOrder>>

    @Query("""
        SELECT * FROM repair_orders 
        WHERE clientName LIKE '%' || :query || '%' 
           OR clientPhone LIKE '%' || :query || '%'
           OR orderNumber LIKE '%' || :query || '%'
           OR deviceBrand LIKE '%' || :query || '%'
           OR deviceModel LIKE '%' || :query || '%'
           OR serialNumber LIKE '%' || :query || '%'
        ORDER BY entryDate DESC
    """)
    fun searchOrders(query: String): Flow<List<RepairOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: RepairOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<RepairOrder>)

    @Update
    suspend fun updateOrder(order: RepairOrder)

    @Delete
    suspend fun deleteOrder(order: RepairOrder)

    @Query("SELECT COUNT(*) FROM repair_orders")
    suspend fun getCount(): Int

    @Query("SELECT MAX(id) FROM repair_orders")
    suspend fun getMaxId(): Long?

    @Query("DELETE FROM repair_orders")
    suspend fun deleteAllOrders()
}
