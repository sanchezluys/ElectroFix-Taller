package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.InventoryItem
import com.example.model.InventoryType

enum class InventoryFilter {
    ALL,
    REPUESTOS,
    EQUIPOS,
    LOW_STOCK
}

@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    currency: AppCurrency,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onItemClick: (InventoryItem) -> Unit,
    onAdjustStock: (InventoryItem, Int) -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(InventoryFilter.ALL) }

    val totalEquipos = items.count { it.type == InventoryType.EQUIPO }
    val totalRepuestos = items.count { it.type == InventoryType.REPUESTO }
    val lowStockCount = items.count { it.isLowStock }

    val filteredList = items.filter { item ->
        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            item.name.contains(searchQuery, ignoreCase = true) ||
                    item.sku.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.compatibleModels.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true)
        }

        val matchesFilter = when (selectedFilter) {
            InventoryFilter.ALL -> true
            InventoryFilter.REPUESTOS -> item.type == InventoryType.REPUESTO
            InventoryFilter.EQUIPOS -> item.type == InventoryType.EQUIPO
            InventoryFilter.LOW_STOCK -> item.isLowStock
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen")
    ) {
        // Summary Header Cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                InventoryStatCard(
                    title = "Total Ítems",
                    value = "${items.size}",
                    subtitle = "${items.sumOf { it.stock }} unidades",
                    icon = Icons.Default.Inventory2,
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = selectedFilter == InventoryFilter.ALL,
                    onClick = { selectedFilter = InventoryFilter.ALL }
                )
            }
            item {
                InventoryStatCard(
                    title = "Repuestos",
                    value = "$totalRepuestos",
                    subtitle = "Piezas e insumos",
                    icon = Icons.Default.Build,
                    color = MaterialTheme.colorScheme.secondary,
                    isSelected = selectedFilter == InventoryFilter.REPUESTOS,
                    onClick = { selectedFilter = InventoryFilter.REPUESTOS }
                )
            }
            item {
                InventoryStatCard(
                    title = "Equipos",
                    value = "$totalEquipos",
                    subtitle = "Dispositivos taller",
                    icon = Icons.Default.Devices,
                    color = Color(0xFF0284C7),
                    isSelected = selectedFilter == InventoryFilter.EQUIPOS,
                    onClick = { selectedFilter = InventoryFilter.EQUIPOS }
                )
            }
            item {
                InventoryStatCard(
                    title = "Stock Bajo",
                    value = "$lowStockCount",
                    subtitle = if (lowStockCount > 0) "¡Requiere reposición!" else "Al día",
                    icon = Icons.Default.Warning,
                    color = if (lowStockCount > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                    isSelected = selectedFilter == InventoryFilter.LOW_STOCK,
                    onClick = { selectedFilter = InventoryFilter.LOW_STOCK }
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar por nombre, SKU, modelo o estante...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("input_search_inventory"),
            singleLine = true
        )

        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == InventoryFilter.ALL,
                    onClick = { selectedFilter = InventoryFilter.ALL },
                    label = { Text("Todos (${items.size})") },
                    modifier = Modifier.testTag("chip_filter_all")
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == InventoryFilter.REPUESTOS,
                    onClick = { selectedFilter = InventoryFilter.REPUESTOS },
                    label = { Text("🛠️ Repuestos ($totalRepuestos)") },
                    modifier = Modifier.testTag("chip_filter_repuestos")
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == InventoryFilter.EQUIPOS,
                    onClick = { selectedFilter = InventoryFilter.EQUIPOS },
                    label = { Text("📱 Equipos ($totalEquipos)") },
                    modifier = Modifier.testTag("chip_filter_equipos")
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == InventoryFilter.LOW_STOCK,
                    onClick = { selectedFilter = InventoryFilter.LOW_STOCK },
                    label = { Text("⚠️ Stock Bajo ($lowStockCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFEE2E2),
                        selectedLabelColor = Color(0xFF991B1B)
                    ),
                    modifier = Modifier.testTag("chip_filter_low_stock")
                )
            }
        }

        // List or Empty State
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No se encontraron ítems para '$searchQuery'" else "No hay elementos en esta categoría",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Registra repuestos, insumos o equipos para el control de inventario del taller.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = onAddItemClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_empty_add_inventory")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nuevo Registro en Inventario")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        currency = currency,
                        onClick = { onItemClick(item) },
                        onIncrement = { onAdjustStock(item, 1) },
                        onDecrement = { onAdjustStock(item, -1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (title == "Stock Bajo" && value != "0") Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    currency: AppCurrency,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("card_inventory_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isLowStock) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (item.type == InventoryType.EQUIPO) Icons.Default.Devices else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.type.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            if (item.sku.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.sku,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Low Stock Badge
                if (item.isLowStock) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Bajo Stock",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            if (item.compatibleModels.isNotBlank() || item.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.compatibleModels.isNotBlank()) {
                        Text(
                            text = item.compatibleModels,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (item.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = item.location,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Prices & Quick Stock Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Venta: ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currency.symbol} ${String.format("%.2f", item.salePrice)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (item.costPrice > 0) {
                        Text(
                            text = "Costo: ${currency.symbol} ${String.format("%.2f", item.costPrice)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Stock Controls
                Surface(
                    color = if (item.isLowStock) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            enabled = item.stock > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Restar Stock",
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "${item.stock} unid.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isLowStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Sumar Stock",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
