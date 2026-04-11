package com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model

import com.github.clabersmith.sleepplayer.R
import com.github.clabersmith.sleepplayer.core.playback.WhiteNoiseTrack
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastEpisode
import com.github.clabersmith.sleepplayer.features.podcasts.domain.model.PodcastFeed

sealed class MenuState() {
    abstract val context: MenuContext

    abstract val selectedIndex: Int
    abstract val itemCount: Int

    abstract val title: String

    abstract fun reduce(event: MenuEvent): MenuTransition

    abstract fun copyWithIndex(newIndex: Int): MenuState

    abstract fun withContext(context: MenuContext): MenuState

    protected fun handleCommonEvents(event: MenuEvent): MenuTransition? {
        return when (event) {
            MenuEvent.ScanForwardDown -> MenuTransition(this, effects = listOf(MenuEffect.StartScanForward))
            MenuEvent.ScanForwardUp   -> MenuTransition(this, effects = listOf(MenuEffect.StopScan))
            MenuEvent.ScanBackDown    -> MenuTransition(this, effects = listOf(MenuEffect.StartScanBack))
            MenuEvent.ScanBackUp      -> MenuTransition(this, effects = listOf(MenuEffect.StopScan))
            MenuEvent.MenuLongPress   -> MenuTransition(Home(context))
            else -> null
        }
    }

    data class Home(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = 0 //not used, Home has dynamic items based on now playing state

        override val title = "Menu"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {
                MenuEvent.Confirm -> {
                    when(selectedIndex) {
                        0 ->
                            MenuTransition(
                                newState = Podcasts(context),
                                direction = NavDirection.Forward
                            )

                        1 ->
                            MenuTransition(
                                newState = WhiteNoisePlay(context),
                                direction = NavDirection.Forward
                            )

                        2 ->
                            MenuTransition(
                                newState = Settings(context),
                                direction = NavDirection.Forward
                            )

                        3 ->
                            MenuTransition(
                                newState = this   //Extras
                            )

                        else -> MenuTransition(this)
                    }
                }

                else -> MenuTransition(this)
            }

        }
    }

    data class Podcasts(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = 2

        override val title = "Podcasts"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    when (selectedIndex) {

                        0 -> MenuTransition(
                            newState = Play(
                                context,
                                selectedIndex = 0
                            ),
                            direction = NavDirection.Forward
                        )

                        1 -> MenuTransition(
                            newState = Download(
                                context,
                                selectedIndex = 0
                            ),
                            direction = NavDirection.Forward
                        )

                        else -> MenuTransition(this)
                    }
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Home(
                        context,
                        selectedIndex = 0
                    ),
                    direction = NavDirection.Back
                )

                else -> MenuTransition(this)
            }

        }
    }

    data class Download(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int
            get() = context.slots.size + if (context.slots.size < context.maxSlotsCount) 1 else 0

        override val title = "Downloads"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {

                    val slotCount = context.slots.size
                    val hasAddNew = slotCount < context.maxSlotsCount
                    val addNewIndex = slotCount

                    when {
                        hasAddNew && selectedIndex == addNewIndex ->
                            MenuTransition(
                                newState = Categories(
                                    context = context,
                                    selectedIndex = 0
                                ),
                                direction = NavDirection.Forward
                            )

                        else -> {
                            val slot = context.slots[selectedIndex]

                            MenuTransition(
                                newState = EpisodeDetail(
                                    context,
                                    feedIndex = slot.feedIndex,
                                    episodeIndex = slot.episodeIndex,
                                    episode = slot.loadedEpisode,
                                    origin = EpisodeDetail.Origin.DOWNLOAD,
                                    actionRows = listOf(ActionRow.Delete),
                                    selectedIndex = 0
                                ),
                                direction = NavDirection.Forward
                            )
                        }
                    }
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    newState = Podcasts(context),
                    direction = NavDirection.Back
                )

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Categories(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = context.categories.size

        override val title = "Categories"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val category = context.categories[selectedIndex]

                    val categoryFeeds = context.feeds
                        .filter { it.category == category }

                    MenuTransition(
                        newState = Feeds(
                            context = context,
                            categoryName = category,
                            categoryFeeds = categoryFeeds,
                            selectedIndex = 0
                        ),
                        direction = NavDirection.Forward
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Download(
                        context,
                        selectedIndex = 0
                    ),
                    direction = NavDirection.Back
                )

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class Feeds(
        override val context: MenuContext,
        val categoryName: String,
        val categoryFeeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = categoryFeeds.size

        override val title = "$categoryName Podcasts"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val selectedFeed = categoryFeeds[selectedIndex]
                    val feedIndex = context.feeds.indexOf(selectedFeed)

                    MenuTransition(
                        newState = Episodes(
                            context = context,
                            feedIndex = feedIndex,
                            episodes = selectedFeed.episodes,
                            categoryFeeds = categoryFeeds,

                            selectedIndex = 0
                        ),
                        direction = NavDirection.Forward
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Categories(
                        context
                    ),
                    direction = NavDirection.Back
                )

                else -> MenuTransition(this)
            }
        }
    }

    data class Episodes(
        override val context: MenuContext,
        val feedIndex: Int,
        val episodes: List<PodcastEpisode>,
        val categoryFeeds: List<PodcastFeed>,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int get() = episodes.size

        override val title = "Podcast Episodes"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val episode = episodes[selectedIndex]

                    val alreadyDownloaded =
                        context.slots.any {
                            it.feedIndex == feedIndex &&
                                    it.episodeIndex == selectedIndex
                        }

                    val rows =
                        if (alreadyDownloaded)
                            listOf(ActionRow.AlreadyDownloaded)
                        else
                            listOf(ActionRow.Download)

                    MenuTransition(
                        newState = EpisodeDetail(
                            context = context,
                            feedIndex = feedIndex,
                            episodeIndex = selectedIndex,
                            episode = episode,
                            origin = EpisodeDetail.Origin.EPISODES,
                            actionRows = rows,
                            selectedIndex = 0
                        ),
                        direction = NavDirection.Forward
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Feeds(
                        context = context,
                        categoryName = context.feeds[feedIndex].category,
                        categoryFeeds = categoryFeeds,
                    ),
                    direction = NavDirection.Back
                )

                else ->
                    MenuTransition(this)
            }
        }
    }

    data class EpisodeDetail(
        override val context: MenuContext,
        val feedIndex: Int,
        val episodeIndex: Int,
        val episode: PodcastEpisode,
        val actionRows: List<ActionRow> = emptyList(),
        val isDownloading: Boolean = false,
        val origin: Origin,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int get() = 0  //not used, EpisodeDetail has dynamic items based on episode info and download state

        override val title = "Episode Detail"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {

                    val action = actionRows.getOrNull(selectedIndex)
                        ?: return MenuTransition(this)

                    when (action) {
                        ActionRow.Download ->
                            MenuTransition(
                                newState = copy(
                                    isDownloading = true,
                                    actionRows = listOf(
                                        ActionRow.Downloading(),
                                        ActionRow.Cancel
                                    ),
                                    selectedIndex = 1
                                ),
                                effects = listOf(
                                    MenuEffect.StartDownload(this)
                                )
                            )

                        ActionRow.Cancel ->
                            MenuTransition(
                                newState = copy(
                                    isDownloading = false,
                                    actionRows = listOf(ActionRow.Download),
                                    selectedIndex = 0
                                ),
                                effects = listOf(
                                    MenuEffect.CancelDownload(this)
                                )
                            )

                        ActionRow.Delete ->
                            MenuTransition(
                                newState = Download(context, selectedIndex = 0),
                                effects = listOf(
                                    MenuEffect.DeleteEpisode(this)
                                )
                            )

                        else -> MenuTransition(this)
                    }
                }

                MenuEvent.MenuShortPress -> {

                    if (isDownloading) {
                        return MenuTransition(
                            newState = copy(
                                isDownloading = false,
                                actionRows = listOf(ActionRow.Download),
                                selectedIndex = 0
                            ),
                            effects = listOf(MenuEffect.CancelDownload(this))
                        )
                    }

                    when (origin) {
                        Origin.DOWNLOAD -> MenuTransition(Download(context))
                        Origin.EPISODES -> MenuTransition(
                            Episodes(
                                context = context,
                                feedIndex = feedIndex,
                                episodes = context.feeds[feedIndex].episodes,
                                categoryFeeds = context.feeds.filter { it.category == context.feeds[feedIndex].category },
                                selectedIndex = episodeIndex
                            ),
                            direction = NavDirection.Back
                        )
                        else -> MenuTransition(this)
                    }
                }

                else ->
                    MenuTransition(this)
            }
        }

        enum class Origin {
            EPISODES,
            DOWNLOAD,
        }
    }

    data class Play(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int get() = context.slots.size

        override val title = "Play"

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    if(context.slots.isEmpty()) {
                        return MenuTransition(this)
                    }

                    val slot = context.slots[selectedIndex]

                    MenuTransition(
                        newState = NowPlaying(
                            context = context,
                            slot = slot,
                            origin = NowPlaying.Origin.PLAY,
                            selectedIndex = 0
                        ),
                        effects = listOf(
                            MenuEffect.CheckStartPlayback(slot),
                            MenuEffect.GoToNowPlaying(slot, NowPlaying.Origin.PLAY)
                        )
                    )
                }

                MenuEvent.MenuShortPress -> {
                    MenuTransition (
                        newState = Podcasts(context, selectedIndex = 0),
                        direction = NavDirection.Back
                    )
                }

                else -> MenuTransition(this)
            }
        }
    }

    data class NowPlaying(
        override val context: MenuContext,
        val slot: SlotState,
        val origin: Origin,
        override val selectedIndex: Int = 0
    ) : MenuState() {
        override val itemCount: Int = 1

        override val title = "Now Playing"

        override fun withContext(context: MenuContext) = copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {
            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.MenuShortPress ->

                    when (origin) {
                        Origin.PLAY -> MenuTransition(
                            Play(context)
                        )

                        Origin.HOME -> MenuTransition(
                            Home(context, selectedIndex = 1)
                        )

                        else -> MenuTransition(this)
                    }

                MenuEvent.PlaybackStopped -> MenuTransition(Home(context)) // redirect to Home

                else -> MenuTransition(this)
            }
        }

        enum class Origin {
            HOME,
            PLAY
        }

        override fun copyWithIndex(newIndex: Int) = copy(selectedIndex = newIndex)
    }

    data class WhiteNoisePlay(
        override val context: MenuContext,
        override val selectedIndex: Int = 0,
        val playingIndex: Int? = null
    ) : MenuState() {

        private val tracks = listOf(
            "Phase" to WhiteNoiseTrack(R.raw.phase),
            "Hypnag" to WhiteNoiseTrack(R.raw.hypnag),
            "Air Conditioner" to WhiteNoiseTrack(R.raw.ac),
            "Box Fan" to WhiteNoiseTrack(R.raw.boxfan),
            "Metal Fan" to WhiteNoiseTrack(R.raw.metalfan),
            "Brown" to WhiteNoiseTrack(R.raw.brown),
            "Pink" to WhiteNoiseTrack(R.raw.pink),
            "Green" to WhiteNoiseTrack(R.raw.green)
        )

        override val itemCount: Int get() = tracks.size

        override val title = "White Noise"

        override fun copyWithIndex(newIndex: Int) =
            copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) =
            copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {

                    val track = tracks[selectedIndex].second
                    val currentTrack = context.currentWhiteNoiseTrack

                    if (track == currentTrack) {
                        MenuTransition(
                            newState = this, // no UI change
                            effects = listOf(MenuEffect.StopWhiteNoise)
                        )
                    } else {
                        MenuTransition(
                            newState = this, // no UI change
                            effects = listOf(MenuEffect.StartWhiteNoise(track))
                        )
                    }
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    Home(context, selectedIndex = 1), // returns to White Noise slot
                    direction = NavDirection.Back
                )

                else -> MenuTransition(this)
            }
        }
    }

    data class Settings(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        override val itemCount: Int = 3

        override val title: String = "Settings"

        override fun copyWithIndex(newIndex: Int) =
            copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) =
            copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    when (selectedIndex) {
                        0 -> MenuTransition(
                            newState = PlaybackSettings(context),
                            direction = NavDirection.Forward
                        )

                        1 -> MenuTransition(
                            newState = DisplaySettings(context),
                            direction = NavDirection.Forward
                        )

                        2 -> MenuTransition(
                            newState = AudioSettings(context),
                            direction = NavDirection.Forward
                        )

                        else -> MenuTransition(this)
                    }
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    newState = Home(context, selectedIndex = 2),
                    direction = NavDirection.Back
                )

                else -> MenuTransition(this)
            }
        }
    }

    data class PlaybackSettings(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        private val items = listOf(
            SettingsItem.DuckVolume,
            SettingsItem.AutoFade,
            SettingsItem.AutoStop
        )

        override val itemCount: Int get() = items.size

        override val title: String = "Playback Settings"

        override fun copyWithIndex(newIndex: Int) =
            copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) =
            copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            // Override scan behavior (DO NOT call handleCommonEvents for scan)
            when (event) {

                // -----------------------------
                // Scan Forward (press + hold)
                // -----------------------------
                MenuEvent.ScanForwardDown -> {
                    return MenuTransition(
                        newState = this,
                        effects = listOf(
                            // Immediate single step (tap)
                            MenuEffect.UpdatePlaybackSettings { current ->
                                adjustSetting(current, +1)
                            },

                            // Hold for continuous adjustment
                            MenuEffect.StartRepeatingEffect(
                                MenuEffect.UpdatePlaybackSettings { current ->
                                    adjustSetting(current, +1)
                                }
                            )
                        )
                    )
                }

                // -----------------------------
                // Scan Back (press + hold)
                // -----------------------------
                MenuEvent.ScanBackDown -> {
                    return MenuTransition(
                        newState = this,
                        effects = listOf(
                            // Immediate single step (tap)
                            MenuEffect.UpdatePlaybackSettings { current ->
                                adjustSetting(current, -1)
                            },

                            MenuEffect.StartRepeatingEffect(
                                MenuEffect.UpdatePlaybackSettings { current ->
                                    adjustSetting(current, -1)
                                }
                            )
                        )
                    )
                }

                // -----------------------------
                // Stop repeating on release
                // -----------------------------
                MenuEvent.ScanForwardUp,
                MenuEvent.ScanBackUp -> {
                    return MenuTransition(
                        newState = this,
                        effects = listOf(MenuEffect.StopRepeatingEffect)
                    )
                }

                MenuEvent.MenuLongPress ->
                    return MenuTransition(Home(context))

                else -> {}
            }

            return when (event) {
                MenuEvent.MenuShortPress -> MenuTransition(
                    newState = Settings(context),
                    direction = NavDirection.Back
                )

                else -> MenuTransition(this)
            }
        }

        private fun adjustSetting(
            playbackSettings: com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings,
            delta: Int
        ): com.github.clabersmith.sleepplayer.core.ui.skin.ipod.model.PlaybackSettings {
            return when (items[selectedIndex]) {
                SettingsItem.DuckVolume -> {
                    val updated = (playbackSettings.duckVolumePercent + delta * 10)
                        .coerceIn(0, 100)

                    playbackSettings.copy(duckVolumePercent = updated)
                }

                SettingsItem.AutoFade -> {
                    val values = (1..20).toList()
                    val index = playbackSettings.autoFadeMinutes?.let { values.indexOf(it) } ?: -1
                    val newIndex = (index + delta).coerceIn(-1, values.lastIndex)

                    val updated = if (newIndex == -1) null else values[newIndex]

                    playbackSettings.copy(autoFadeMinutes = updated)
                }

                SettingsItem.AutoStop -> {
                    val values = (1..25).toList()
                    val index = playbackSettings.autoStopMinutes?.let { values.indexOf(it) } ?: -1
                    val newIndex = (index + delta).coerceIn(-1, values.lastIndex)

                    val updated = if (newIndex == -1) null else values[newIndex]

                    playbackSettings.copy(autoStopMinutes = updated)
                }
            }
        }

        sealed class SettingsItem {
            object DuckVolume : SettingsItem()
            object AutoFade : SettingsItem()
            object AutoStop : SettingsItem()
        }
    }

    data class DisplaySettings(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        private val items = listOf(
            Theme.White,
            Theme.Black,
            Theme.Silver,
            Theme.Blue,
            Theme.Green,
            Theme.Pink
        )

        override val itemCount: Int get() = items.size

        override val title: String = "Display"

        override fun copyWithIndex(newIndex: Int) =
            copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) =
            copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            handleCommonEvents(event)?.let { return it }

            return when (event) {

                MenuEvent.Confirm -> {
                    val selectedTheme = items[selectedIndex]

                    MenuTransition(
                        newState = this,
                        effects = listOf(
                            MenuEffect.UpdateDisplayTheme(selectedTheme)
                        )
                    )
                }

                MenuEvent.MenuShortPress -> MenuTransition(
                    newState = Settings(context, selectedIndex = 1),
                    direction = NavDirection.Back
                )

                else -> MenuTransition(this)
            }
        }

        enum class Theme {
            White,
            Black,
            Silver,
            Blue,
            Green,
            Pink
        }
    }

    data class AudioSettings(
        override val context: MenuContext,
        override val selectedIndex: Int = 0
    ) : MenuState() {

        private val items = listOf(
            AudioItem.ClickSound,
            AudioItem.PodcastVolume,
            AudioItem.WhiteNoiseVolume,
        )

        override val itemCount = items.size
        override val title = "Audio"

        override fun copyWithIndex(newIndex: Int) =
            copy(selectedIndex = newIndex)

        override fun withContext(context: MenuContext) =
            copy(context = context)

        override fun reduce(event: MenuEvent): MenuTransition {

            when (event) {

                // -----------------------------
                // Scan Forward (press + hold)
                // -----------------------------
                MenuEvent.ScanForwardDown -> {
                    return when (items[selectedIndex]) {

                        AudioItem.PodcastVolume -> MenuTransition(
                            newState = this,
                            effects = listOf(
                                MenuEffect.UpdateAudioSettings {
                                    it.copy(
                                        defaultPodcastVolume = adjust(it.defaultPodcastVolume, +1)
                                    )
                                },
                                MenuEffect.StartRepeatingEffect(
                                    MenuEffect.UpdateAudioSettings {
                                        it.copy(
                                            defaultPodcastVolume = adjust(it.defaultPodcastVolume, +1)
                                        )
                                    }
                                )
                            )
                        )

                        AudioItem.WhiteNoiseVolume -> MenuTransition(
                            newState = this,
                            effects = listOf(
                                MenuEffect.UpdateAudioSettings {
                                    it.copy(
                                        defaultWhiteNoiseVolume = adjust(it.defaultWhiteNoiseVolume, +1)
                                    )
                                },
                                MenuEffect.StartRepeatingEffect(
                                    MenuEffect.UpdateAudioSettings {
                                        it.copy(
                                            defaultWhiteNoiseVolume = adjust(it.defaultWhiteNoiseVolume, +1)
                                        )
                                    }
                                )
                            )
                        )

                        else -> MenuTransition(this)
                    }
                }

                // -----------------------------
                // Scan Back (press + hold)
                // -----------------------------
                MenuEvent.ScanBackDown -> {
                    return when (items[selectedIndex]) {

                        AudioItem.PodcastVolume -> MenuTransition(
                            newState = this,
                            effects = listOf(
                                MenuEffect.UpdateAudioSettings {
                                    it.copy(
                                        defaultPodcastVolume = adjust(it.defaultPodcastVolume, -1)
                                    )
                                },
                                MenuEffect.StartRepeatingEffect(
                                    MenuEffect.UpdateAudioSettings {
                                        it.copy(
                                            defaultPodcastVolume = adjust(it.defaultPodcastVolume, -1)
                                        )
                                    }
                                )
                            )
                        )

                        AudioItem.WhiteNoiseVolume -> MenuTransition(
                            newState = this,
                            effects = listOf(
                                MenuEffect.UpdateAudioSettings {
                                    it.copy(
                                        defaultWhiteNoiseVolume = adjust(it.defaultWhiteNoiseVolume, -1)
                                    )
                                },
                                MenuEffect.StartRepeatingEffect(
                                    MenuEffect.UpdateAudioSettings {
                                        it.copy(
                                            defaultWhiteNoiseVolume = adjust(it.defaultWhiteNoiseVolume, -1)
                                        )
                                    }
                                )
                            )
                        )

                        else -> MenuTransition(this)
                    }
                }

                // -----------------------------
                // Stop repeating on release
                // -----------------------------
                MenuEvent.ScanForwardUp,
                MenuEvent.ScanBackUp -> {
                    return MenuTransition(
                        newState = this,
                        effects = listOf(MenuEffect.StopRepeatingEffect)
                    )
                }

                // -----------------------------
                // Confirm (click)
                // -----------------------------
                MenuEvent.Confirm -> {
                    return when (items[selectedIndex]) {
                        AudioItem.ClickSound -> MenuTransition(
                            newState = this,
                            effects = listOf(
                                MenuEffect.UpdateAudioSettings {
                                    it.copy(clickEnabled = !it.clickEnabled)
                                }
                            )
                        )
                        else -> MenuTransition(this)
                    }
                }

                // -----------------------------
                // Navigation
                // -----------------------------
                MenuEvent.MenuShortPress -> {
                    return MenuTransition(
                        newState = Settings(context, selectedIndex = 2),
                        direction = NavDirection.Back
                    )
                }

                MenuEvent.MenuLongPress ->
                    return MenuTransition(Settings(context))

                else -> {}
            }

            return MenuTransition(this)
        }

        sealed class AudioItem {
            object ClickSound : AudioItem()
            object PodcastVolume : AudioItem()
            object WhiteNoiseVolume : AudioItem()
        }

        private fun adjust(
            value: Int,
            delta: Int
        ) = (value + delta).coerceIn(0, 100)
    }
}
