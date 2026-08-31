package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.APP_WATERMARK
import com.example.model.RepairOrder
import com.example.model.WorkshopSettings
import com.example.util.OrderDocumentGenerator

@Composable
fun ShareDocumentDialog(
    order: RepairOrder,
    settings: WorkshopSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_share_document"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Comprobante de Orden",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${order.orderNumber} • ${order.clientName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selecciona el formato para generar y compartir el documento de ingreso del equipo:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Option 1: PDF Document
                ShareFormatCard(
                    title = "Documento PDF Formal",
                    description = "Formato A4 con logo, datos del taller, fotos del equipo, condiciones de garantía y líneas de firma.",
                    icon = Icons.Default.PictureAsPdf,
                    badgeText = "PDF Oficial",
                    badgeColor = Color(0xFFDC2626),
                    onClick = {
                        val pdfFile = OrderDocumentGenerator.generateOrderPdf(context, order, settings)
                        OrderDocumentGenerator.shareDocument(
                            context = context,
                            file = pdfFile,
                            mimeType = "application/pdf",
                            fallbackText = order.buildShareableReceipt(settings)
                        )
                        onDismiss()
                    },
                    testTag = "btn_share_pdf"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Image PNG (WhatsApp / Social Media)
                ShareFormatCard(
                    title = "Comprobante en Imagen (PNG)",
                    description = "Tarjeta gráfica con diseño del taller y miniaturas fotográficas lista para enviar a WhatsApp.",
                    icon = Icons.Default.Image,
                    badgeText = "Ideal WhatsApp",
                    badgeColor = Color(0xFF16A34A),
                    onClick = {
                        val imageFile = OrderDocumentGenerator.generateOrderImage(context, order, settings)
                        OrderDocumentGenerator.shareDocument(
                            context = context,
                            file = imageFile,
                            mimeType = "image/png",
                            fallbackText = order.buildShareableReceipt(settings)
                        )
                        onDismiss()
                    },
                    testTag = "btn_share_image"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Plain Text Message
                ShareFormatCard(
                    title = "Mensaje de Texto",
                    description = "Texto estructurado con emojis listo para copiar o enviar como mensaje directo.",
                    icon = Icons.Default.Message,
                    badgeText = "Texto",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        OrderDocumentGenerator.shareDocument(
                            context = context,
                            file = null,
                            mimeType = "text/plain",
                            fallbackText = order.buildShareableReceipt(settings)
                        )
                        onDismiss()
                    },
                    testTag = "btn_share_text"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Watermark footer label
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Marca de agua: $APP_WATERMARK",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ShareFormatCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = badgeColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
