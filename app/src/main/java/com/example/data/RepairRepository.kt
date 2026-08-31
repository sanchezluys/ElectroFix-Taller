package com.example.data

import com.example.model.Client
import com.example.model.InventoryItem
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import kotlinx.coroutines.flow.Flow

class RepairRepository(
    private val repairOrderDao: RepairOrderDao,
    private val clientDao: ClientDao,
    private val inventoryDao: InventoryDao
) {
    val allOrders: Flow<List<RepairOrder>> = repairOrderDao.getAllOrders()
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    val allInventory: Flow<List<InventoryItem>> = inventoryDao.getAllItems()

    fun searchOrders(query: String): Flow<List<RepairOrder>> {
        return repairOrderDao.searchOrders(query.trim())
    }

    fun getOrdersByClient(clientId: Long): Flow<List<RepairOrder>> {
        return repairOrderDao.getOrdersByClient(clientId)
    }

    fun searchClients(query: String): Flow<List<Client>> {
        return clientDao.searchClients(query.trim())
    }

    fun searchInventory(query: String): Flow<List<InventoryItem>> {
        return inventoryDao.searchItems(query.trim())
    }

    suspend fun getOrderById(id: Long): RepairOrder? = repairOrderDao.getOrderById(id)

    suspend fun saveOrder(order: RepairOrder): Long {
        return if (order.id == 0L) {
            val generatedOrderNumber = if (order.orderNumber.isBlank()) {
                val maxId = repairOrderDao.getMaxId() ?: 1000L
                "ORD-${maxId + 1}"
            } else {
                order.orderNumber
            }
            repairOrderDao.insertOrder(order.copy(orderNumber = generatedOrderNumber))
        } else {
            repairOrderDao.updateOrder(order)
            order.id
        }
    }

    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: RepairStatus,
        note: String
    ) {
        val order = repairOrderDao.getOrderById(orderId) ?: return
        val now = System.currentTimeMillis()
        val updated = order.copy(
            status = newStatus,
            statusNote = note.ifBlank { order.statusNote },
            completedDate = if (newStatus == RepairStatus.LISTO && order.completedDate == null) now else order.completedDate,
            deliveredDate = if (newStatus == RepairStatus.ENTREGADO && order.deliveredDate == null) now else order.deliveredDate
        )
        repairOrderDao.updateOrder(updated)
    }

    suspend fun deleteOrder(order: RepairOrder) {
        repairOrderDao.deleteOrder(order)
    }

    suspend fun saveClient(client: Client): Long {
        return if (client.id == 0L) {
            clientDao.insertClient(client)
        } else {
            clientDao.updateClient(client)
            client.id
        }
    }

    suspend fun deleteClient(client: Client) {
        clientDao.deleteClient(client)
    }

    // Inventory operations
    suspend fun saveInventoryItem(item: InventoryItem): Long {
        return if (item.id == 0L) {
            val generatedSku = if (item.sku.isBlank()) {
                val count = inventoryDao.getCount()
                "SKU-${1000 + count + 1}"
            } else {
                item.sku
            }
            inventoryDao.insertItem(item.copy(sku = generatedSku, lastUpdated = System.currentTimeMillis()))
        } else {
            inventoryDao.updateItem(item.copy(lastUpdated = System.currentTimeMillis()))
            item.id
        }
    }

    suspend fun adjustInventoryStock(itemId: Long, delta: Int) {
        val item = inventoryDao.getItemById(itemId) ?: return
        val newStock = (item.stock + delta).coerceAtLeast(0)
        inventoryDao.updateStock(itemId, newStock, System.currentTimeMillis())
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        inventoryDao.deleteItem(item)
    }

    suspend fun checkAndPopulateIfEmpty() {
        if (repairOrderDao.getCount() == 0) {
            AppDatabase.populateInitialData(clientDao, repairOrderDao, inventoryDao)
        }
    }

    suspend fun deleteAllOrders() {
        repairOrderDao.deleteAllOrders()
    }

    suspend fun deleteAllData() {
        repairOrderDao.deleteAllOrders()
        clientDao.deleteAllClients()
        inventoryDao.deleteAllItems()
    }
}
