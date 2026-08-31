package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RepairStatus
import com.example.ui.theme.CoralOnTertiaryContainer
import com.example.ui.theme.CoralTertiaryContainer
import com.example.ui.theme.StatusWarrantyBg
import com.example.ui.theme.StatusWarrantyFg
import com.example.ui.theme.TechBlueContainer
import com.example.ui.theme.TechBlueOnContainer
import com.example.viewmodel.WorkshopMetrics

@Composable
fun WorkshopMetricsSummary(
    metrics: WorkshopMetrics,
    onStatusClick: (RepairStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("workshop_metrics_card"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Indicador 1: Activos
        MetricIndicatorItem(
            modifier = Modifier.weight(1f),
            title = "Activos",
            count = metrics.activeInShop,
            icon = Icons.Default.Handyman,
            containerColor = TechBlueContainer,
            contentColor = TechBlueOnContainer,
            testTag = "metric_indicator_activos",
            onClick = { onStatusClick(RepairStatus.EN_REPARACION) }
        )

        // Indicador 2: Pendientes
        MetricIndicatorItem(
            modifier = Modifier.weight(1f),
            title = "Pendientes",
            count = metrics.inDiagnostic + metrics.inRepair,
            icon = Icons.Default.Schedule,
            containerColor = CoralTertiaryContainer.copy(alpha = 0.7f),
            contentColor = CoralOnTertiaryContainer,
            testTag = "metric_indicator_pendientes",
            onClick = { onStatusClick(RepairStatus.DIAGNOSTICO) }
        )

        // Indicador 3: Garantías
        MetricIndicatorItem(
            modifier = Modifier.weight(1f),
            title = "Garantías",
            count = metrics.inWarranty,
            icon = Icons.Default.Security,
            containerColor = StatusWarrantyBg,
            contentColor = StatusWarrantyFg,
            testTag = "metric_indicator_garantias",
            onClick = { onStatusClick(RepairStatus.EN_GARANTIA) }
        )
    }
}

@Composable
private fun MetricIndicatorItem(
    title: String,
    count: Int,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Icono de fondo con transparencia
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.18f),
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 6.dp, y = 4.dp)
            )

            // Contenido frontal (texto y contador)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.90f),
                    maxLines = 1
                )
            }
        }
    }
}


