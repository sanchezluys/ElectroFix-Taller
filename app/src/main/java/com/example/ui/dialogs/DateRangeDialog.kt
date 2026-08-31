package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DateFilterOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    initialStart: Long?,
    initialEnd: Long?,
    onApplyRange: (start: Long?, end: Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf(initialStart ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(initialEnd ?: System.currentTimeMillis()) }
    var isSelectingStartDate by remember { mutableStateOf(false) }
    var isSelectingEndDate by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("date_range_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Filtrar por Rango de Fechas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date Card
                    Card(
                        onClick = { isSelectingStartDate = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("date_range_start_btn")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Desde",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateFormat.format(Date(startDate)),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // End Date Card
                    Card(
                        onClick = { isSelectingEndDate = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("date_range_end_btn")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Hasta",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateFormat.format(Date(endDate)),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets
                Text(
                    text = "Accesos rápidos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            startDate = now
                            endDate = now
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hoy", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            endDate = cal.timeInMillis
                            cal.add(Calendar.DAY_OF_YEAR, -6)
                            startDate = cal.timeInMillis
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("7 días", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            startDate = cal.timeInMillis
                            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                            endDate = cal.timeInMillis
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Este mes", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sortedStart = minOf(startDate, endDate)
                            val sortedEnd = maxOf(startDate, endDate)
                            onApplyRange(sortedStart, sortedEnd)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_apply_date_range")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aplicar Filtro")
                    }
                }
            }
        }
    }

    // Start Date Picker Dialog
    if (isSelectingStartDate) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { isSelectingStartDate = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    isSelectingStartDate = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isSelectingStartDate = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (isSelectingEndDate) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { isSelectingEndDate = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    isSelectingEndDate = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isSelectingEndDate = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
