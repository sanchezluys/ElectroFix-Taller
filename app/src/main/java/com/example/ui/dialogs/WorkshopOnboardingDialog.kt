package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.AppCurrency
import com.example.model.WorkshopSettings
import com.example.util.ImageStorageHelper

@Composable
fun WorkshopOnboardingDialog(
    initialSettings: WorkshopSettings,
    onComplete: (WorkshopSettings) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialSettings.workshopName.ifBlank { "ElectroFix Taller" }) }
    var phone by remember { mutableStateOf(initialSettings.workshopPhone) }
    var address by remember { mutableStateOf(initialSettings.workshopAddress) }
    var warrantyPolicy by remember { mutableStateOf(initialSettings.warrantyPolicy) }
    var selectedCurrency by remember { mutableStateOf(initialSettings.currency) }
    var logoUri by remember { mutableStateOf(initialSettings.logoUri) }

    val pickLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveWorkshopLogo(context, uri)
            logoUri = savedPath ?: uri.toString()
        }
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("dialog_workshop_onboarding"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Bienvenido a ElectroFix Taller",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Configura los datos oficiales de tu taller técnico para los comprobantes, PDFs y órdenes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Logo selector
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { pickLogoLauncher.launch("image/*") }
                        .testTag("btn_onboarding_logo"),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo del Taller",
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Subir Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Logo",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = "Toca para agregar el Logo (opcional)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Taller *") },
                    placeholder = { Text("Ej: ElectroFix Servicio Técnico") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_onboarding_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono / WhatsApp *") },
                    placeholder = { Text("Ej: +34 600 000 000") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_onboarding_phone"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección del Taller") },
                    placeholder = { Text("Ej: Calle Principal 123, Local 4") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_onboarding_address"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = warrantyPolicy,
                    onValueChange = { warrantyPolicy = it },
                    label = { Text("Política de Garantía del Taller *") },
                    placeholder = { Text("Texto que aparecerá en los comprobantes...") },
                    leadingIcon = { Icon(Icons.Default.Policy, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_onboarding_warranty"),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Currency Choice
                Text(
                    text = "Moneda de Trabajo:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppCurrency.entries.forEach { curr ->
                        val isSelected = selectedCurrency == curr
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCurrency = curr }
                                .padding(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = curr.symbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = curr.label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Warning Notice about Lock
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aviso: Una vez seleccionada la moneda y completado el registro, la divisa y los datos oficiales del taller quedarán protegidos y bloqueados en Ajustes para preservar la coherencia contable y legal de tus comprobantes.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val newSettings = initialSettings.copy(
                            workshopName = name.trim(),
                            workshopPhone = phone.trim(),
                            workshopAddress = address.trim(),
                            warrantyPolicy = warrantyPolicy.trim(),
                            currency = selectedCurrency,
                            logoUri = logoUri,
                            isDataLocked = true,
                            isFirstLaunchCompleted = true
                        )
                        onComplete(newSettings)
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_complete_onboarding"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comenzar a Usar ElectroFix", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
