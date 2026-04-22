package com.github.clabersmith.sleepplayer.testutil.domain.repository

import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxDownloadStatus
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSfxRepository : SfxRepository {

    private val _status = MutableStateFlow(SfxDownloadStatus())
    override val status: StateFlow<SfxDownloadStatus> = _status

    var startDownloadCalled = false

    /**
     * Optional scripted behavior when startDownload() is invoked.
     * Tests can override this.
     */
    var onStartDownload: (suspend () -> Unit)? = null

    override suspend fun startDownload() {
        startDownloadCalled = true

        onStartDownload?.invoke()
    }

    // -----------------------------------
    // Test Helpers (this is the real value)
    // -----------------------------------

    fun emitIdle() {
        _status.value = SfxDownloadStatus()
    }

    fun emitStarting() {
        _status.value = SfxDownloadStatus(
            isDownloading = true,
            message = "Starting download..."
        )
    }

    fun emitProgress(current: Int, total: Int) {
        _status.value = SfxDownloadStatus(
            isDownloading = true,
            message = "Updating $current of $total",
            current = current
        )
    }

    fun emitComplete(timestamp: Long = System.currentTimeMillis()) {
        _status.value = SfxDownloadStatus(
            isDownloading = false,
            message = "Download complete",
            lastFullUpdateAt = timestamp
        )
    }

    fun emitUpToDate() {
        _status.value = SfxDownloadStatus(
            isDownloading = false,
            message = "Everything is up to date",
            isUpToDate = true
        )
    }

    fun emitError(message: String = "Error") {
        _status.value = SfxDownloadStatus(
            isDownloading = false,
            message = message
        )
    }
}