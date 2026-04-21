package com.github.clabersmith.sleepplayer.features.sfx.data.repository

import com.github.clabersmith.sleepplayer.features.sfx.data.download.SfxDownloader
import com.github.clabersmith.sleepplayer.features.sfx.data.local.SfxSlotRepository
import com.github.clabersmith.sleepplayer.features.sfx.data.remote.SfxRemoteDataSource
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxDownloadStatus
import com.github.clabersmith.sleepplayer.features.sfx.domain.repository.SfxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DefaultSfxRepository(
    private val remote: SfxRemoteDataSource,
    private val downloader: SfxDownloader,
    private val slotRepo: SfxSlotRepository
) : SfxRepository {

    private val _status = MutableStateFlow(SfxDownloadStatus())
    override val status: StateFlow<SfxDownloadStatus> = _status

    private var running = false

    override suspend fun startDownload() {
        if (running) return
        running = true

        try {
            val feed = remote.fetchFeed()
            val slots = slotRepo.loadSlots()

            val updates = feed.items.filter { item ->
                val local = slots.find { it.index == item.index }
                val localTs = local?.lastDownloadedAt ?: 0L
                item.lastModified > localTs
            }

            if (updates.isEmpty()) {
                _status.value = SfxDownloadStatus(
                    message = "Everything is up to date",
                    isUpToDate = true
                )
                return
            }

            _status.value = _status.value.copy(
                isDownloading = true,
                message = "Starting download..."
            )

            updates.forEachIndexed { i, item ->
                _status.value = _status.value.copy(
                    message = "Updating ${i + 1} of ${updates.size}",
                    current = item.index
                )

                downloader.download(item.index, item.url)

                val now = System.currentTimeMillis()
                slotRepo.updateSlot(item.index, now)
            }

            _status.value = _status.value.copy(
                isDownloading = false,
                message = "Download complete",
                lastFullUpdateAt = System.currentTimeMillis()
            )

        } finally {
            running = false
        }
    }
}