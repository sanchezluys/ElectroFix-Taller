package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InventoryType(val label: String) {
    REPUESTO("Repuesto"),
    EQUIPO("Equipo")
}

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: InventoryType = InventoryType.REPUESTO,
    val category: String = "General",
    val sku: String = "",
    val stock: Int = 0,
    val minStock: Int = 2,
    val costPrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val compatibleModels: String = "",
    val location: String = "",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = stock <= minStock
}
