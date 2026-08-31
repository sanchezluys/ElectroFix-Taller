package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.InventoryItem
import com.example.model.InventoryType

@Composable
fun InventoryDetailDialog(
    item: InventoryItem,
    currency: AppCurrency = AppCurrency.DEFAULT,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: (delta: Int) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .testTag("dialog_inventory_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (item.type == InventoryType.EQUIPO) Icons.Default.Devices else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Surface(
                            color = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.type.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.type == InventoryType.EQUIPO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
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
                // Stock Status Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isLowStock) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = if (item.isLowStock) BorderStroke(1.dp, Color(0xFFFCA5A5)) else null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.isLowStock) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Stock Bajo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                } else {
                                    Text(
                                        text = "Existencias Disponibles",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = "Mínimo de alerta: ${item.minStock} unid.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onAdjustStock(-1) },
                                enabled = item.stock > 0,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Restar")
                            }

                            Text(
                                text = "${item.stock}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isLowStock) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = { onAdjustStock(1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Sumar")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pricing Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Precio al Público / Venta",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${currency.symbol} ${String.format("%.2f", item.salePrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Costo Adquisición",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${currency.symbol} ${String.format("%.2f", item.costPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail Attributes
                if (item.category.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Categoría: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.category, fontSize = 13.sp)
                    }
                }

                if (item.sku.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Código / SKU: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.sku, fontSize = 13.sp)
                    }
                }

                if (item.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ubicación en taller: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(item.location, fontSize = 13.sp)
                    }
                }

                if (item.compatibleModels.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Compatibilidad / Modelo:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.compatibleModels, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Notas:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(item.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onEdit()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_edit_inventory_item")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }

                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_delete_inventory_item")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text("¿Eliminar de inventario?")
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar '${item.name}' del inventario? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
