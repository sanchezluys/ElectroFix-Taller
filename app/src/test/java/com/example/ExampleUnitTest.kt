package com.example

import com.example.model.DateFilterOption
import com.example.model.DeviceCategory
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testDateFilterOption_TodayRange() {
        val (start, end) = DateFilterOption.TODAY.getRange(null, null)
        assertNotNull(start)
        assertNotNull(end)
        assertTrue(end!! >= start!!)
        
        val now = System.currentTimeMillis()
        assertTrue(now in start..end)
    }

    @Test
    fun testOrders_DefaultSortedByDescendingEntryDate() {
        val now = System.currentTimeMillis()
        val orderOld = RepairOrder(
            id = 1,
            orderNumber = "ORD-1001",
            clientName = "Cliente 1",
            clientPhone = "123",
            deviceCategory = DeviceCategory.MOBILE,
            deviceBrand = "Samsung",
            deviceModel = "A52",
            reportedIssue = "Pantalla rota",
            entryDate = now - 100000
        )
        val orderNew = RepairOrder(
            id = 2,
            orderNumber = "ORD-1002",
            clientName = "Cliente 2",
            clientPhone = "456",
            deviceCategory = DeviceCategory.COMPUTER,
            deviceBrand = "Dell",
            deviceModel = "Inspiron",
            reportedIssue = "No enciende",
            entryDate = now
        )

        val orders = listOf(orderOld, orderNew)
        val sortedOrders = orders.sortedByDescending { it.entryDate }

        assertEquals(orderNew.id, sortedOrders[0].id)
        assertEquals(orderOld.id, sortedOrders[1].id)
    }

    @Test
    fun testRepairOrder_DisplayOrderNumber_FallbackWhenBlank() {
        val orderBlank = RepairOrder(
            id = 5,
            orderNumber = "",
            clientName = "Carlos Perez",
            clientPhone = "+57 300 123 4567",
            deviceCategory = DeviceCategory.MOBILE,
            deviceBrand = "Xiaomi",
            deviceModel = "Redmi Note 12",
            reportedIssue = "Batería hinchada"
        )
        assertEquals("ORD-1005", orderBlank.displayOrderNumber)

        val orderWithNumber = orderBlank.copy(orderNumber = "ORD-1042")
        assertEquals("ORD-1042", orderWithNumber.displayOrderNumber)
    }

    @Test
    fun testRepairOrder_BuildShareableReceipt_ContainsAllQAData() {
        val order = RepairOrder(
            id = 12,
            orderNumber = "ORD-1012",
            clientName = "María Gómez",
            clientPhone = "+57 310 987 6543",
            deviceCategory = DeviceCategory.MOBILE,
            deviceBrand = "Apple",
            deviceModel = "iPhone 13 Pro",
            serialNumber = "SN-IPH13-8899",
            accessoriesIncluded = "Funda y cargador",
            reportedIssue = "Cambio de display OLED",
            technicalDiagnosis = "Módulo de pantalla roto, touch inoperativo",
            workPerformed = "Reemplazo de display original y prueba táctil",
            status = RepairStatus.EN_REPARACION,
            partsCost = 120.0,
            laborCost = 30.0,
            depositPaid = 50.0,
            totalAmount = 150.0
        )

        val receiptText = order.buildShareableReceipt()

        // QA Assertions for all essential receipt fields
        assertTrue("Debe contener el número de orden", receiptText.contains("ORD-1012"))
        assertTrue("Debe contener el nombre del cliente", receiptText.contains("María Gómez"))
        assertTrue("Debe contener el teléfono del cliente", receiptText.contains("+57 310 987 6543"))
        assertTrue("Debe contener el modelo del equipo", receiptText.contains("iPhone 13 Pro"))
        assertTrue("Debe contener el serial/IMEI", receiptText.contains("SN-IPH13-8899"))
        assertTrue("Debe contener los accesorios", receiptText.contains("Funda y cargador"))
        assertTrue("Debe contener la falla reportada", receiptText.contains("Cambio de display OLED"))
        assertTrue("Debe contener el diagnóstico", receiptText.contains("Módulo de pantalla roto"))
        assertTrue("Debe contener el trabajo realizado", receiptText.contains("Reemplazo de display original"))
        assertTrue("Debe contener el estado actual", receiptText.contains("En Reparación"))
        assertTrue("Debe contener el total y abono", receiptText.contains("Presupuesto Total"))
        assertTrue("Debe contener el saldo pendiente", receiptText.contains("Saldo Pendiente"))
    }
}

