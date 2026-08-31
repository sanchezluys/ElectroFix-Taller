package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "repair_orders")
data class RepairOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String, // e.g. "TK-1001"
    val clientId: Long = 0,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String = "",
    val deviceCategory: DeviceCategory,
    val deviceBrand: String,
    val deviceModel: String,
    val serialNumber: String = "",
    val accessoriesIncluded: String = "",
    val reportedIssue: String,
    val technicalDiagnosis: String = "",
    val workPerformed: String = "",
    val status: RepairStatus = RepairStatus.RECIBIDO,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDIENTE,
    val estimatedCost: Double = 0.0,
    val partsCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val depositPaid: Double = 0.0,
    val totalAmount: Double = 0.0,
    val entryDate: Long = System.currentTimeMillis(),
    val estimatedDeliveryDate: Long? = null,
    val completedDate: Long? = null,
    val deliveredDate: Long? = null,
    val warrantyDays: Int = 30,
    val isUrgent: Boolean = false,
    val statusNote: String = "",
    val photos: List<String> = emptyList()
) {
    val balanceDue: Double
        get() = (if (totalAmount > 0) totalAmount else estimatedCost) - depositPaid

    val displayOrderNumber: String
        get() = if (orderNumber.isNotBlank()) orderNumber else "ORD-${if (id > 0) (1000 + id) else 1001}"

    val formattedEntryDate: String
        get() = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(entryDate))

    val formattedEstimatedDate: String
        get() = estimatedDeliveryDate?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        } ?: "No especificada"

    fun getFormattedTotal(settings: WorkshopSettings): String {
        val amount = if (totalAmount > 0) totalAmount else estimatedCost
        return settings.formatMoney(amount)
    }

    fun getFormattedTotal(currency: AppCurrency = AppCurrency.DEFAULT): String {
        val amount = if (totalAmount > 0) totalAmount else estimatedCost
        return currency.format(amount)
    }

    val formattedTotal: String
        get() = getFormattedTotal(AppCurrency.DEFAULT)

    fun getFormattedDeposit(settings: WorkshopSettings): String {
        return settings.formatMoney(depositPaid)
    }

    fun getFormattedDeposit(currency: AppCurrency = AppCurrency.DEFAULT): String {
        return currency.format(depositPaid)
    }

    val formattedDeposit: String
        get() = getFormattedDeposit(AppCurrency.DEFAULT)

    fun getFormattedBalanceDue(settings: WorkshopSettings): String {
        return settings.formatMoney(balanceDue.coerceAtLeast(0.0))
    }

    fun getFormattedBalanceDue(currency: AppCurrency = AppCurrency.DEFAULT): String {
        return currency.format(balanceDue.coerceAtLeast(0.0))
    }

    val formattedBalanceDue: String
        get() = getFormattedBalanceDue(AppCurrency.DEFAULT)

    fun buildShareableReceipt(
        settings: WorkshopSettings = WorkshopSettings()
    ): String {
        return buildString {
            appendLine("🛠️ *${settings.workshopName.ifBlank { "ELECTROFIX TALLER" }.uppercase()}*")
            if (settings.workshopAddress.isNotBlank()) {
                appendLine("📍 *Dirección:* ${settings.workshopAddress}")
            }
            if (settings.workshopPhone.isNotBlank()) {
                appendLine("📞 *Teléfono Taller:* ${settings.workshopPhone}")
            }
            appendLine("-----------------------------")
            appendLine("📋 *COMPROBANTE DE SERVICIO TÉCNICO*")
            appendLine("🎫 *Orden N°:* $displayOrderNumber")
            appendLine("📅 *Fecha Ingreso:* $formattedEntryDate")
            appendLine("👤 *Cliente:* $clientName")
            appendLine("📱 *Contacto:* $clientPhone")
            appendLine("-----------------------------")
            appendLine("💻 *Equipo:* ${deviceCategory.title}")
            appendLine("🏷️ *Marca y Modelo:* $deviceBrand $deviceModel")
            if (serialNumber.isNotBlank()) appendLine("🔢 *Serial / IMEI:* $serialNumber")
            if (accessoriesIncluded.isNotBlank()) appendLine("🔌 *Accesorios Dejados:* $accessoriesIncluded")
            appendLine("-----------------------------")
            appendLine("⚠️ *Falla Reportada:* $reportedIssue")
            if (technicalDiagnosis.isNotBlank()) {
                appendLine("🔬 *Diagnóstico:* $technicalDiagnosis")
            }
            if (workPerformed.isNotBlank()) {
                appendLine("✅ *Trabajo Realizado:* $workPerformed")
            }
            appendLine("🔄 *Estado Actual:* ${status.label}")
            if (statusNote.isNotBlank()) {
                appendLine("📝 *Nota:* $statusNote")
            }
            if (photos.isNotEmpty()) {
                appendLine("📸 *Fotos adjuntas:* ${photos.size} registro(s) visual(es)")
            }
            appendLine("-----------------------------")
            appendLine("💰 *Presupuesto Total:* ${getFormattedTotal(settings)}")
            if (depositPaid > 0) {
                appendLine("💵 *Abono Inicial:* ${getFormattedDeposit(settings)}")
                appendLine("💳 *Saldo Pendiente:* ${getFormattedBalanceDue(settings)}")
            }
            appendLine("📦 *Estado de Pago:* ${paymentStatus.label}")
            if (status == RepairStatus.ENTREGADO && warrantyDays > 0) {
                appendLine("🛡️ *Garantía Otorgada:* $warrantyDays días")
            }
            if (settings.warrantyPolicy.isNotBlank()) {
                appendLine("-----------------------------")
                appendLine("📜 *Términos y Política de Servicio:*")
                appendLine(settings.warrantyPolicy)
            }
            appendLine("-----------------------------")
            appendLine("🔒 $APP_WATERMARK")
        }
    }
}
