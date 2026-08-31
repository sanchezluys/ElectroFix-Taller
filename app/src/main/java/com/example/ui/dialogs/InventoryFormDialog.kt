package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.InventoryItem
import com.example.model.InventoryType

@Composable
fun InventoryFormDialog(
    initialItem: InventoryItem? = null,
    currency: AppCurrency = AppCurrency.DEFAULT,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    val isEditing = initialItem != null

    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var selectedType by remember { mutableStateOf(initialItem?.type ?: InventoryType.REPUESTO) }
    var category by remember { mutableStateOf(initialItem?.category ?: if (initialItem?.type == InventoryType.EQUIPO) "Laptops" else "Pantallas") }
    var sku by remember { mutableStateOf(initialItem?.sku ?: "") }
    var stock by remember { mutableIntStateOf(initialItem?.stock ?: 1) }
    var minStock by remember { mutableIntStateOf(initialItem?.minStock ?: 2) }
    var costPriceStr by remember { mutableStateOf(if ((initialItem?.costPrice ?: 0.0) > 0) initialItem!!.costPrice.toString() else "") }
    var salePriceStr by remember { mutableStateOf(if ((initialItem?.salePrice ?: 0.0) > 0) initialItem!!.salePrice.toString() else "") }
    var compatibleModels by remember { mutableStateOf(initialItem?.compatibleModels ?: "") }
    var location by remember { mutableStateOf(initialItem?.location ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonCategories = if (selectedType == InventoryType.REPUESTO) {
        listOf("Pantallas", "Baterías", "Pines de Carga", "Almacenamiento", "RAM", "Insumos", "Cámaras", "Carcasas", "Otros")
    } else {
        listOf("Smartphones", "Laptops", "Tablets", "Consolas", "PCs de Escritorio", "Audio / Parlantes", "Accesorios", "Otros")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .testTag("dialog_inventory_form"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (selectedType == InventoryType.EQUIPO) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selectedType == InventoryType.EQUIPO) Icons.Default.Devices else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (selectedType == InventoryType.EQUIPO) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEditing) "Editar Ítem" else "Nuevo en Inventario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedType == InventoryType.EQUIPO) "Equipo o Dispositivo" else "Repuesto o Componente",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector: Repuesto vs Equipo
                Text(
                    text = "Tipo de Registro *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedType == InventoryType.REPUESTO,
                        onClick = {
                            selectedType = InventoryType.REPUESTO
                            if (category == "Laptops" || category == "Smartphones") category = "Pantallas"
                        },
                        label = { Text("🛠️ Repuesto / Pieza") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_type_repuesto"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )

                    FilterChip(
                        selected = selectedType == InventoryType.EQUIPO,
                        onClick = {
                            selectedType = InventoryType.EQUIPO
                            if (category == "Pantallas" || category == "Baterías") category = "Smartphones"
                        },
                        label = { Text("📱 Equipo / Aparato") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_type_equipo"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text(if (selectedType == InventoryType.EQUIPO) "Nombre del Equipo / Modelo *" else "Nombre del Repuesto *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null)
                    },
                    placeholder = {
                        Text(if (selectedType == InventoryType.EQUIPO) "Ej. Lenovo ThinkPad T480s" else "Ej. Pantalla OLED iPhone 13")
                    },
                    isError = errorMessage != null && name.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inventory_name"),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text(
                    text = "Categoría:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonCategories.take(4).forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría personalizada") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inventory_category"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SKU / Serial Code & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("Código / SKU") },
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inventory_sku"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Ubicación / Estante") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inventory_location"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stock & Min Stock Counters
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Existencia en Stock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Unidades disponibles ahora",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (stock > 0) stock-- },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Menos")
                                }

                                Text(
                                    text = stock.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconButton(
                                    onClick = { stock++ },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Más")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Alerta de Stock Mínimo",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Avisa cuando el stock sea menor o igual",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (minStock > 0) minStock-- },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = minStock.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                IconButton(
                                    onClick = { minStock++ },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prices: Cost Price & Sale Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Costo (${currency.symbol})") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inventory_cost"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Precio Venta (${currency.symbol})") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_inventory_sale_price"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compatible Models
                OutlinedTextField(
                    value = compatibleModels,
                    onValueChange = { compatibleModels = it },
                    label = { Text("Modelos o Equipos Compatibles") },
                    placeholder = { Text("Ej. iPhone 13, 13 Pro, A2633...") },
                    leadingIcon = { Icon(Icons.Default.Devices, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inventory_compatible"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes / Description
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Observaciones") },
                    placeholder = { Text("Detalles de proveedor, estado o garantía...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_inventory_notes"),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre del ítem es obligatorio"
                        return@Button
                    }
                    val cost = costPriceStr.toDoubleOrNull() ?: 0.0
                    val sale = salePriceStr.toDoubleOrNull() ?: 0.0

                    val itemToSave = InventoryItem(
                        id = initialItem?.id ?: 0L,
                        name = name.trim(),
                        type = selectedType,
                        category = category.trim().ifBlank { "General" },
                        sku = sku.trim(),
                        stock = stock,
                        minStock = minStock,
                        costPrice = cost,
                        salePrice = sale,
                        compatibleModels = compatibleModels.trim(),
                        location = location.trim(),
                        notes = notes.trim(),
                        lastUpdated = System.currentTimeMillis()
                    )
                    onSave(itemToSave)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_inventory_item")
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Registrar Ítem")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
