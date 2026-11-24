package com.example.update

import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

sealed interface InAppUpdateUiState {
    data object Idle : InAppUpdateUiState
    data class UpdateAvailable(
        val availableVersionCode: Int,
        val isImmediatePreferred: Boolean,
        val stalenessDays: Int
    ) : InAppUpdateUiState

    data class Downloading(val progressPercent: Int) : InAppUpdateUiState
    data object Downloaded : InAppUpdateUiState
}

enum class ManualUpdateCheckResult {
    CHECKING,
    UPDATE_FOUND,
    UP_TO_DATE,
    UNAVAILABLE
}

private const val HIGH_PRIORITY_UPDATE_THRESHOLD = 4
private const val STALENESS_DAYS_FOR_IMMEDIATE = 7

class InAppUpdateController(
    context: Context
) {
    val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)

    var uiState by mutableStateOf<InAppUpdateUiState>(InAppUpdateUiState.Idle)
        private set

    var isBannerDismissed by mutableStateOf(false)
        private set

    private var cachedAppUpdateInfo: AppUpdateInfo? = null
    private var hasAutoPromptedThisSession = false

    fun onInstallStateChanged(installStatus: Int, bytesDownloaded: Long, totalBytesToDownload: Long) {
        when (installStatus) {
            InstallStatus.PENDING -> {
                uiState = InAppUpdateUiState.Downloading(progressPercent = 0)
            }
            InstallStatus.DOWNLOADING -> {
                val percent = if (totalBytesToDownload > 0L) {
                    ((bytesDownloaded * 100L) / totalBytesToDownload).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                uiState = InAppUpdateUiState.Downloading(progressPercent = percent)
            }
            InstallStatus.DOWNLOADED -> {
                uiState = InAppUpdateUiState.Downloaded
            }
            InstallStatus.INSTALLED -> {
                uiState = InAppUpdateUiState.Idle
                cachedAppUpdateInfo = null
            }
            InstallStatus.CANCELED, InstallStatus.FAILED -> {
                val info = cachedAppUpdateInfo
                if (info != null && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    uiState = buildAvailableState(info)
                } else {
                    uiState = InAppUpdateUiState.Idle
                }
            }
            else -> Unit
        }
    }

    fun checkForUpdates(
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        autoLaunchIfAvailable: Boolean,
        onManualResult: ((ManualUpdateCheckResult) -> Unit)? = null
    ) {
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    cachedAppUpdateInfo = appUpdateInfo

                    when {
                        // 1. Update already downloaded -> prompt user to restart and install
                        appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                            uiState = InAppUpdateUiState.Downloaded
                            onManualResult?.invoke(ManualUpdateCheckResult.UPDATE_FOUND)
                        }

                        // 2. Immediate update already in progress -> resume per Google Play guidelines
                        appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                            startUpdateFlow(
                                appUpdateInfo = appUpdateInfo,
                                updateType = AppUpdateType.IMMEDIATE,
                                launcher = launcher
                            )
                            onManualResult?.invoke(ManualUpdateCheckResult.UPDATE_FOUND)
                        }

                        // 3. New update available on Google Play
                        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                            val availableState = buildAvailableState(appUpdateInfo)
                            val preferredType = selectPreferredUpdateType(appUpdateInfo)

                            if (preferredType != null) {
                                uiState = availableState
                                isBannerDismissed = false
                                onManualResult?.invoke(ManualUpdateCheckResult.UPDATE_FOUND)

                                if (autoLaunchIfAvailable && !hasAutoPromptedThisSession) {
                                    hasAutoPromptedThisSession = true
                                    startUpdateFlow(appUpdateInfo, preferredType, launcher)
                                } else if (onManualResult != null) {
                                    startUpdateFlow(appUpdateInfo, preferredType, launcher)
                                }
                            } else {
                                uiState = InAppUpdateUiState.Idle
                                onManualResult?.invoke(ManualUpdateCheckResult.UP_TO_DATE)
                            }
                        }

                        else -> {
                            uiState = InAppUpdateUiState.Idle
                            onManualResult?.invoke(ManualUpdateCheckResult.UP_TO_DATE)
                        }
                    }
                }
                .addOnFailureListener {
                    // Fail silently on background checks (e.g. devices without Play Store or debug sideloads)
                    onManualResult?.invoke(ManualUpdateCheckResult.UNAVAILABLE)
                }
        } catch (_: Exception) {
            onManualResult?.invoke(ManualUpdateCheckResult.UNAVAILABLE)
        }
    }

    fun launchUpdateFromBanner(
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        val info = cachedAppUpdateInfo
        if (info != null) {
            val type = selectPreferredUpdateType(info) ?: AppUpdateType.FLEXIBLE
            startUpdateFlow(info, type, launcher)
        } else {
            checkForUpdates(launcher = launcher, autoLaunchIfAvailable = true)
        }
    }

    fun completeDownloadedUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (_: Exception) {
            // Ignored safely if Play Store service is temporarily unreachable
        }
    }

    fun dismissBanner() {
        isBannerDismissed = true
    }

    fun handleActivityResult(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            // User postponed or cancelled; keep non-intrusive banner available so they can update later
            val info = cachedAppUpdateInfo
            if (info != null && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                uiState = buildAvailableState(info)
            }
        }
    }

    private fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        updateType: Int,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
        } catch (_: Exception) {
            // Ignore intent sender launch failures gracefully
        }
    }

    private fun selectPreferredUpdateType(appUpdateInfo: AppUpdateInfo): Int? {
        val staleness = appUpdateInfo.clientVersionStalenessDays() ?: 0
        val highPriorityOrStale = appUpdateInfo.updatePriority() >= HIGH_PRIORITY_UPDATE_THRESHOLD ||
            staleness >= STALENESS_DAYS_FOR_IMMEDIATE

        return when {
            highPriorityOrStale && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
            else -> null
        }
    }

    private fun buildAvailableState(appUpdateInfo: AppUpdateInfo): InAppUpdateUiState.UpdateAvailable {
        val staleness = appUpdateInfo.clientVersionStalenessDays() ?: 0
        val immediatePreferred = (appUpdateInfo.updatePriority() >= HIGH_PRIORITY_UPDATE_THRESHOLD ||
            staleness >= STALENESS_DAYS_FOR_IMMEDIATE) &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

        return InAppUpdateUiState.UpdateAvailable(
            availableVersionCode = appUpdateInfo.availableVersionCode(),
            isImmediatePreferred = immediatePreferred,
            stalenessDays = staleness
        )
    }
}

@Composable
fun rememberInAppUpdateController(
    snackbarHostState: SnackbarHostState
): Pair<InAppUpdateController, ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>> {
    val context = LocalContext.current
    val controller = remember { InAppUpdateController(context) }

    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        controller.handleActivityResult(result.resultCode)
    }

    // Register & unregister Play Core flexible download listener cleanly
    DisposableEffect(controller) {
        val listener = InstallStateUpdatedListener { state ->
            controller.onInstallStateChanged(
                installStatus = state.installStatus(),
                bytesDownloaded = state.bytesDownloaded(),
                totalBytesToDownload = state.totalBytesToDownload()
            )
        }
        try {
            controller.appUpdateManager.registerListener(listener)
        } catch (_: Exception) {
            // Safe fallback on environments without Play Services
        }
        onDispose {
            try {
                controller.appUpdateManager.unregisterListener(listener)
            } catch (_: Exception) {
            }
        }
    }

    // Initial check on app launch
    LaunchedEffect(controller) {
        controller.checkForUpdates(
            launcher = updateLauncher,
            autoLaunchIfAvailable = true
        )
    }

    // Resume check per Google Play In-App Updates best practices (stalled IMMEDIATE or DOWNLOADED flexible)
    LifecycleResumeEffect(controller) {
        controller.checkForUpdates(
            launcher = updateLauncher,
            autoLaunchIfAvailable = false
        )
        onPauseOrDispose { }
    }

    // Prompt user via Material 3 Snackbar when a flexible update finishes downloading
    LaunchedEffect(controller.uiState) {
        if (controller.uiState is InAppUpdateUiState.Downloaded) {
            val result = snackbarHostState.showSnackbar(
                message = "Actualización de ElectroFix lista para instalar.",
                actionLabel = "Reiniciar",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                controller.completeDownloadedUpdate()
            }
        }
    }

    return controller to updateLauncher
}

@Composable
fun InAppUpdateBanner(
    state: InAppUpdateUiState,
    isDismissed: Boolean,
    onUpdateClick: () -> Unit,
    onRestartClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = when (state) {
        is InAppUpdateUiState.Idle -> false
        is InAppUpdateUiState.UpdateAvailable -> !isDismissed
        is InAppUpdateUiState.Downloading -> true
        is InAppUpdateUiState.Downloaded -> true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("in_app_update_banner"),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state is InAppUpdateUiState.Downloading) {
                                Icons.Default.Download
                            } else {
                                Icons.Default.SystemUpdate
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (state) {
                                    is InAppUpdateUiState.UpdateAvailable -> "Actualización disponible en Google Play"
                                    is InAppUpdateUiState.Downloading -> "Descargando actualización (${state.progressPercent}%)"
                                    is InAppUpdateUiState.Downloaded -> "Actualización lista para instalar"
                                    is InAppUpdateUiState.Idle -> ""
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (state) {
                                    is InAppUpdateUiState.UpdateAvailable -> "Incluye mejoras de rendimiento y estabilidad."
                                    is InAppUpdateUiState.Downloading -> "Puedes seguir trabajando mientras se descarga."
                                    is InAppUpdateUiState.Downloaded -> "Reinicia para aplicar la nueva versión."
                                    is InAppUpdateUiState.Idle -> ""
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    when (state) {
                        is InAppUpdateUiState.UpdateAvailable -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = onUpdateClick,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = ButtonDefaults.ContentPadding,
                                    modifier = Modifier.testTag("btn_in_app_update_now")
                                ) {
                                    Text("Actualizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = onDismissClick,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_dismiss_in_app_update")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Posponer actualización",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        is InAppUpdateUiState.Downloaded -> {
                            Button(
                                onClick = onRestartClick,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_restart_to_update")
                            ) {
                                Text("Reiniciar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        else -> Unit
                    }
                }

                if (state is InAppUpdateUiState.Downloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }
            }
        }
    }
}
