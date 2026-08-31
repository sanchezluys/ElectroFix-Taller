package com.example.ui.dialogs

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Client
import com.example.util.ContactPickerHelper

@Composable
fun ClientFormDialog(
    initialClient: Client?,
    onDismiss: () -> Unit,
    onSave: (Client) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialClient?.name ?: "") }
    var phone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var email by remember { mutableStateOf(initialClient?.email ?: "") }
    var address by remember { mutableStateOf(initialClient?.address ?: "") }
    var notes by remember { mutableStateOf(initialClient?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            val info = ContactPickerHelper.extractContactInfo(context, contactUri)
            if (info != null) {
                if (info.name.isNotBlank()) name = info.name
                if (info.phone.isNotBlank()) phone = info.phone
                if (info.email.isNotBlank()) email = info.email
                errorMessage = null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialClient != null && initialClient.id > 0) "Editar Cliente" else "Nuevo Cliente",
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
                // Button to import directly from device contacts
                OutlinedButton(
                    onClick = {
                        try {
                            contactPickerLauncher.launch(ContactPickerHelper.createContactPickerIntent())
                        } catch (e: Exception) {
                            errorMessage = "No se pudo abrir la agenda de contactos."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("btn_import_contact_from_phone"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContactPhone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar de Contactos del Teléfono", fontSize = 13.sp)
                }

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nombre Completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_input_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = null
                    },
                    label = { Text("Teléfono / WhatsApp *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_input_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_input_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección / Localidad (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_input_address"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas internas (Opcional)") },
                    placeholder = { Text("Ej: Cliente habitual, empresa, etc.") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_input_notes"),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre es obligatorio"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorMessage = "El teléfono es obligatorio"
                        return@Button
                    }
                    onSave(
                        (initialClient ?: Client(name = name, phone = phone)).copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("client_save_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guardar Cliente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("client_cancel_button")) {
                Text("Cancelar")
            }
        }
    )
}
