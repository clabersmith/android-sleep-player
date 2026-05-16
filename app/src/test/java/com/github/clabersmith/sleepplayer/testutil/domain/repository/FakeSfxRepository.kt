package com.github.clabersmith.sleepplayer.testutil.domain.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.local.PersistedSfxSlot
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxDownloadStatus
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSfxRepository : SfxRepository {

    private val _status = MutableStateFlow(SfxDownloadStatus())
    override val status: StateFlow<SfxDownloadStatus> = _status

    var startDownloadCalled = false

    /**
     * In-memory slots used for tests
     */
    private var slots: MutableList<PersistedSfxSlot> =
        (0 until 4).map { index ->
            PersistedSfxSlot(
            index = index,
            fileName = "sfx_${index + 1}.mp3",
            lastDownloadedAt = 1234L
            )
        }.toMutableList()

    /**
     * Optional scripted behavior when startDownload() is invoked.
     */
    var onStartDownload: (suspend () -> Unit)? = null

    override suspend fun startDownload() {
        startDownloadCalled = true
        onStartDownload?.invoke()
    }

    override suspend fun getSlots(): List<PersistedSfxSlot> {
        return slots
    }

    override suspend fun getFileNameForIndex(index: Int): String? {
        return slots
            .find { it.index == index }
            ?.fileName
            ?.takeIf { it.isNotBlank() }
    }

    // -----------------------------------
    // Test helpers for slots
    // -----------------------------------

    fun setSlots(newSlots: List<PersistedSfxSlot>) {
        slots = newSlots.toMutableList()
    }

    fun setFileName(index: Int, fileName: String) {
        val i = slots.indexOfFirst { it.index == index }
        if (i != -1) {
            slots[i] = slots[i].copy(
                fileName = fileName,
                lastDownloadedAt = System.currentTimeMillis()
            )
        }
    }

    fun clearSlots() {
        slots = slots.map {
            it.copy(fileName = "", lastDownloadedAt = 0L)
        }.toMutableList()
    }

    // -----------------------------------
    // Status emit helpers
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
            message = "Download complete"
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