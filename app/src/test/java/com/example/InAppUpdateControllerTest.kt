package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.update.InAppUpdateController
import com.example.update.InAppUpdateUiState
import com.google.android.play.core.install.model.InstallStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InAppUpdateControllerTest {

    @Test
    fun installStateTransitionsUpdateUiStateCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = InAppUpdateController(context)

        assertEquals(InAppUpdateUiState.Idle, controller.uiState)
        assertFalse(controller.isBannerDismissed)

        controller.onInstallStateChanged(
            installStatus = InstallStatus.PENDING,
            bytesDownloaded = 0L,
            totalBytesToDownload = 0L
        )
        assertEquals(InAppUpdateUiState.Downloading(0), controller.uiState)

        controller.onInstallStateChanged(
            installStatus = InstallStatus.DOWNLOADING,
            bytesDownloaded = 500L,
            totalBytesToDownload = 1000L
        )
        assertEquals(InAppUpdateUiState.Downloading(50), controller.uiState)

        controller.onInstallStateChanged(
            installStatus = InstallStatus.DOWNLOADED,
            bytesDownloaded = 1000L,
            totalBytesToDownload = 1000L
        )
        assertEquals(InAppUpdateUiState.Downloaded, controller.uiState)

        controller.onInstallStateChanged(
            installStatus = InstallStatus.INSTALLED,
            bytesDownloaded = 1000L,
            totalBytesToDownload = 1000L
        )
        assertEquals(InAppUpdateUiState.Idle, controller.uiState)
    }

    @Test
    fun dismissBannerMarksBannerAsDismissed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = InAppUpdateController(context)

        controller.dismissBanner()

        assertTrue(controller.isBannerDismissed)
    }
}
