package com.example.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.audio.AudioManager
import com.example.audio.BgmTrack
import com.example.audio.NarrativeEventType
import com.example.audio.SoundEffect
import com.example.audio.SoundEffectManager
import com.example.ui.theme.ArcaneSurface
import com.example.ui.theme.ArcaneSurfaceVariant
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.TorchAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DungeonsMainScreen(
    viewModel: DungeonsViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedChronicles by viewModel.savedChronicles.collectAsStateWithLifecycle()

    val audioManager = remember { AudioManager(context) }
    val soundEffectManager = remember { SoundEffectManager(context) }
    val isMuted by audioManager.isMuted.collectAsStateWithLifecycle()
    val currentTrack by audioManager.currentTrack.collectAsStateWithLifecycle()
    val lastPlayedSfx by soundEffectManager.lastPlayedSoundEffect.collectAsStateWithLifecycle()
    val sfxVolume by soundEffectManager.sfxVolume.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSoundtrackDialog by remember { mutableStateOf(false) }

    val tutorialPrefs = remember(context) {
        context.getSharedPreferences("dungeons_decisions_tutorial_prefs", Context.MODE_PRIVATE)
    }
    var showTutorialPromptDialog by remember {
        mutableStateOf(!tutorialPrefs.getBoolean("has_answered_tutorial_prompt", false))
    }
    var showTutorialOverlay by remember { mutableStateOf(false) }

    // Escuchar eventos de efectos de sonido (botón de decisión, log de eventos, tirada de dados, etc.)
    LaunchedEffect(viewModel, soundEffectManager) {
        viewModel.soundEffectEvents.collect { effect ->
            soundEffectManager.playSound(effect)
        }
    }

    // Escuchar eventos narrativos de transición de turnos (Dilema, Éxito, Fallo Crítico)
    LaunchedEffect(viewModel, soundEffectManager) {
        viewModel.narrativeTurnEvents.collect { narrativeEvent ->
            soundEffectManager.playNarrativeEvent(narrativeEvent)
        }
    }

    // Selección dinámica de atmósfera musical según la pantalla, fase, turno, tirada D20 y desenlace:
    // - Menú Inicial (WELCOME_PLAYER_COUNT) -> Barroco Clásico (bgm_menu)
    // - Forja de Héroes (PARTY_FORGE) -> Balada Medieval de Laúd (bgm_medieval_tavern)
    // - Mesa Activa (Exploración / Éxito 15-20) -> Fantasía Mística (bgm_adventure)
    // - Mesa Activa (Deliberación / Costo Ético 6-14) -> Suspenso Filosófico (bgm_suspense_dilemma)
    // - Mesa Activa (Infortunio D20 1-5) -> Tensión Dramática (bgm_tension)
    // - Códice Ético / Archivos de Crónicas -> Fuga Barroca Académica (bgm_baroque_codex)
    // - Epílogo -> Fanfarria Heroica (bgm_epilogue)
    val lastLoggedRoll = uiState.storyLogs.lastOrNull()?.diceRoll
    val activeDiceForMood = uiState.currentDiceRoll ?: lastLoggedRoll
    val isConcluded = uiState.currentTurnPayload?.isStoryConcluded == true

    DisposableEffect(
        uiState.selectedTab,
        uiState.phase,
        uiState.currentTurnNumber,
        activeDiceForMood,
        isConcluded
    ) {
        val targetTrack = BgmTrack.resolveDynamicTrack(
            selectedTab = uiState.selectedTab,
            phase = uiState.phase,
            turnNumber = uiState.currentTurnNumber,
            lastDiceRoll = activeDiceForMood,
            isStoryConcluded = isConcluded
        )
        audioManager.playTrack(targetTrack)
        onDispose {
            // Transición limpia entre variaciones musicales
        }
    }

    // Gestión de Ciclo de Vida: pausar cuando la app se minimiza y liberar recursos en onDispose
    DisposableEffect(lifecycleOwner, audioManager, soundEffectManager) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> audioManager.pause()
                Lifecycle.Event.ON_RESUME -> audioManager.resume()
                Lifecycle.Event.ON_DESTROY -> {
                    audioManager.release()
                    soundEffectManager.release()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            audioManager.release()
            soundEffectManager.release()
        }
    }

    LaunchedEffect(uiState.statusBannerMessage) {
        uiState.statusBannerMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BackHandler(
        enabled = !showTutorialOverlay && (
            uiState.selectedTab != MainTab.AVENTURA ||
                uiState.phase != AdventurePhase.WELCOME_PLAYER_COUNT
            )
    ) {
        if (uiState.selectedTab != MainTab.AVENTURA) {
            viewModel.selectTab(MainTab.AVENTURA)
        } else {
            viewModel.navigateBackInAdventure()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showSoundtrackDialog = true }
                    ) {
                        Text(
                            text = "⚔️ DUNGEONS & DECISIONS",
                            style = MaterialTheme.typography.titleLarge,
                            color = ImperialGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("top_app_bar_title")
                        )
                        currentTrack?.let { track ->
                            val sfxSuffix = if (!isMuted && lastPlayedSfx != null) {
                                " • ${lastPlayedSfx!!.emoji} ${lastPlayedSfx!!.titleEs}"
                            } else {
                                ""
                            }
                            Text(
                                text = if (isMuted) "🔇 Audio Silenciado" else "${track.genreTagEs}$sfxSuffix",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isMuted) ParchmentMuted else ImperialGoldLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Botón de ayuda siempre visible para abrir el Tutorial Interactivo
                    IconButton(
                        onClick = {
                            tutorialPrefs.edit().putBoolean("has_answered_tutorial_prompt", true).apply()
                            showTutorialPromptDialog = false
                            showTutorialOverlay = true
                            soundEffectManager.playSound(SoundEffect.DECISION_BUTTON_PRESS)
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ArcaneSurfaceVariant.copy(alpha = 0.85f))
                            .testTag("top_bar_tutorial_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Tutorial interactivo y reglas del juego",
                            tint = ImperialGoldLight
                        )
                    }

                    // Botón de selector de estilo musical (Barroco, Medieval, Fantasía, Suspenso, Tensión) y Lyria
                    IconButton(
                        onClick = { showSoundtrackDialog = true },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ArcaneSurfaceVariant.copy(alpha = 0.85f))
                            .testTag("soundtrack_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Estilos musicales y generador Lyria",
                            tint = ImperialGoldLight
                        )
                    }

                    // Botón flotante en la barra superior con Icons.Default.VolumeUp y Icons.Default.VolumeOff
                    IconButton(
                        onClick = {
                            val muted = audioManager.toggleMute()
                            soundEffectManager.setMuted(muted)
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMuted) ArcaneSurfaceVariant.copy(alpha = 0.6f)
                                else ImperialGold.copy(alpha = 0.22f)
                            )
                            .testTag("audio_mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = if (isMuted) {
                                "Activar música ambiental"
                            } else {
                                "Silenciar música ambiental"
                            },
                            tint = if (isMuted) ParchmentMuted else ImperialGold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ObsidianBackground.copy(alpha = 0.95f),
                    titleContentColor = ImperialGold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ArcaneSurface,
                contentColor = ParchmentLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.AVENTURA,
                    onClick = { viewModel.selectTab(MainTab.AVENTURA) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == MainTab.AVENTURA) {
                                Icons.Filled.Explore
                            } else {
                                Icons.Outlined.Explore
                            },
                            contentDescription = stringResource(R.string.adventure_tab)
                        )
                    },
                    label = { Text(stringResource(R.string.adventure_tab)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = ImperialGoldLight,
                        indicatorColor = ImperialGold,
                        unselectedIconColor = ParchmentMuted,
                        unselectedTextColor = ParchmentMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_adventure")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.CRONICAS,
                    onClick = { viewModel.selectTab(MainTab.CRONICAS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (savedChronicles.isNotEmpty()) {
                                    Badge(
                                        containerColor = ImperialGold,
                                        contentColor = ObsidianBackground
                                    ) {
                                        Text(savedChronicles.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.selectedTab == MainTab.CRONICAS) {
                                    Icons.Filled.AutoStories
                                } else {
                                    Icons.Outlined.AutoStories
                                },
                                contentDescription = stringResource(R.string.chronicles_tab)
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.chronicles_tab)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = ImperialGoldLight,
                        indicatorColor = ImperialGold,
                        unselectedIconColor = ParchmentMuted,
                        unselectedTextColor = ParchmentMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_chronicles")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == MainTab.CODICE,
                    onClick = { viewModel.selectTab(MainTab.CODICE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == MainTab.CODICE) {
                                Icons.AutoMirrored.Filled.MenuBook
                            } else {
                                Icons.AutoMirrored.Outlined.MenuBook
                            },
                            contentDescription = stringResource(R.string.codex_tab)
                        )
                    },
                    label = { Text(stringResource(R.string.codex_tab)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = ImperialGoldLight,
                        indicatorColor = ImperialGold,
                        unselectedIconColor = ParchmentMuted,
                        unselectedTextColor = ParchmentMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_codex")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBackground,
                            ArcaneSurface.copy(alpha = 0.85f),
                            ArcaneSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                MainTab.AVENTURA -> {
                    when (uiState.phase) {
                        AdventurePhase.WELCOME_PLAYER_COUNT -> {
                            WelcomePlayerCountScreen(
                                selectedCount = uiState.playerCount,
                                selectedPaceMode = uiState.selectedPaceMode,
                                onSelectCount = viewModel::selectPlayerCount,
                                onSelectPaceMode = viewModel::selectPaceMode,
                                onContinueToPartySetup = viewModel::proceedToPartyForge,
                                onQuickStartAdventure = viewModel::quickStartAdventureWithCount,
                                onOpenTutorial = {
                                    tutorialPrefs.edit().putBoolean("has_answered_tutorial_prompt", true).apply()
                                    showTutorialPromptDialog = false
                                    showTutorialOverlay = true
                                    soundEffectManager.playSound(SoundEffect.DECISION_BUTTON_PRESS)
                                }
                            )
                        }
                        AdventurePhase.PARTY_FORGE -> {
                            PartyForgeScreen(
                                players = uiState.players,
                                selectedPaceMode = uiState.selectedPaceMode,
                                selectedHeroTabIndex = uiState.selectedForgeHeroIndex,
                                onSelectHeroTab = viewModel::selectForgeHeroTab,
                                onUpdatePlayerName = viewModel::updatePlayerName,
                                onUpdatePlayerGender = viewModel::updatePlayerGender,
                                onUpdatePlayerSpecies = viewModel::updatePlayerSpecies,
                                onUpdatePlayerArchetype = viewModel::updatePlayerArchetype,
                                onUpdatePlayerEmblemColor = viewModel::updatePlayerEmblemColor,
                                onRandomizeHeroProfile = viewModel::randomizeHeroProfile,
                                onGenerateHeroIdentityAi = viewModel::generateHeroIdentityWithAi,
                                onGenerateAllHeroesAi = viewModel::generateAllHeroesIdentityWithAi,
                                onBackToCount = { viewModel.resetToWelcome() },
                                onLaunchCampaign = viewModel::startCampaign
                            )
                        }
                        AdventurePhase.ACTIVE_TABLETOP -> {
                            ActiveTabletopScreen(
                                uiState = uiState,
                                onSelectTransitionStyle = viewModel::selectNarrativeTransitionStyle,
                                onRollVirtualD20 = viewModel::rollVirtualD20,
                                onManualDiceChange = viewModel::updateManualDiceInput,
                                onConfirmPhysicalDiceRoll = viewModel::confirmPhysicalDiceRoll,
                                onChooseOption = viewModel::selectOptionForTurn,
                                onAdvanceToNextTurn = viewModel::advanceToNextTurn,
                                onCustomActionChange = viewModel::updateCustomActionInput,
                                onSubmitCustomAction = viewModel::submitCustomAction,
                                onConcludeAndSave = viewModel::concludeAndSaveChronicle,
                                onNewAdventure = viewModel::resetToWelcome,
                                onTriggerEventLogSound = viewModel::triggerEventLogSound,
                                onTriggerProblemCardSound = { step ->
                                    soundEffectManager.playProblemCardRevealed(step)
                                }
                            )
                        }
                        AdventurePhase.CAMPAIGN_EPILOGUE -> {
                            CampaignEpilogueScreen(
                                uiState = uiState,
                                onStartNewGame = viewModel::resetToWelcome,
                                onViewChroniclesTab = { viewModel.selectTab(MainTab.CRONICAS) }
                            )
                        }
                    }
                }
                MainTab.CRONICAS -> {
                    SavedChroniclesScreen(
                        chronicles = savedChronicles,
                        selectedChronicle = uiState.selectedChronicleForModal,
                        onOpenChronicle = { viewModel.openChronicleModal(it) },
                        onDismissModal = { viewModel.openChronicleModal(null) },
                        onDeleteChronicle = viewModel::deleteChronicle,
                        onGoToAdventure = { viewModel.selectTab(MainTab.AVENTURA) }
                    )
                }
                MainTab.CODICE -> {
                    EthicalCodexScreen()
                }
            }
        }
    }

    if (showSoundtrackDialog) {
        SoundtrackAndLyriaModal(
            currentTrack = currentTrack,
            isMuted = isMuted,
            sfxVolume = sfxVolume,
            isGeneratingLyria = uiState.isGeneratingLyriaMusic,
            onSelectTrack = { track -> audioManager.playTrack(track) },
            onPlaySoundEffect = { sfx -> soundEffectManager.playSound(sfx) },
            onPlayNarrativeEvent = { eventType -> soundEffectManager.playNarrativeEvent(eventType) },
            onChangeSfxVolume = { vol -> soundEffectManager.setVolume(vol) },
            onToggleMute = {
                val muted = audioManager.toggleMute()
                soundEffectManager.setMuted(muted)
            },
            onGenerateLyriaTrack = { track, prompt, useFullPro ->
                viewModel.composeMusicWithLyria(
                    prompt = prompt,
                    useFullLengthPro = useFullPro,
                    onAudioReady = { base64Audio ->
                        audioManager.updateTrackWithGeneratedAudio(track, base64Audio)
                    }
                )
            },
            onDismiss = { showSoundtrackDialog = false }
        )
    }

    if (showTutorialPromptDialog && !showTutorialOverlay) {
        TutorialPromptDialog(
            onAcceptTutorial = {
                tutorialPrefs.edit().putBoolean("has_answered_tutorial_prompt", true).apply()
                showTutorialPromptDialog = false
                showTutorialOverlay = true
                soundEffectManager.playSound(SoundEffect.DECISION_BUTTON_PRESS)
            },
            onDeclineTutorial = {
                tutorialPrefs.edit().putBoolean("has_answered_tutorial_prompt", true).apply()
                showTutorialPromptDialog = false
            }
        )
    }

    if (showTutorialOverlay) {
        InteractiveGameTutorialOverlay(
            onCloseTutorial = { showTutorialOverlay = false },
            onPlaySoundEffect = { sfx -> soundEffectManager.playSound(sfx) },
            onPlayCardRevealSound = { step -> soundEffectManager.playProblemCardRevealed(step) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SoundtrackAndLyriaModal(
    currentTrack: BgmTrack?,
    isMuted: Boolean,
    sfxVolume: Float,
    isGeneratingLyria: Boolean,
    onSelectTrack: (BgmTrack) -> Unit,
    onPlaySoundEffect: (SoundEffect) -> Unit,
    onPlayNarrativeEvent: (NarrativeEventType) -> Unit,
    onChangeSfxVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onGenerateLyriaTrack: (BgmTrack, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTrackForLyria by remember {
        mutableStateOf(currentTrack ?: BgmTrack.BGM_MENU)
    }
    var customMusicPrompt by remember {
        mutableStateOf((currentTrack ?: BgmTrack.BGM_MENU).defaultLyriaPrompt)
    }
    var useLyriaPro by remember { mutableStateOf(false) }

    val stylePresets = listOf(
        "🎻 Barroco" to "Baroque chamber music with harpsichord and cello counterpoint in D minor",
        "🪕 Medieval" to "Medieval tavern music with lute, celtic harp and wooden recorder in Dorian mode",
        "✨ Fantasía" to "High fantasy orchestral exploration music with celestial harp and lush strings",
        "🕯️ Suspenso" to "Mysterious suspense soundtrack with tremolo strings and philosophical tension",
        "⚡ Tensión" to "Urgent dark fantasy crisis music with dramatic minor strings and deep percussion"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArcaneSurface,
        titleContentColor = ImperialGold,
        textContentColor = ParchmentLight,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ImperialGold
                )
                Text(
                    text = "Variaciones Musicales y Lyria Studio",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "7 Variaciones por Evento (Barroca, Medieval, Fantasía, Suspenso):",
                    style = MaterialTheme.typography.labelLarge,
                    color = ImperialGoldLight
                )

                BgmTrack.entries.forEach { track ->
                    val isPlayingThis = currentTrack == track
                    Surface(
                        onClick = {
                            selectedTrackForLyria = track
                            customMusicPrompt = track.defaultLyriaPrompt
                            onSelectTrack(track)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPlayingThis) ImperialGold.copy(alpha = 0.22f) else ObsidianBackground,
                        border = BorderStroke(
                            width = if (isPlayingThis) 1.5.dp else 1.dp,
                            color = if (isPlayingThis) ImperialGold else ParchmentBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("track_option_${track.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${if (isPlayingThis) "🔊 " else "🎵 "}${track.genreTagEs} — ${track.titleEs}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isPlayingThis) ImperialGoldLight else ParchmentLight
                            )
                            Text(
                                text = track.descriptionEs,
                                style = MaterialTheme.typography.labelMedium,
                                color = ParchmentMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Eventos Narrativos de Transición (Dilema, Éxito, Fallo Crítico):",
                    style = MaterialTheme.typography.labelLarge,
                    color = ImperialGoldLight
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onPlayNarrativeEvent(NarrativeEventType.DILEMA) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("narrative_sfx_dilemma")
                    ) {
                        Text("⚖️ Dilema", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                    OutlinedButton(
                        onClick = { onPlayNarrativeEvent(NarrativeEventType.EXITO) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("narrative_sfx_success")
                    ) {
                        Text("🌟 Éxito", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                    OutlinedButton(
                        onClick = { onPlayNarrativeEvent(NarrativeEventType.FALLO_CRITICO) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("narrative_sfx_critical_failure")
                    ) {
                        Text("⚡ Fallo", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Efectos de Sonido (SFX — SoundPool Android):",
                    style = MaterialTheme.typography.labelLarge,
                    color = ImperialGoldLight
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Volumen SFX (${(sfxVolume * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ParchmentMuted
                    )
                    Slider(
                        value = sfxVolume,
                        onValueChange = onChangeSfxVolume,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = ImperialGold,
                            activeTrackColor = ImperialGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_volume_slider")
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.PROBLEM_CARD_REVEALED) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_problem_card_reveal")
                    ) {
                        Text("🃏 Tarjeta", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.TUTORIAL_COMPLETED) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_tutorial_completed")
                    ) {
                        Text("🎓 Fin Tutorial", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_decision_button")
                    ) {
                        Text("⚔️ Decisión", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.EVENT_LOG_UPDATED) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_event_log_updated")
                    ) {
                        Text("📜 Log Eventos", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.DICE_ROLL) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_dice_roll")
                    ) {
                        Text("🎲 Dados", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.DILEMMA_RESOLVED) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_dilemma_resolved")
                    ) {
                        Text("✨ Resuelto", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                    OutlinedButton(
                        onClick = { onPlaySoundEffect(SoundEffect.DECISION_ERROR) },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sfx_preview_decision_error")
                    ) {
                        Text("⚠️ Error", style = MaterialTheme.typography.labelMedium, color = ImperialGoldLight)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Componer Variación con IA Lyria (${selectedTrackForLyria.genreTagEs}):",
                    style = MaterialTheme.typography.labelLarge,
                    color = TorchAmber
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stylePresets.forEach { (label, presetPrompt) ->
                        FilterChip(
                            selected = customMusicPrompt == presetPrompt,
                            onClick = { customMusicPrompt = presetPrompt },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ImperialGold.copy(alpha = 0.25f),
                                selectedLabelColor = ImperialGoldLight
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !useLyriaPro,
                        onClick = { useLyriaPro = false },
                        label = { Text("Clip 30s (lyria-3-clip-preview)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ImperialGold.copy(alpha = 0.25f),
                            selectedLabelColor = ImperialGoldLight
                        )
                    )
                    FilterChip(
                        selected = useLyriaPro,
                        onClick = { useLyriaPro = true },
                        label = { Text("Pro (lyria-3-pro-preview)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ImperialGold.copy(alpha = 0.25f),
                            selectedLabelColor = ImperialGoldLight
                        )
                    )
                }

                OutlinedTextField(
                    value = customMusicPrompt,
                    onValueChange = { customMusicPrompt = it },
                    label = { Text("Estilo musical para Lyria") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImperialGold,
                        unfocusedBorderColor = ParchmentBorder,
                        focusedTextColor = ParchmentLight,
                        unfocusedTextColor = ParchmentLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onGenerateLyriaTrack(
                            selectedTrackForLyria,
                            customMusicPrompt,
                            useLyriaPro
                        )
                    },
                    enabled = !isGeneratingLyria && customMusicPrompt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color(0xFF1F1300)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingLyria) {
                        CircularProgressIndicator(
                            color = Color(0xFF1F1300),
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generando variación...")
                    } else {
                        Text("Sintetizar Variación con Lyria")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ImperialGold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onToggleMute,
                border = BorderStroke(1.dp, ImperialGold)
            ) {
                Text(
                    text = if (isMuted) "Activar Audio" else "Silenciar",
                    color = ImperialGoldLight
                )
            }
        }
    )
}
