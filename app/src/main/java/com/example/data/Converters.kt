package com.example.data

import androidx.room.TypeConverter
import com.example.model.DeviceCategory
import com.example.model.InventoryType
import com.example.model.PaymentStatus
import com.example.model.RepairStatus

class Converters {
    @TypeConverter
    fun fromInventoryType(type: InventoryType): String = type.name

    @TypeConverter
    fun toInventoryType(value: String): InventoryType = try {
        InventoryType.valueOf(value)
    } catch (e: Exception) {
        InventoryType.REPUESTO
    }
    @TypeConverter
    fun fromDeviceCategory(category: DeviceCategory): String = category.name

    @TypeConverter
    fun toDeviceCategory(value: String): DeviceCategory = try {
        DeviceCategory.valueOf(value)
    } catch (e: Exception) {
        DeviceCategory.AUDIO_OTHER
    }

    @TypeConverter
    fun fromRepairStatus(status: RepairStatus): String = status.name

    @TypeConverter
    fun toRepairStatus(value: String): RepairStatus = try {
        RepairStatus.valueOf(value)
    } catch (e: Exception) {
        RepairStatus.RECIBIDO
    }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = try {
        PaymentStatus.valueOf(value)
    } catch (e: Exception) {
        PaymentStatus.PENDIENTE
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(separator = "|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|||").filter { it.isNotBlank() }
    }
}
