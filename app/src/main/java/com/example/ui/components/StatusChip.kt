package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceCategory
import com.example.model.PaymentStatus
import com.example.model.RepairStatus

@Composable
fun StatusBadge(
    status: RepairStatus,
    modifier: Modifier = Modifier,
    useShortLabel: Boolean = false
) {
    val icon: ImageVector = when (status) {
        RepairStatus.RECIBIDO -> Icons.Default.Inventory
        RepairStatus.DIAGNOSTICO -> Icons.Default.Search
        RepairStatus.PRESUPUESTADO -> Icons.Default.PendingActions
        RepairStatus.ESPERANDO_REPUESTO -> Icons.Default.HourglassEmpty
        RepairStatus.EN_REPARACION -> Icons.Default.PlayArrow
        RepairStatus.LISTO -> Icons.Default.CheckCircle
        RepairStatus.ENTREGADO -> Icons.Default.CheckCircle
        RepairStatus.EN_GARANTIA -> Icons.Default.Policy
        RepairStatus.NO_REPARADO -> Icons.Default.Error
        RepairStatus.CANCELADO -> Icons.Default.Error
    }

    Surface(
        color = status.backgroundColor,
        shape = RoundedCornerShape(50.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = status.contentColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (useShortLabel) status.shortLabel.uppercase() else status.label,
                color = status.contentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
fun PaymentBadge(
    paymentStatus: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (paymentStatus) {
        PaymentStatus.PENDIENTE -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
        PaymentStatus.ABONADO -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        PaymentStatus.PAGADO -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = paymentStatus.label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CategoryBadge(
    category: DeviceCategory,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = category.getIcon(),
                contentDescription = category.title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.title.split("/")[0].trim(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
