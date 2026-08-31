package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DateFilterOption
import com.example.model.RepairStatus

@Composable
fun FilterBar(
    selectedStatus: RepairStatus?,
    showOnlyActive: Boolean,
    dateFilter: DateFilterOption,
    customDateStart: Long?,
    customDateEnd: Long?,
    isDateSortDescending: Boolean,
    onDateFilterSelected: (DateFilterOption) -> Unit,
    onToggleDateSort: () -> Unit,
    onStatusSelected: (RepairStatus?) -> Unit,
    onToggleActive: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateScrollState = rememberScrollState()
    val statusScrollState = rememberScrollState()
    val isAllStatusSelected = selectedStatus == null && !showOnlyActive

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // Row 1: Date Sorting + Date Filter Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(dateScrollState)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort Order Button (Mayor fecha / Menor fecha)
            SuggestionChip(
                onClick = onToggleDateSort,
                label = {
                    Text(
                        text = if (isDateSortDescending) "Mayor fecha" else "Menor fecha",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (isDateSortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (isDateSortDescending) "Mayor fecha primero" else "Menor fecha primero",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("btn_toggle_date_sort")
            )

            // Date Filters: Todas, Hoy, 7 días, Este mes, Personalizado
            DateFilterOption.values().forEach { option ->
                val isSelected = dateFilter == option
                val labelText = when {
                    option == DateFilterOption.CUSTOM && isSelected && (customDateStart != null || customDateEnd != null) -> {
                        DateFilterOption.formatCustomRange(customDateStart, customDateEnd)
                    }
                    else -> option.shortLabel
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onDateFilterSelected(option) },
                    label = { Text(labelText, fontSize = 11.sp) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else if (option == DateFilterOption.CUSTOM) {
                        {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_date_${option.name}")
                )
            }
        }

        // Row 2: Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(statusScrollState)
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Todos" Chip
            FilterChip(
                selected = isAllStatusSelected,
                onClick = onClearFilters,
                label = { Text("Todos", fontSize = 11.sp) },
                leadingIcon = if (isAllStatusSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("filter_all_orders")
            )

            // "Activos en Taller" Quick Toggle Chip
            FilterChip(
                selected = showOnlyActive,
                onClick = onToggleActive,
                label = { Text("Activos en Taller", fontSize = 11.sp) },
                leadingIcon = if (showOnlyActive) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("filter_only_active")
            )

            // Status Filter Chips (exclusive order statuses)
            RepairStatus.values().forEach { status ->
                val isSelected = !showOnlyActive && selectedStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onStatusSelected(null)
                        } else {
                            onStatusSelected(status)
                        }
                    },
                    label = { Text(status.shortLabel, fontSize = 11.sp) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = status.backgroundColor,
                        selectedLabelColor = status.contentColor
                    ),
                    modifier = Modifier.testTag("filter_status_${status.name}")
                )
            }
        }
    }
}

