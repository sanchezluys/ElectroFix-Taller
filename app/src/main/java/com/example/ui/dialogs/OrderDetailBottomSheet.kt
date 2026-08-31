package com.example.ui.dialogs

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppCurrency
import com.example.model.PaymentStatus
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PaymentBadge
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailBottomSheet(
    order: RepairOrder,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatus: () -> Unit,
    onCallClient: (String) -> Unit,
    onWhatsAppClient: (String, String) -> Unit,
    onShareReceipt: (RepairOrder) -> Unit,
    currency: AppCurrency = AppCurrency.DEFAULT
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("order_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Row: Ticket #, Urgent badge, Actions (Edit, Delete, Close)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.orderNumber,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (order.isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Urgente",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "URGENTE",
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("detail_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Orden",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Device Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = order.deviceCategory.getIcon(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "${order.deviceBrand} ${order.deviceModel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryBadge(category = order.deviceCategory)
                            if (order.serialNumber.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "S/N: ${order.serialNumber}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Status & Change Status Action
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado del Equipo",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusBadge(status = order.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = order.status.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (order.statusNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Última nota: ${order.statusNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onChangeStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_change_status_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar Estado o Agregar Nota")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Client Contact Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información del Cliente",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = order.clientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = order.clientPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (order.clientEmail.isNotBlank()) {
                                Text(
                                    text = order.clientEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = { onCallClient(order.clientPhone) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("detail_call_client")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Llamar",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            val message = "Hola ${order.clientName}, le escribimos desde el Taller ElectroFix respecto a su orden ${order.orderNumber} (${order.deviceBrand} ${order.deviceModel}). Estado actual: ${order.status.label}."
                            IconButton(
                                onClick = { onWhatsAppClient(order.clientPhone, message) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7))
                                    .testTag("detail_whatsapp_client")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Technical Details & Work Notes
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detalles Técnicos y Falla",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Falla Reportada por Cliente:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.reportedIssue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (order.accessoriesIncluded.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Accesorios Recibidos:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = order.accessoriesIncluded,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (order.technicalDiagnosis.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Diagnóstico del Técnico:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = order.technicalDiagnosis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (order.workPerformed.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Trabajo Realizado:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = order.workPerformed,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial & Costs Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Presupuesto y Cobro",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PaymentBadge(paymentStatus = order.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (order.partsCost > 0 || order.laborCost > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Repuestos:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                currency.format(order.partsCost),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mano de obra:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                currency.format(order.laborCost),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            order.getFormattedTotal(currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (order.depositPaid > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Abono recibido:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF166534)
                            )
                            Text(
                                order.getFormattedDeposit(currency),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    if (order.balanceDue > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Saldo pendiente:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                order.getFormattedBalanceDue(currency),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Garantía de reparación:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${order.warrantyDays} días",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Share Receipt Action Button
            FilledTonalButton(
                onClick = { onShareReceipt(order) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_share_receipt_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir Comprobante / WhatsApp / SMS")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Orden") },
            text = { Text("¿Estás seguro de que deseas eliminar permanentemente la orden ${order.orderNumber}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Eliminar")
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
