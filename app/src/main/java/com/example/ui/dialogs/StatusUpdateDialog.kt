package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import com.example.ui.components.StatusBadge

@Composable
fun StatusUpdateDialog(
    order: RepairOrder,
    onDismiss: () -> Unit,
    onConfirm: (RepairStatus, String) -> Unit
) {
    // Step state: null means in step 1 (picking status). Non-null means step 2 (note dialog for that status).
    var pendingStatus by remember { mutableStateOf<RepairStatus?>(null) }
    var note by remember { mutableStateOf("") }

    if (pendingStatus == null) {
        // STEP 1: Fast & Clean Status Selector
        StatusSelectionDialog(
            order = order,
            onDismiss = onDismiss,
            onSelectStatus = { newStatus ->
                // Pre-fill with existing note if same status, or clear for a new status transition
                note = if (newStatus == order.status) order.statusNote else ""
                pendingStatus = newStatus
            }
        )
    } else {
        // STEP 2: Dedicated Optional Note Dialog
        val targetStatus = pendingStatus!!
        StatusNoteConfirmationDialog(
            order = order,
            targetStatus = targetStatus,
            note = note,
            onNoteChange = { note = it },
            onBack = { pendingStatus = null },
            onDismiss = onDismiss,
            onConfirm = {
                onConfirm(targetStatus, note.trim())
            }
        )
    }
}

@Composable
private fun StatusSelectionDialog(
    order: RepairOrder,
    onDismiss: () -> Unit,
    onSelectStatus: (RepairStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cambiar Estado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = order.status, useShortLabel = true)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${order.orderNumber} • ${order.deviceBrand} ${order.deviceModel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Toca un nuevo estado para continuar:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                RepairStatus.values().forEach { status ->
                    val isCurrent = order.status == status
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectStatus(status) }
                            .testTag("select_status_${status.name}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) {
                                status.backgroundColor.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) status.contentColor else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isCurrent) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isCurrent) status.contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = status.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isCurrent) status.contentColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(Actual)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = status.contentColor
                                            )
                                        }
                                    }
                                    Text(
                                        text = status.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Seleccionar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_status_update")
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun StatusNoteConfirmationDialog(
    order: RepairOrder,
    targetStatus: RepairStatus,
    note: String,
    onNoteChange: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val statusSuggestions = remember(targetStatus) {
        when (targetStatus) {
            RepairStatus.RECIBIDO -> listOf(
                "Ingreso a taller para revisión",
                "Inspección visual inicial realizada",
                "Equipo en cola de diagnóstico"
            )
            RepairStatus.DIAGNOSTICO -> listOf(
                "Diagnóstico en banco de trabajo",
                "Falla confirmada en circuito principal",
                "Revisión de consumo de corriente y voltajes"
            )
            RepairStatus.PRESUPUESTADO -> listOf(
                "Presupuesto informado al cliente",
                "Esperando confirmación del cliente",
                "Aceptación de costos pendiente"
            )
            RepairStatus.ESPERANDO_REPUESTO -> listOf(
                "Repuesto solicitado a proveedor",
                "Esperando módulo/pantalla en 24-48 hs",
                "Pieza en tránsito"
            )
            RepairStatus.EN_REPARACION -> listOf(
                "Micro-soldadura en proceso",
                "Reemplazando componente defectuoso",
                "Montaje y ensamblado en curso"
            )
            RepairStatus.LISTO -> listOf(
                "Reparación exitosa y testeada al 100%",
                "Equipo listo para retiro",
                "Pruebas de estrés superadas"
            )
            RepairStatus.ENTREGADO -> listOf(
                "Entregado conforme al cliente",
                "Equipo retirado y cobro completado",
                "Entregado con garantía del taller"
            )
            RepairStatus.NO_REPARADO -> listOf(
                "Sin solución técnica / circuito dañado",
                "Repuesto descontinuado no disponible",
                "Presupuesto rechazado por el cliente"
            )
            RepairStatus.EN_GARANTIA -> listOf(
                "Reingreso por garantía",
                "Revisión técnica bajo cobertura",
                "Rechequeo post-entrega"
            )
            RepairStatus.CANCELADO -> listOf(
                "Orden cancelada por el cliente",
                "Devuelto sin intervención"
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nota del Cambio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Transition Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado actual",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            StatusBadge(status = order.status, useShortLabel = true)
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(18.dp)
                        )

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Nuevo estado",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            StatusBadge(status = targetStatus, useShortLabel = true)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Observación o nota (Opcional):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Suggestion chips
                Text(
                    text = "Sugerencias rápidas para ${targetStatus.label}:",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusSuggestions.forEach { suggestion ->
                        FilterChip(
                            selected = note == suggestion,
                            onClick = {
                                onNoteChange(if (note == suggestion) "" else suggestion)
                            },
                            label = {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = BorderStroke(
                                0.8.dp,
                                if (note == suggestion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = {
                        Text(
                            text = "Escribe una nota opcional (o presiona confirmar)...",
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = {
                        if (note.isNotEmpty()) {
                            IconButton(onClick = { onNoteChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Borrar",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("status_note_input"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_status_update")
            ) {
                Text(if (note.isNotBlank()) "Guardar con Nota" else "Confirmar Cambio")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Atrás")
            }
        }
    )
}


