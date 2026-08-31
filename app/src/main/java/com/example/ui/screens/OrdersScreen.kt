package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.DateFilterOption
import com.example.model.DeviceCategory
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import com.example.ui.components.FilterBar
import com.example.ui.components.RepairOrderCard
import com.example.ui.components.WorkshopMetricsSummary
import com.example.viewmodel.WorkshopMetrics

@Composable
fun OrdersScreen(
    orders: List<RepairOrder>,
    allOrdersCount: Int,
    metrics: WorkshopMetrics,
    searchQuery: String,
    selectedCategory: DeviceCategory?,
    selectedStatus: RepairStatus?,
    showOnlyActive: Boolean,
    dateFilter: DateFilterOption = DateFilterOption.ALL,
    customDateStart: Long? = null,
    customDateEnd: Long? = null,
    isDateSortDescending: Boolean = true,
    onSearchChange: (String) -> Unit,
    onCategorySelected: (DeviceCategory?) -> Unit,
    onStatusSelected: (RepairStatus?) -> Unit,
    onToggleActive: () -> Unit,
    onDateFilterSelected: (DateFilterOption) -> Unit = {},
    onToggleDateSort: () -> Unit = {},
    onClearFilters: () -> Unit,
    onOrderClick: (RepairOrder) -> Unit,
    onOrderChangeStatus: (RepairOrder) -> Unit,
    onOrderAdvance: (RepairOrder) -> Unit,
    onOrderShare: (RepairOrder) -> Unit,
    onNewOrderClick: () -> Unit,
    modifier: Modifier = Modifier,
    currency: AppCurrency = AppCurrency.DEFAULT
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_screen")
    ) {
        // Quick Dashboard KPIs at top
        WorkshopMetricsSummary(
            metrics = metrics,
            onStatusClick = { status -> onStatusSelected(status) }
        )

        // Search Bar with clear button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text("Buscar por ticket, cliente, marca, modelo...", fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("orders_search_input")
            )
        }

        // Horizontal Date & Status Filter Bar
        FilterBar(
            selectedStatus = selectedStatus,
            showOnlyActive = showOnlyActive,
            dateFilter = dateFilter,
            customDateStart = customDateStart,
            customDateEnd = customDateEnd,
            isDateSortDescending = isDateSortDescending,
            onDateFilterSelected = onDateFilterSelected,
            onToggleDateSort = onToggleDateSort,
            onStatusSelected = onStatusSelected,
            onToggleActive = onToggleActive,
            onClearFilters = onClearFilters
        )

        // Orders List or Empty State
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (allOrdersCount == 0) "Sin órdenes en el taller" else "No se encontraron resultados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (allOrdersCount == 0) {
                            "Crea tu primera orden para registrar un Smart TV, consola, móvil o PC."
                        } else {
                            "Prueba ajustando el filtro de fechas, estados o el texto de búsqueda."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (allOrdersCount > 0 && (selectedCategory != null || selectedStatus != null || showOnlyActive || searchQuery.isNotBlank() || dateFilter != DateFilterOption.ALL)) {
                        OutlinedButton(
                            onClick = onClearFilters,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_filters_button")
                        ) {
                            Icon(Icons.Default.FilterAltOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restablecer Filtros")
                        }
                    } else {
                        Button(
                            onClick = onNewOrderClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_new_order_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nuevo Ingreso")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${orders.size} orden(es) encontrada(s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (selectedCategory != null || selectedStatus != null || showOnlyActive || searchQuery.isNotBlank() || dateFilter != DateFilterOption.ALL) {
                            Text(
                                text = "Filtros activos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                itemsIndexed(orders, key = { _, order -> order.id }) { index, order ->
                    RepairOrderCard(
                        order = order,
                        onClick = { onOrderClick(order) },
                        onStatusClick = { onOrderChangeStatus(order) },
                        onAdvanceClick = { onOrderAdvance(order) },
                        onShareClick = { onOrderShare(order) },
                        isAlternate = index % 2 != 0,
                        currency = currency
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
