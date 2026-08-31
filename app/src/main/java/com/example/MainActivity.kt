package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.dialogs.ClientDetailDialog
import com.example.ui.dialogs.ClientFormDialog
import com.example.ui.dialogs.DateRangeDialog
import com.example.ui.dialogs.InventoryDetailDialog
import com.example.ui.dialogs.InventoryFormDialog
import com.example.ui.dialogs.OrderDetailBottomSheet
import com.example.ui.dialogs.OrderFormDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.ShareDocumentDialog
import com.example.ui.dialogs.StatusUpdateDialog
import com.example.ui.dialogs.WorkshopOnboardingDialog
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MetricsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RepairViewModel
import com.example.viewmodel.WorkshopTab

class MainActivity : ComponentActivity() {
    private val viewModel: RepairViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ElectroFixApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectroFixApp(
    viewModel: RepairViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.openSettingsDialog() }
                    ) {
                        if (uiState.settings.logoUri != null) {
                            AsyncImage(
                                model = uiState.settings.logoUri,
                                contentDescription = "Logo Taller",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Handyman,
                                        contentDescription = "ElectroFix Logo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = uiState.settings.workshopName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openSettingsDialog() },
                        modifier = Modifier.testTag("btn_open_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes del Taller",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == WorkshopTab.ORDERS,
                    onClick = { viewModel.setTab(WorkshopTab.ORDERS) },
                    icon = {
                        if (uiState.metrics.activeInShop > 0) {
                            BadgedBox(badge = {
                                Badge { Text(uiState.metrics.activeInShop.toString()) }
                            }) {
                                Icon(Icons.Default.HomeRepairService, contentDescription = "Órdenes")
                            }
                        } else {
                            Icon(Icons.Default.HomeRepairService, contentDescription = "Órdenes")
                        }
                    },
                    label = { Text("Órdenes", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_tab_orders")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == WorkshopTab.INVENTORY,
                    onClick = { viewModel.setTab(WorkshopTab.INVENTORY) },
                    icon = {
                        val lowStockCount = uiState.inventory.count { it.isLowStock }
                        if (lowStockCount > 0) {
                            BadgedBox(badge = {
                                Badge(containerColor = Color(0xFFDC2626)) { Text(lowStockCount.toString()) }
                            }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Inventario")
                            }
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Inventario")
                        }
                    },
                    label = { Text("Inventario", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_tab_inventory")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == WorkshopTab.METRICS,
                    onClick = { viewModel.setTab(WorkshopTab.METRICS) },
                    icon = {
                        Icon(Icons.Default.Analytics, contentDescription = "Resumen")
                    },
                    label = { Text("Métricas", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_tab_metrics")
                )
            }
        },
        floatingActionButton = {
            when (uiState.currentTab) {
                WorkshopTab.ORDERS -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openNewOrderForm() },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nuevo Ingreso", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_new_order")
                    )
                }
                WorkshopTab.INVENTORY -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openNewInventoryForm() },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nuevo Ítem", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_new_inventory_item")
                    )
                }
                WorkshopTab.METRICS -> {
                    // No FAB needed for analytics
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                WorkshopTab.ORDERS -> {
                    OrdersScreen(
                        orders = uiState.filteredOrders,
                        allOrdersCount = uiState.orders.size,
                        metrics = uiState.metrics,
                        searchQuery = uiState.searchQuery,
                        selectedCategory = uiState.selectedCategory,
                        selectedStatus = uiState.selectedStatus,
                        showOnlyActive = uiState.showOnlyActive,
                        dateFilter = uiState.dateFilter,
                        customDateStart = uiState.customDateStart,
                        customDateEnd = uiState.customDateEnd,
                        isDateSortDescending = uiState.isDateSortDescending,
                        currency = uiState.settings.currency,
                        onSearchChange = { viewModel.onSearchQueryChange(it) },
                        onCategorySelected = { viewModel.onSelectCategory(it) },
                        onStatusSelected = { viewModel.onSelectStatus(it) },
                        onToggleActive = { viewModel.toggleOnlyActive() },
                        onDateFilterSelected = { viewModel.onSelectDateFilter(it) },
                        onToggleDateSort = { viewModel.toggleDateSort() },
                        onClearFilters = { viewModel.clearFilters() },
                        onOrderClick = { viewModel.openOrderDetail(it) },
                        onOrderChangeStatus = { viewModel.openStatusUpdateDialog(it) },
                        onOrderAdvance = { viewModel.quickAdvanceStatus(it) },
                        onOrderShare = { viewModel.openShareDocumentDialog(it) },
                        onNewOrderClick = { viewModel.openNewOrderForm() }
                    )
                }

                WorkshopTab.INVENTORY -> {
                    InventoryScreen(
                        items = uiState.filteredInventory,
                        currency = uiState.settings.currency,
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { viewModel.onSearchQueryChange(it) },
                        onItemClick = { viewModel.openInventoryDetail(it) },
                        onAdjustStock = { item, delta -> viewModel.adjustInventoryStock(item, delta) },
                        onAddItemClick = { viewModel.openNewInventoryForm() }
                    )
                }

                WorkshopTab.METRICS -> {
                    MetricsScreen(
                        metrics = uiState.metrics,
                        orders = uiState.orders,
                        currency = uiState.settings.currency
                    )
                }
            }
        }
    }

    // ==========================================
    // MODALS AND DIALOGS
    // ==========================================

    // 0. First-Launch Workshop Onboarding Dialog
    if (uiState.isOnboardingOpen) {
        WorkshopOnboardingDialog(
            initialSettings = uiState.settings,
            onComplete = { configuredSettings ->
                viewModel.completeOnboarding(configuredSettings)
            },
            onDismiss = { viewModel.closeOnboarding() }
        )
    }

    // 1. Order Detail Bottom Sheet
    uiState.selectedOrderForDetail?.let { order ->
        OrderDetailBottomSheet(
            order = order,
            currency = uiState.settings.currency,
            onDismiss = { viewModel.closeOrderDetail() },
            onEdit = {
                viewModel.closeOrderDetail()
                viewModel.openEditOrderForm(order)
            },
            onDelete = {
                viewModel.deleteOrder(order)
            },
            onChangeStatus = {
                viewModel.openStatusUpdateDialog(order)
            },
            onCallClient = { phone -> viewModel.callClient(context, phone) },
            onWhatsAppClient = { phone, msg -> viewModel.messageClientWhatsApp(context, phone, msg) },
            onShareReceipt = { ord -> viewModel.openShareDocumentDialog(ord) }
        )
    }

    // 2. Order Form Dialog (New / Edit)
    if (uiState.isOrderFormOpen) {
        OrderFormDialog(
            initialOrder = uiState.orderToEdit,
            existingClients = uiState.clients,
            settings = uiState.settings,
            onDismiss = { viewModel.closeOrderForm() },
            onSave = { order -> viewModel.saveOrder(order) }
        )
    }

    // 3. Status Update Dialog
    if (uiState.isStatusUpdateDialogOpen && uiState.orderForStatusUpdate != null) {
        StatusUpdateDialog(
            order = uiState.orderForStatusUpdate!!,
            onDismiss = { viewModel.closeStatusUpdateDialog() },
            onConfirm = { newStatus, note ->
                viewModel.updateOrderStatus(
                    uiState.orderForStatusUpdate!!.id,
                    newStatus,
                    note
                )
            }
        )
    }

    // 4. Share Document Dialog (PDF / Image / Text with watermark)
    if (uiState.isShareDocumentDialogOpen && uiState.orderForShareDocument != null) {
        ShareDocumentDialog(
            order = uiState.orderForShareDocument!!,
            settings = uiState.settings,
            onDismiss = { viewModel.closeShareDocumentDialog() }
        )
    }

    // 5. Client Form Dialog (New / Edit)
    if (uiState.isClientFormOpen) {
        ClientFormDialog(
            initialClient = uiState.clientToEdit,
            onDismiss = { viewModel.closeClientForm() },
            onSave = { client -> viewModel.saveClient(client) }
        )
    }

    // 6. Client Detail Dialog
    uiState.selectedClientForDetail?.let { client ->
        val clientOrders = uiState.orders.filter {
            it.clientId == client.id || it.clientName.equals(client.name, ignoreCase = true)
        }
        ClientDetailDialog(
            client = client,
            clientOrders = clientOrders,
            currency = uiState.settings.currency,
            onDismiss = { viewModel.closeClientDetail() },
            onEdit = {
                viewModel.closeClientDetail()
                viewModel.openEditClientForm(client)
            },
            onDelete = { viewModel.deleteClient(client) },
            onNewOrderForClient = {
                viewModel.closeClientDetail()
                viewModel.openNewOrderForm(client)
            },
            onSelectOrder = { order ->
                viewModel.openOrderDetail(order)
            },
            onCallClient = { phone -> viewModel.callClient(context, phone) },
            onWhatsAppClient = { phone, msg -> viewModel.messageClientWhatsApp(context, phone, msg) }
        )
    }

    // 7. Inventory Form Dialog (New / Edit)
    if (uiState.isInventoryFormOpen) {
        InventoryFormDialog(
            initialItem = uiState.inventoryItemToEdit,
            currency = uiState.settings.currency,
            onDismiss = { viewModel.closeInventoryForm() },
            onSave = { item -> viewModel.saveInventoryItem(item) }
        )
    }

    // 8. Inventory Detail Dialog
    uiState.selectedInventoryItemForDetail?.let { item ->
        InventoryDetailDialog(
            item = item,
            currency = uiState.settings.currency,
            onDismiss = { viewModel.closeInventoryDetail() },
            onEdit = {
                viewModel.closeInventoryDetail()
                viewModel.openEditInventoryForm(item)
            },
            onDelete = { viewModel.deleteInventoryItem(item) },
            onAdjustStock = { delta -> viewModel.adjustInventoryStock(item, delta) }
        )
    }

    // 9. Settings Dialog (Currency, Thousands Separator, Workshop Data & Delete DB)
    if (uiState.isSettingsDialogOpen) {
        SettingsDialog(
            settings = uiState.settings,
            onSaveSettings = { updated ->
                viewModel.saveSettings(updated)
            },
            onDeleteAllOrders = {
                viewModel.deleteAllData()
            },
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }

    // 10. Custom Date Range Dialog
    if (uiState.isDateRangeDialogOpen) {
        DateRangeDialog(
            initialStart = uiState.customDateStart,
            initialEnd = uiState.customDateEnd,
            onApplyRange = { start, end ->
                viewModel.onSetCustomDateRange(start, end)
            },
            onDismiss = { viewModel.closeDateRangeDialog() }
        )
    }
}
