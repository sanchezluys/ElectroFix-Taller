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
            orderNumber = "EF-0001",
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
            orderNumber = "EF-0002",
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
}

