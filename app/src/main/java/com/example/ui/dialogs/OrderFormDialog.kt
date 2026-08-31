package com.example.ui.dialogs

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.AppCurrency
import com.example.model.Client
import com.example.model.DeviceCategory
import com.example.model.PaymentStatus
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import com.example.model.WorkshopSettings
import com.example.util.AppPermissionType
import com.example.util.ContactPickerHelper
import com.example.util.ImageStorageHelper
import com.example.util.PermissionHelper
import com.example.util.PermissionRationaleDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormDialog(
    initialOrder: RepairOrder?,
    existingClients: List<Client>,
    onDismiss: () -> Unit,
    onSave: (RepairOrder) -> Unit,
    settings: WorkshopSettings = WorkshopSettings()
) {
    val context = LocalContext.current

    var clientName by remember { mutableStateOf(initialOrder?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(initialOrder?.clientPhone ?: "") }
    var clientEmail by remember { mutableStateOf(initialOrder?.clientEmail ?: "") }
    var clientId by remember { mutableStateOf(initialOrder?.clientId ?: 0L) }

    var category by remember { mutableStateOf(initialOrder?.deviceCategory ?: DeviceCategory.SMART_TV) }
    var brand by remember { mutableStateOf(initialOrder?.deviceBrand ?: "") }
    var model by remember { mutableStateOf(initialOrder?.deviceModel ?: "") }
    var serialNumber by remember { mutableStateOf(initialOrder?.serialNumber ?: "") }
    var accessories by remember { mutableStateOf(initialOrder?.accessoriesIncluded ?: "") }

    var reportedIssue by remember { mutableStateOf(initialOrder?.reportedIssue ?: "") }
    var technicalDiagnosis by remember { mutableStateOf(initialOrder?.technicalDiagnosis ?: "") }
    var workPerformed by remember { mutableStateOf(initialOrder?.workPerformed ?: "") }

    var status by remember { mutableStateOf(initialOrder?.status ?: RepairStatus.RECIBIDO) }
    var paymentStatus by remember { mutableStateOf(initialOrder?.paymentStatus ?: PaymentStatus.PENDIENTE) }

    var partsCostText by remember {
        mutableStateOf(if ((initialOrder?.partsCost ?: 0.0) > 0) initialOrder?.partsCost?.toInt().toString() else "")
    }
    var laborCostText by remember {
        mutableStateOf(if ((initialOrder?.laborCost ?: 0.0) > 0) initialOrder?.laborCost?.toInt().toString() else "")
    }
    var depositText by remember {
        mutableStateOf(if ((initialOrder?.depositPaid ?: 0.0) > 0) initialOrder?.depositPaid?.toInt().toString() else "")
    }
    var totalText by remember {
        mutableStateOf(if ((initialOrder?.totalAmount ?: 0.0) > 0) initialOrder?.totalAmount?.toInt().toString() else "")
    }

    var isUrgent by remember { mutableStateOf(initialOrder?.isUrgent ?: false) }
    var statusNote by remember { mutableStateOf(initialOrder?.statusNote ?: "") }

    // Photos list (up to 5)
    var photos by remember { mutableStateOf(initialOrder?.photos ?: emptyList()) }
    var pendingCameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraPhotoPath by remember { mutableStateOf<String?>(null) }
    var permissionRationaleType by remember { mutableStateOf<AppPermissionType?>(null) }

    var showSerialScannerDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Contact Picker
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            val info = ContactPickerHelper.extractContactInfo(context, contactUri)
            if (info != null) {
                if (info.name.isNotBlank()) clientName = info.name
                if (info.phone.isNotBlank()) clientPhone = info.phone
                if (info.email.isNotBlank()) clientEmail = info.email

                // Match with existing client in database if present
                val phoneDigits = info.phone.filter { it.isDigit() }
                val matched = existingClients.firstOrNull {
                    (it.phone.isNotBlank() && it.phone.filter { c -> c.isDigit() } == phoneDigits) ||
                    it.name.equals(info.name, ignoreCase = true)
                }
                if (matched != null) {
                    clientId = matched.id
                    if (clientEmail.isBlank() && matched.email.isNotBlank()) {
                        clientEmail = matched.email
                    }
                }
                errorMessage = null
            }
        }
    }

    // High quality camera capture launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraPhotoPath != null && photos.size < 5) {
            try {
                val file = File(pendingCameraPhotoPath!!)
                if (file.exists() && file.length() > 0) {
                    photos = photos + pendingCameraPhotoPath!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Permission Request Launcher
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (photos.size < 5) {
                val temp = ImageStorageHelper.createTempImageUri(context, "order")
                if (temp != null) {
                    pendingCameraPhotoUri = temp.first
                    pendingCameraPhotoPath = temp.second
                    takePhotoLauncher.launch(temp.first)
                }
            }
        } else {
            permissionRationaleType = AppPermissionType.CAMERA
        }
    }

    // Contacts Permission Request Launcher
    val requestContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                contactPickerLauncher.launch(ContactPickerHelper.createContactPickerIntent())
            } catch (e: Exception) {
                errorMessage = "No se pudo abrir la agenda de contactos"
            }
        } else {
            permissionRationaleType = AppPermissionType.CONTACTS
        }
    }

    fun launchCameraCapture() {
        if (photos.size >= 5) return
        if (PermissionHelper.isCameraGranted(context)) {
            val temp = ImageStorageHelper.createTempImageUri(context, "order")
            if (temp != null) {
                pendingCameraPhotoUri = temp.first
                pendingCameraPhotoPath = temp.second
                takePhotoLauncher.launch(temp.first)
            }
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchContactPicker() {
        if (PermissionHelper.isContactsGranted(context)) {
            try {
                contactPickerLauncher.launch(ContactPickerHelper.createContactPickerIntent())
            } catch (e: Exception) {
                errorMessage = "No se pudo abrir la agenda de contactos"
            }
        } else {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Gallery Photo Launcher
    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val remainingSlots = 5 - photos.size
        val urisToAdd = uris.take(remainingSlots)
        val savedPaths = urisToAdd.mapNotNull { ImageStorageHelper.saveImageFromUri(context, it, "order") }
        if (savedPaths.isNotEmpty()) {
            photos = photos + savedPaths
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (initialOrder?.id != null && initialOrder.id > 0) "Editar Orden ${initialOrder.displayOrderNumber}" else "Nueva Orden",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("order_form_close")) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                if (clientName.isBlank()) {
                                    errorMessage = "Por favor ingrese el nombre del cliente"
                                    return@Button
                                }
                                if (clientPhone.isBlank()) {
                                    errorMessage = "Por favor ingrese el teléfono de contacto"
                                    return@Button
                                }
                                if (brand.isBlank() || model.isBlank()) {
                                    errorMessage = "Por favor ingrese la marca y modelo del equipo"
                                    return@Button
                                }
                                if (reportedIssue.isBlank()) {
                                    errorMessage = "Por favor detalle la falla reportada"
                                    return@Button
                                }

                                val parts = partsCostText.toDoubleOrNull() ?: 0.0
                                val labor = laborCostText.toDoubleOrNull() ?: 0.0
                                val customTotal = totalText.toDoubleOrNull()
                                val computedTotal = if (customTotal != null && customTotal > 0) customTotal else (parts + labor)
                                val deposit = depositText.toDoubleOrNull() ?: 0.0

                                val determinedPaymentStatus = when {
                                    deposit >= computedTotal && computedTotal > 0 -> PaymentStatus.PAGADO
                                    deposit > 0 -> PaymentStatus.ABONADO
                                    else -> paymentStatus
                                }

                                val orderToSave = (initialOrder ?: RepairOrder(
                                    orderNumber = "",
                                    clientName = clientName,
                                    clientPhone = clientPhone,
                                    deviceCategory = category,
                                    deviceBrand = brand,
                                    deviceModel = model,
                                    reportedIssue = reportedIssue
                                )).copy(
                                    clientId = clientId,
                                    clientName = clientName.trim(),
                                    clientPhone = clientPhone.trim(),
                                    clientEmail = clientEmail.trim(),
                                    deviceCategory = category,
                                    deviceBrand = brand.trim(),
                                    deviceModel = model.trim(),
                                    serialNumber = serialNumber.trim(),
                                    accessoriesIncluded = accessories.trim(),
                                    reportedIssue = reportedIssue.trim(),
                                    technicalDiagnosis = technicalDiagnosis.trim(),
                                    workPerformed = workPerformed.trim(),
                                    status = status,
                                    paymentStatus = determinedPaymentStatus,
                                    partsCost = parts,
                                    laborCost = labor,
                                    depositPaid = deposit,
                                    totalAmount = computedTotal,
                                    estimatedCost = computedTotal,
                                    isUrgent = isUrgent,
                                    statusNote = statusNote.trim(),
                                    photos = photos
                                )

                                onSave(orderToSave)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("order_form_save")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ==========================================
                // SECTION 1: DATOS DEL CLIENTE
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Datos del Cliente",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { launchContactPicker() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_pick_contact_agenda")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buscar en Agenda", fontSize = 11.sp)
                        }

                        if (clientName.isNotBlank() || clientPhone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = ContactPickerHelper.createSaveContactIntent(
                                            name = clientName.trim(),
                                            phone = clientPhone.trim(),
                                            email = clientEmail.trim()
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        errorMessage = "No se pudo abrir la agenda para guardar el contacto"
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("btn_save_contact_agenda")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar en Agenda", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Suggest existing clients if typing matches
                val matchingClients = if (clientName.isNotBlank() && initialOrder?.id == null) {
                    existingClients.filter {
                        (it.name.contains(clientName, ignoreCase = true) || it.phone.contains(clientName)) &&
                        !it.name.equals(clientName.trim(), ignoreCase = true)
                    }.take(3)
                } else emptyList()

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { newName ->
                        clientName = newName
                        errorMessage = null
                        val exact = existingClients.firstOrNull { it.name.equals(newName.trim(), ignoreCase = true) }
                        if (exact != null) {
                            clientId = exact.id
                            if (clientPhone.isBlank()) clientPhone = exact.phone
                            if (clientEmail.isBlank()) clientEmail = exact.email
                        }
                    },
                    label = { Text("Nombre del Cliente *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { launchContactPicker() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "Buscar contacto en agenda del teléfono",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_client_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (matchingClients.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingClients.forEach { client ->
                            SuggestionChip(
                                onClick = {
                                    clientName = client.name
                                    clientPhone = client.phone
                                    clientEmail = client.email
                                    clientId = client.id
                                },
                                label = { Text("${client.name} (${client.phone})", fontSize = 11.sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = {
                        clientPhone = it
                        errorMessage = null
                    },
                    label = { Text("Teléfono / WhatsApp *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_client_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientEmail,
                    onValueChange = { clientEmail = it },
                    label = { Text("Email (Opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_client_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // ==========================================
                // SECTION 2: DATOS DEL EQUIPO
                // ==========================================
                Text(
                    text = "2. Tipo y Datos del Equipo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Category Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeviceCategory.values().forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat.title.split("/")[0].trim(), fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.getIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("category_select_${cat.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = {
                            brand = it
                            errorMessage = null
                        },
                        label = { Text("Marca * (ej: Samsung, Sony)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_device_brand"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = model,
                        onValueChange = {
                            model = it
                            errorMessage = null
                        },
                        label = { Text("Modelo * (ej: PS5, 55\" LED)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_device_model"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Serial with Scanner Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("N° Serie / IMEI") },
                        placeholder = { Text("O escanear con cámara ->") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_device_serial"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { showSerialScannerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("btn_scan_serial")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear IMEI/Serial")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Escanear", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = accessories,
                    onValueChange = { accessories = it },
                    label = { Text("Accesorios recibidos con el equipo") },
                    placeholder = { Text("Ej: Control remoto original, cargador, cable de poder...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_device_accessories"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // ==========================================
                // SECTION 3: FOTOS DEL EQUIPO (HASTA 5)
                // ==========================================
                Text(
                    text = "3. Registro Fotográfico (${photos.size}/5)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Toma o adjunta hasta 5 fotos del estado físico del equipo (pantalla, golpes, rayones).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { launchCameraCapture() },
                        enabled = photos.size < 5,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_take_photo_camera")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cámara", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (photos.size < 5) pickGalleryLauncher.launch("image/*")
                        },
                        enabled = photos.size < 5,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_pick_photo_gallery")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galería", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photos thumbnails list
                if (photos.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        photos.forEachIndexed { index, photoPath ->
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = photoPath,
                                    contentDescription = "Foto $index",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .clickable {
                                            photos = photos.filterIndexed { i, _ -> i != index }
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar foto",
                                        tint = Color.White,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // ==========================================
                // SECTION 4: FALLA Y DIAGNÓSTICO
                // ==========================================
                Text(
                    text = "4. Falla y Diagnóstico Técnico",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reportedIssue,
                    onValueChange = {
                        reportedIssue = it
                        errorMessage = null
                    },
                    label = { Text("Falla reportada por el cliente *") },
                    placeholder = { Text("Ej: No enciende, se escucha pero no da imagen, conector roto...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reported_issue"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = technicalDiagnosis,
                    onValueChange = { technicalDiagnosis = it },
                    label = { Text("Diagnóstico técnico inicial (Opcional)") },
                    placeholder = { Text("Ej: Tiras LED quemadas, corto en línea de carga...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_technical_diagnosis"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = workPerformed,
                    onValueChange = { workPerformed = it },
                    label = { Text("Trabajo técnico realizado (Opcional)") },
                    placeholder = { Text("Ej: Sustitución de módulo de display y limpieza...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_work_performed"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Priority Urgent Switch
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUrgent) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isUrgent) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Marcar como Reparación URGENTE",
                                fontSize = 13.sp,
                                fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUrgent) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isUrgent,
                            onCheckedChange = { isUrgent = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFDC2626),
                                checkedTrackColor = Color(0xFFFCA5A5)
                            ),
                            modifier = Modifier.testTag("switch_urgent")
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // ==========================================
                // SECTION 5: PRESUPUESTO Y ESTADO DE INGRESO
                // (Nota: Garantía retirada aquí, se define en la entrega)
                // ==========================================
                Text(
                    text = "5. Presupuesto y Cobro",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = partsCostText,
                        onValueChange = { partsCostText = it },
                        label = { Text("Repuestos (${settings.currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_parts_cost"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = laborCostText,
                        onValueChange = { laborCostText = it },
                        label = { Text("Mano de Obra (${settings.currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_labor_cost"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = it },
                        label = { Text("Total Estimado (${settings.currency.symbol})") },
                        placeholder = {
                            val p = partsCostText.toDoubleOrNull() ?: 0.0
                            val l = laborCostText.toDoubleOrNull() ?: 0.0
                            Text(settings.formatMoney(p + l))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_cost"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it },
                        label = { Text("Abono Inicial (${settings.currency.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_deposit"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status & Note
                Text(
                    text = "Estado Inicial:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RepairStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.shortLabel, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = st.backgroundColor,
                                selectedLabelColor = st.contentColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = statusNote,
                    onValueChange = { statusNote = it },
                    label = { Text("Nota de estado / Recepción") },
                    placeholder = { Text("Ej: Recibido con cable original sin golpes visibles...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_initial_note"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }

    if (showSerialScannerDialog) {
        SerialScannerDialog(
            currentSerial = serialNumber,
            onDismiss = { showSerialScannerDialog = false },
            onSerialDetected = { detected ->
                serialNumber = detected
            }
        )
    }

    if (permissionRationaleType != null) {
        PermissionRationaleDialog(
            type = permissionRationaleType!!,
            onDismiss = { permissionRationaleType = null },
            onRequestPermission = {
                val type = permissionRationaleType!!
                permissionRationaleType = null
                if (type == AppPermissionType.CAMERA) {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            onOpenSettings = {
                permissionRationaleType = null
                PermissionHelper.openAppSettings(context)
            }
        )
    }
}
