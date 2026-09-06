package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.RepairRepository
import com.example.model.AppCurrency
import com.example.model.Client
import com.example.model.DateFilterOption
import com.example.model.DeviceCategory
import com.example.model.InventoryItem
import com.example.model.InventoryType
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import com.example.model.WorkshopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkshopMetrics(
    val totalOrders: Int = 0,
    val activeInShop: Int = 0,
    val inDiagnostic: Int = 0,
    val inRepair: Int = 0,
    val readyForPickup: Int = 0,
    val delivered: Int = 0,
    val inWarranty: Int = 0,
    val urgentCount: Int = 0,
    val pendingAmount: Double = 0.0,
    val totalIncome: Double = 0.0
)

enum class WorkshopTab(val title: String) {
    ORDERS("Órdenes"),
    INVENTORY("Inventario"),
    METRICS("Resumen Taller")
}

data class FilterState(
    val searchQuery: String = "",
    val selectedCategory: DeviceCategory? = null,
    val selectedStatus: RepairStatus? = null,
    val showOnlyActive: Boolean = false,
    val dateFilter: DateFilterOption = DateFilterOption.ALL,
    val customDateStart: Long? = null,
    val customDateEnd: Long? = null,
    val isDateSortDescending: Boolean = true,
    val currentTab: WorkshopTab = WorkshopTab.ORDERS
)

data class DialogState(
    val isOnboardingOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val isDateRangeDialogOpen: Boolean = false,
    val selectedOrderForDetail: RepairOrder? = null,
    val isOrderFormOpen: Boolean = false,
    val orderToEdit: RepairOrder? = null,
    val isStatusUpdateDialogOpen: Boolean = false,
    val orderForStatusUpdate: RepairOrder? = null,
    val isShareDocumentDialogOpen: Boolean = false,
    val orderForShareDocument: RepairOrder? = null,
    val isClientFormOpen: Boolean = false,
    val clientToEdit: Client? = null,
    val selectedClientForDetail: Client? = null,
    val isInventoryFormOpen: Boolean = false,
    val inventoryItemToEdit: InventoryItem? = null,
    val selectedInventoryItemForDetail: InventoryItem? = null
)

data class RepairUiState(
    val orders: List<RepairOrder> = emptyList(),
    val filteredOrders: List<RepairOrder> = emptyList(),
    val clients: List<Client> = emptyList(),
    val filteredClients: List<Client> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val filteredInventory: List<InventoryItem> = emptyList(),
    val metrics: WorkshopMetrics = WorkshopMetrics(),
    val settings: WorkshopSettings = WorkshopSettings(),
    val searchQuery: String = "",
    val selectedCategory: DeviceCategory? = null,
    val selectedStatus: RepairStatus? = null,
    val showOnlyActive: Boolean = false,
    val dateFilter: DateFilterOption = DateFilterOption.ALL,
    val customDateStart: Long? = null,
    val customDateEnd: Long? = null,
    val isDateSortDescending: Boolean = true,
    val currentTab: WorkshopTab = WorkshopTab.ORDERS,
    val isOnboardingOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val isDateRangeDialogOpen: Boolean = false,
    val selectedOrderForDetail: RepairOrder? = null,
    val isOrderFormOpen: Boolean = false,
    val orderToEdit: RepairOrder? = null,
    val isStatusUpdateDialogOpen: Boolean = false,
    val orderForStatusUpdate: RepairOrder? = null,
    val isShareDocumentDialogOpen: Boolean = false,
    val orderForShareDocument: RepairOrder? = null,
    val isClientFormOpen: Boolean = false,
    val clientToEdit: Client? = null,
    val selectedClientForDetail: Client? = null,
    val isInventoryFormOpen: Boolean = false,
    val inventoryItemToEdit: InventoryItem? = null,
    val selectedInventoryItemForDetail: InventoryItem? = null
)

class RepairViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("workshop_settings_prefs", Context.MODE_PRIVATE)
    private val repository: RepairRepository

    private val _settings = MutableStateFlow(loadSettingsFromPrefs())
    private val _filters = MutableStateFlow(FilterState())
    private val _dialogs = MutableStateFlow(
        DialogState(isOnboardingOpen = !loadSettingsFromPrefs().isFirstLaunchCompleted)
    )

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = RepairRepository(db.repairOrderDao(), db.clientDao(), db.inventoryDao())
        viewModelScope.launch {
            try {
                repository.checkAndPopulateIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val dbDataFlow = combine(
        repository.allOrders,
        repository.allClients,
        repository.allInventory
    ) { orders, clients, inventory ->
        Triple(orders, clients, inventory)
    }

    val uiState: StateFlow<RepairUiState> = combine(
        dbDataFlow,
        _settings,
        _filters,
        _dialogs
    ) { (orders, clients, inventory), settings, filters, dialogs ->
        // Compute metrics
        val activeCount = orders.count { it.status.isActiveInWorkshop }
        val diagCount = orders.count { it.status == RepairStatus.DIAGNOSTICO }
        val repairCount = orders.count { it.status == RepairStatus.EN_REPARACION }
        val readyCount = orders.count { it.status == RepairStatus.LISTO }
        val deliveredCount = orders.count { it.status == RepairStatus.ENTREGADO }
        val warrantyCount = orders.count { it.status == RepairStatus.EN_GARANTIA }
        val urgentCount = orders.count { it.isUrgent && it.status.isActiveInWorkshop }
        val pendingAmount = orders.filter { it.status.isActiveInWorkshop }.sumOf { it.balanceDue.coerceAtLeast(0.0) }
        val totalIncome = orders.sumOf { it.depositPaid }

        val metrics = WorkshopMetrics(
            totalOrders = orders.size,
            activeInShop = activeCount,
            inDiagnostic = diagCount,
            inRepair = repairCount,
            readyForPickup = readyCount,
            delivered = deliveredCount,
            inWarranty = warrantyCount,
            urgentCount = urgentCount,
            pendingAmount = pendingAmount,
            totalIncome = totalIncome
        )

        // Filter Orders with Status, Search, Active, and Date Filters + Sorting (Default: Descending by date)
        val filteredOrders = orders.filter { order ->
            val matchesQuery = if (filters.searchQuery.isBlank()) true else {
                val q = filters.searchQuery.lowercase().trim()
                order.clientName.lowercase().contains(q) ||
                        order.clientPhone.contains(q) ||
                        order.orderNumber.lowercase().contains(q) ||
                        order.deviceBrand.lowercase().contains(q) ||
                        order.deviceModel.lowercase().contains(q) ||
                        order.serialNumber.lowercase().contains(q) ||
                        order.reportedIssue.lowercase().contains(q)
            }
            val matchesCategory = filters.selectedCategory == null || order.deviceCategory == filters.selectedCategory
            val matchesStatus = filters.selectedStatus == null || order.status == filters.selectedStatus
            val matchesActive = !filters.showOnlyActive || order.status.isActiveInWorkshop

            val (startDate, endDate) = filters.dateFilter.getRange(filters.customDateStart, filters.customDateEnd)
            val matchesDate = (startDate == null || order.entryDate >= startDate) &&
                    (endDate == null || order.entryDate <= endDate)

            matchesQuery && matchesCategory && matchesStatus && matchesActive && matchesDate
        }.let { list ->
            if (filters.isDateSortDescending) {
                list.sortedByDescending { it.entryDate }
            } else {
                list.sortedBy { it.entryDate }
            }
        }

        // Filter Clients
        val filteredClients = clients.filter { client ->
            if (filters.searchQuery.isBlank()) true else {
                val q = filters.searchQuery.lowercase().trim()
                client.name.lowercase().contains(q) ||
                        client.phone.contains(q) ||
                        client.email.lowercase().contains(q) ||
                        client.address.lowercase().contains(q)
            }
        }

        // Filter Inventory
        val filteredInventory = inventory.filter { item ->
            if (filters.searchQuery.isBlank()) true else {
                val q = filters.searchQuery.lowercase().trim()
                item.name.lowercase().contains(q) ||
                        item.sku.lowercase().contains(q) ||
                        item.category.lowercase().contains(q) ||
                        item.compatibleModels.lowercase().contains(q) ||
                        item.location.lowercase().contains(q)
            }
        }

        // Sync selected order if updated in DB
        val updatedOrderDetail = dialogs.selectedOrderForDetail?.let { current ->
            orders.find { it.id == current.id } ?: current
        }

        // Sync selected inventory item if updated in DB
        val updatedInventoryDetail = dialogs.selectedInventoryItemForDetail?.let { current ->
            inventory.find { it.id == current.id } ?: current
        }

        RepairUiState(
            orders = orders,
            filteredOrders = filteredOrders,
            clients = clients,
            filteredClients = filteredClients,
            inventory = inventory,
            filteredInventory = filteredInventory,
            metrics = metrics,
            settings = settings,
            searchQuery = filters.searchQuery,
            selectedCategory = filters.selectedCategory,
            selectedStatus = filters.selectedStatus,
            showOnlyActive = filters.showOnlyActive,
            dateFilter = filters.dateFilter,
            customDateStart = filters.customDateStart,
            customDateEnd = filters.customDateEnd,
            isDateSortDescending = filters.isDateSortDescending,
            currentTab = filters.currentTab,
            isOnboardingOpen = dialogs.isOnboardingOpen,
            isSettingsDialogOpen = dialogs.isSettingsDialogOpen,
            isDateRangeDialogOpen = dialogs.isDateRangeDialogOpen,
            selectedOrderForDetail = updatedOrderDetail,
            isOrderFormOpen = dialogs.isOrderFormOpen,
            orderToEdit = dialogs.orderToEdit,
            isStatusUpdateDialogOpen = dialogs.isStatusUpdateDialogOpen,
            orderForStatusUpdate = dialogs.orderForStatusUpdate,
            isShareDocumentDialogOpen = dialogs.isShareDocumentDialogOpen,
            orderForShareDocument = dialogs.orderForShareDocument,
            isClientFormOpen = dialogs.isClientFormOpen,
            clientToEdit = dialogs.clientToEdit,
            selectedClientForDetail = dialogs.selectedClientForDetail,
            isInventoryFormOpen = dialogs.isInventoryFormOpen,
            inventoryItemToEdit = dialogs.inventoryItemToEdit,
            selectedInventoryItemForDetail = updatedInventoryDetail
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RepairUiState(settings = loadSettingsFromPrefs())
    )

    private fun loadSettingsFromPrefs(): WorkshopSettings {
        val currencyCode = prefs.getString("selected_currency", AppCurrency.DEFAULT.code)
        val name = prefs.getString("workshop_name", "ElectroFix Taller") ?: "ElectroFix Taller"
        val phone = prefs.getString("workshop_phone", "+57 300 123 4567") ?: "+57 300 123 4567"
        val address = prefs.getString("workshop_address", "Av. Principal 123, Local 4") ?: "Av. Principal 123, Local 4"
        val warranty = prefs.getString(
            "warranty_policy",
            "La garantía cubre exclusivamente la mano de obra técnica y los repuestos especificados en esta orden durante el período acordado. No cubre daños por humedad, sobrevoltaje eléctrico, golpes o intervención por terceros."
        ) ?: ""
        val logoUri = prefs.getString("logo_uri", null)
        val isLocked = prefs.getBoolean("is_data_locked", false)
        val useThousand = prefs.getBoolean("use_thousand_separator", true)
        val separatorCharStr = prefs.getString("thousand_separator_char", ",") ?: ","
        val isOnboarded = prefs.getBoolean("is_onboarded", false)

        return WorkshopSettings(
            workshopName = name,
            workshopPhone = phone,
            workshopAddress = address,
            warrantyPolicy = warranty,
            logoUri = logoUri,
            isDataLocked = isLocked,
            currency = AppCurrency.fromCode(currencyCode),
            useThousandSeparator = useThousand,
            thousandSeparatorChar = separatorCharStr.firstOrNull() ?: ',',
            isFirstLaunchCompleted = isOnboarded
        )
    }

    fun saveSettings(newSettings: WorkshopSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("workshop_name", newSettings.workshopName)
            putString("workshop_phone", newSettings.workshopPhone)
            putString("workshop_address", newSettings.workshopAddress)
            putString("warranty_policy", newSettings.warrantyPolicy)
            putString("logo_uri", newSettings.logoUri)
            putBoolean("is_data_locked", newSettings.isDataLocked)
            putString("selected_currency", newSettings.currency.code)
            putBoolean("use_thousand_separator", newSettings.useThousandSeparator)
            putString("thousand_separator_char", newSettings.thousandSeparatorChar.toString())
            putBoolean("is_onboarded", newSettings.isFirstLaunchCompleted)
            apply()
        }
    }

    fun completeOnboarding(configuredSettings: WorkshopSettings) {
        val finalSettings = configuredSettings.copy(
            isFirstLaunchCompleted = true,
            isDataLocked = true
        )
        saveSettings(finalSettings)
        _dialogs.update { it.copy(isOnboardingOpen = false) }
    }

    fun closeOnboarding() {
        _dialogs.update { it.copy(isOnboardingOpen = false) }
    }

    fun setCurrency(currency: AppCurrency) {
        saveSettings(_settings.value.copy(currency = currency))
    }

    fun openSettingsDialog() {
        _dialogs.update { it.copy(isSettingsDialogOpen = true) }
    }

    fun closeSettingsDialog() {
        _dialogs.update { it.copy(isSettingsDialogOpen = false) }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _dialogs.update {
                it.copy(
                    selectedOrderForDetail = null,
                    selectedClientForDetail = null
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _filters.update { it.copy(searchQuery = query) }
    }

    fun onSelectCategory(category: DeviceCategory?) {
        _filters.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    fun onSelectStatus(status: RepairStatus?) {
        _filters.update {
            it.copy(selectedStatus = if (it.selectedStatus == status) null else status)
        }
    }

    fun toggleOnlyActive() {
        _filters.update { it.copy(showOnlyActive = !it.showOnlyActive) }
    }

    fun setTab(tab: WorkshopTab) {
        _filters.update { it.copy(currentTab = tab) }
    }

    fun onSelectDateFilter(option: DateFilterOption) {
        if (option == DateFilterOption.CUSTOM) {
            openDateRangeDialog()
        } else {
            _filters.update {
                it.copy(dateFilter = option)
            }
        }
    }

    fun onSetCustomDateRange(start: Long?, end: Long?) {
        _filters.update {
            it.copy(
                dateFilter = DateFilterOption.CUSTOM,
                customDateStart = start,
                customDateEnd = end
            )
        }
        closeDateRangeDialog()
    }

    fun toggleDateSort() {
        _filters.update {
            it.copy(isDateSortDescending = !it.isDateSortDescending)
        }
    }

    fun openDateRangeDialog() {
        _dialogs.update { it.copy(isDateRangeDialogOpen = true) }
    }

    fun closeDateRangeDialog() {
        _dialogs.update { it.copy(isDateRangeDialogOpen = false) }
    }

    fun clearFilters() {
        _filters.update {
            it.copy(
                searchQuery = "",
                selectedCategory = null,
                selectedStatus = null,
                showOnlyActive = false,
                dateFilter = DateFilterOption.ALL,
                customDateStart = null,
                customDateEnd = null,
                isDateSortDescending = true
            )
        }
    }

    fun openOrderDetail(order: RepairOrder) {
        _dialogs.update { it.copy(selectedOrderForDetail = order) }
    }

    fun closeOrderDetail() {
        _dialogs.update { it.copy(selectedOrderForDetail = null) }
    }

    fun openNewOrderForm(prefilledClient: Client? = null) {
        val newOrder = prefilledClient?.let {
            RepairOrder(
                orderNumber = "",
                clientId = it.id,
                clientName = it.name,
                clientPhone = it.phone,
                clientEmail = it.email,
                deviceCategory = DeviceCategory.MOBILE,
                deviceBrand = "",
                deviceModel = "",
                reportedIssue = ""
            )
        }
        _dialogs.update {
            it.copy(
                orderToEdit = newOrder,
                isOrderFormOpen = true
            )
        }
    }

    fun openEditOrderForm(order: RepairOrder) {
        _dialogs.update {
            it.copy(
                orderToEdit = order,
                isOrderFormOpen = true
            )
        }
    }

    fun closeOrderForm() {
        _dialogs.update {
            it.copy(
                isOrderFormOpen = false,
                orderToEdit = null
            )
        }
    }

    fun saveOrder(order: RepairOrder) {
        viewModelScope.launch {
            val savedOrder = repository.saveOrder(order)
            // Client upsert
            val client = Client(
                id = order.clientId,
                name = order.clientName,
                phone = order.clientPhone,
                email = order.clientEmail
            )
            repository.saveClient(client)
            closeOrderForm()

            // Open share document dialog so the user can immediately generate PDF/Image/Text with complete order data
            openShareDocumentDialog(savedOrder)
        }
    }

    fun autoSaveOrder(order: RepairOrder) {
        viewModelScope.launch {
            val saved = repository.saveOrder(order)
            _dialogs.update {
                if (it.selectedOrderForDetail?.id == order.id) {
                    it.copy(selectedOrderForDetail = saved)
                } else it
            }
        }
    }

    fun deleteOrder(order: RepairOrder) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            closeOrderDetail()
        }
    }

    fun openStatusUpdateDialog(order: RepairOrder) {
        _dialogs.update {
            it.copy(
                orderForStatusUpdate = order,
                isStatusUpdateDialogOpen = true
            )
        }
    }

    fun closeStatusUpdateDialog() {
        _dialogs.update {
            it.copy(
                isStatusUpdateDialogOpen = false,
                orderForStatusUpdate = null
            )
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: RepairStatus, note: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus, note)
            closeStatusUpdateDialog()
        }
    }

    fun quickAdvanceStatus(order: RepairOrder) {
        val nextStatus = when (order.status) {
            RepairStatus.RECIBIDO -> RepairStatus.DIAGNOSTICO
            RepairStatus.DIAGNOSTICO -> RepairStatus.PRESUPUESTADO
            RepairStatus.PRESUPUESTADO -> RepairStatus.EN_REPARACION
            RepairStatus.ESPERANDO_REPUESTO -> RepairStatus.EN_REPARACION
            RepairStatus.EN_REPARACION -> RepairStatus.LISTO
            RepairStatus.LISTO -> RepairStatus.ENTREGADO
            RepairStatus.ENTREGADO -> RepairStatus.EN_GARANTIA
            RepairStatus.EN_GARANTIA -> RepairStatus.EN_REPARACION
            RepairStatus.NO_REPARADO -> RepairStatus.ENTREGADO
            RepairStatus.CANCELADO -> RepairStatus.RECIBIDO
        }
        if (nextStatus != order.status) {
            viewModelScope.launch {
                repository.updateOrderStatus(
                    order.id,
                    nextStatus,
                    "Estado actualizado a ${nextStatus.label}"
                )
            }
        }
    }

    fun openShareDocumentDialog(order: RepairOrder) {
        _dialogs.update {
            it.copy(
                orderForShareDocument = order,
                isShareDocumentDialogOpen = true
            )
        }
    }

    fun closeShareDocumentDialog() {
        _dialogs.update {
            it.copy(
                isShareDocumentDialogOpen = false,
                orderForShareDocument = null
            )
        }
    }

    fun openNewClientForm() {
        _dialogs.update {
            it.copy(
                clientToEdit = null,
                isClientFormOpen = true
            )
        }
    }

    fun openEditClientForm(client: Client) {
        _dialogs.update {
            it.copy(
                clientToEdit = client,
                isClientFormOpen = true
            )
        }
    }

    fun closeClientForm() {
        _dialogs.update {
            it.copy(
                isClientFormOpen = false,
                clientToEdit = null
            )
        }
    }

    fun saveClient(client: Client) {
        viewModelScope.launch {
            repository.saveClient(client)
            closeClientForm()
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
            _dialogs.update { it.copy(selectedClientForDetail = null) }
        }
    }

    fun openClientDetail(client: Client) {
        _dialogs.update { it.copy(selectedClientForDetail = client) }
    }

    fun closeClientDetail() {
        _dialogs.update { it.copy(selectedClientForDetail = null) }
    }

    // ==========================================
    // INVENTORY MANAGEMENT METHODS
    // ==========================================
    fun openNewInventoryForm() {
        _dialogs.update {
            it.copy(
                inventoryItemToEdit = null,
                isInventoryFormOpen = true
            )
        }
    }

    fun openEditInventoryForm(item: InventoryItem) {
        _dialogs.update {
            it.copy(
                inventoryItemToEdit = item,
                isInventoryFormOpen = true
            )
        }
    }

    fun closeInventoryForm() {
        _dialogs.update {
            it.copy(
                isInventoryFormOpen = false,
                inventoryItemToEdit = null
            )
        }
    }

    fun saveInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.saveInventoryItem(item)
            closeInventoryForm()
        }
    }

    fun adjustInventoryStock(item: InventoryItem, delta: Int) {
        viewModelScope.launch {
            repository.adjustInventoryStock(item.id, delta)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
            _dialogs.update { it.copy(selectedInventoryItemForDetail = null) }
        }
    }

    fun openInventoryDetail(item: InventoryItem) {
        _dialogs.update { it.copy(selectedInventoryItemForDetail = item) }
    }

    fun closeInventoryDetail() {
        _dialogs.update { it.copy(selectedInventoryItemForDetail = null) }
    }

    // Direct Contact Helper Functions
    fun callClient(context: Context, phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handled safely
        }
    }

    fun messageClientWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to SMS
            sendSms(context, phone, message)
        }
    }

    fun sendSms(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanPhone")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handled
        }
    }
}
