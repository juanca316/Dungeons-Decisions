package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.NarrativeEventType
import com.example.audio.SoundEffect
import com.example.audio.SoundEffectManager
import com.example.data.ChronicleEntity
import com.example.data.ChronicleRepository
import com.example.data.DilemmaOption
import com.example.data.DmTurnPayload
import com.example.data.EthicalArchetype
import com.example.data.GamePaceMode
import com.example.data.HeroGender
import com.example.data.HeroSpecies
import com.example.data.HeroStats
import com.example.data.MoralScores
import com.example.data.PlayerHero
import com.example.data.StoryTurnLog
import com.example.data.TurnOutcomeResolution
import com.example.network.DungeonMasterEngine
import com.example.network.GeminiRetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.random.Random

enum class AdventurePhase {
    WELCOME_PLAYER_COUNT,
    PARTY_FORGE,
    ACTIVE_TABLETOP,
    CAMPAIGN_EPILOGUE
}

enum class MainTab {
    AVENTURA,
    CRONICAS,
    CODICE
}

data class DungeonsUiState(
    val selectedTab: MainTab = MainTab.AVENTURA,
    val phase: AdventurePhase = AdventurePhase.WELCOME_PLAYER_COUNT,
    val playerCount: Int = 3,
    val selectedForgeHeroIndex: Int = 0,
    val selectedPaceMode: GamePaceMode = GamePaceMode.MODO_ESTANDAR,
    val proceduralSeed: Long = System.currentTimeMillis(),
    val players: List<PlayerHero> = defaultPlayersForCount(3),
    val currentTurnNumber: Int = 1,
    val activePlayerIndex: Int = 0,
    val currentTurnPayload: DmTurnPayload? = null,
    val selectedOptionForTurn: DilemmaOption? = null,
    val isDiceLockedForTurn: Boolean = false,
    val pendingTurnResolution: TurnOutcomeResolution? = null,
    val storyLogs: List<StoryTurnLog> = emptyList(),
    val moralScores: MoralScores = MoralScores(),
    val scoresHistory: List<MoralScores> = listOf(MoralScores(50, 50, 50, 50)),
    val currentDiceRoll: Int? = null,
    val manualDiceInput: String = "",
    val isRollingDice: Boolean = false,
    val isGeneratingTurn: Boolean = false,
    val customActionInput: String = "",
    val statusBannerMessage: String? = null,
    val selectedChronicleForModal: ChronicleEntity? = null,
    val isGeneratingLyriaMusic: Boolean = false,
    val currentNarrativeEvent: NarrativeEventType = NarrativeEventType.DILEMA,
    val narrativeTransitionStyle: NarrativeTransitionStyle = NarrativeTransitionStyle.PAGE_CURL
) {
    val activePlayer: PlayerHero
        get() = players.getOrElse(activePlayerIndex) {
            players.firstOrNull() ?: PlayerHero(
                index = 0,
                name = "Aldric",
                gender = HeroGender.HOMBRE,
                species = HeroSpecies.HUMANO,
                archetype = EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD
            )
        }

    /**
     * Paso actual dentro del orden de 5 pasos de toma de decisiones:
     * 1: Presentación del problema
     * 2: Decisión de la solución
     * 3: Tirada única de D20
     * 4: Resultado de la acción según el dado
     * 5: Modificación de stats del personaje
     */
    val currentDecisionStepNumber: Int
        get() = when {
            pendingTurnResolution != null -> 5
            isDiceLockedForTurn -> 4
            selectedOptionForTurn != null -> 3
            else -> 2
        }
}

val NORDIC_EMBLEM_SWATCHES = listOf(
    0xFF7CC6FE, // Plata Escarcha Lunar
    0xFF34D399, // Esmeralda Aurora
    0xFFF4B942, // Ámbar Rúnico
    0xFFA78BFA, // Amatista Boreal
    0xFFF87171, // Carmesí Escarcha
    0xFF38BDF8  // Cian Glaciar
)

fun defaultPlayersForCount(count: Int): List<PlayerHero> {
    val presets = listOf(
        PlayerHero(
            index = 0,
            name = "Elena",
            gender = HeroGender.MUJER,
            species = HeroSpecies.ELFO,
            archetype = EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD,
            emblemColorHex = NORDIC_EMBLEM_SWATCHES[0],
            stats = HeroStats.baseFor(HeroSpecies.ELFO, EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD)
        ),
        PlayerHero(
            index = 1,
            name = "Grom",
            gender = HeroGender.HOMBRE,
            species = HeroSpecies.ORCO,
            archetype = EthicalArchetype.BUSCADOR_DE_VERDAD,
            emblemColorHex = NORDIC_EMBLEM_SWATCHES[4],
            stats = HeroStats.baseFor(HeroSpecies.ORCO, EthicalArchetype.BUSCADOR_DE_VERDAD)
        ),
        PlayerHero(
            index = 2,
            name = "Aldric",
            gender = HeroGender.HOMBRE,
            species = HeroSpecies.HUMANO,
            archetype = EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA,
            emblemColorHex = NORDIC_EMBLEM_SWATCHES[2],
            stats = HeroStats.baseFor(HeroSpecies.HUMANO, EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA)
        ),
        PlayerHero(
            index = 3,
            name = "Brilda",
            gender = HeroGender.MUJER,
            species = HeroSpecies.ENANO,
            archetype = EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD,
            emblemColorHex = NORDIC_EMBLEM_SWATCHES[1],
            stats = HeroStats.baseFor(HeroSpecies.ENANO, EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD)
        ),
        PlayerHero(
            index = 4,
            name = "Kaelen",
            gender = HeroGender.HOMBRE,
            species = HeroSpecies.MEDIO_ELFO,
            archetype = EthicalArchetype.EXPLORADOR_DE_LIBERTAD,
            emblemColorHex = NORDIC_EMBLEM_SWATCHES[3],
            stats = HeroStats.baseFor(HeroSpecies.MEDIO_ELFO, EthicalArchetype.EXPLORADOR_DE_LIBERTAD)
        ),
        PlayerHero(
            index = 5,
            name = "Lyra",
            gender = HeroGender.MUJER,
            species = HeroSpecies.HUMANO,
            archetype = EthicalArchetype.BUSCADOR_DE_VERDAD,
            emblemColorHex = NORDIC_EMBLEM_SWATCHES[5],
            stats = HeroStats.baseFor(HeroSpecies.HUMANO, EthicalArchetype.BUSCADOR_DE_VERDAD)
        )
    )
    return (0 until count.coerceIn(1, 6)).map { idx ->
        presets[idx]
    }
}

class DungeonsViewModel(
    private val repository: ChronicleRepository,
    private val dmEngine: DungeonMasterEngine = DungeonMasterEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DungeonsUiState())
    val uiState: StateFlow<DungeonsUiState> = _uiState.asStateFlow()

    private val _soundEffectEvents = MutableSharedFlow<SoundEffect>(extraBufferCapacity = 8)
    val soundEffectEvents: SharedFlow<SoundEffect> = _soundEffectEvents.asSharedFlow()

    private val _narrativeTurnEvents = MutableSharedFlow<NarrativeEventType>(extraBufferCapacity = 8)
    val narrativeTurnEvents: SharedFlow<NarrativeEventType> = _narrativeTurnEvents.asSharedFlow()

    val savedChronicles: StateFlow<List<ChronicleEntity>> = repository.allChronicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectPlayerCount(count: Int) {
        val validCount = count.coerceIn(1, 6)
        if (_uiState.value.playerCount != validCount) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        }
        _uiState.update { state ->
            val currentList = state.players
            val defaults = defaultPlayersForCount(6)
            val updatedPlayers = (0 until validCount).map { idx ->
                currentList.getOrNull(idx) ?: defaults[idx]
            }
            state.copy(
                playerCount = validCount,
                players = updatedPlayers,
                selectedForgeHeroIndex = state.selectedForgeHeroIndex.coerceIn(0, validCount - 1)
            )
        }
    }

    fun selectForgeHeroTab(index: Int) {
        val maxIdx = (_uiState.value.players.size - 1).coerceAtLeast(0)
        val clamped = index.coerceIn(0, maxIdx)
        if (_uiState.value.selectedForgeHeroIndex != clamped) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        }
        _uiState.update { it.copy(selectedForgeHeroIndex = clamped) }
    }

    fun selectPaceMode(mode: GamePaceMode) {
        _uiState.update { it.copy(selectedPaceMode = mode) }
    }

    fun selectNarrativeTransitionStyle(style: NarrativeTransitionStyle) {
        _uiState.update { it.copy(narrativeTransitionStyle = style) }
    }

    fun proceedToPartyForge(count: Int = _uiState.value.playerCount) {
        selectPlayerCount(count)
        _uiState.update {
            it.copy(
                phase = AdventurePhase.PARTY_FORGE,
                selectedForgeHeroIndex = 0,
                statusBannerMessage = null
            )
        }
    }

    fun quickStartAdventureWithCount(count: Int) {
        selectPlayerCount(count)
        startCampaign()
    }

    fun updatePlayerName(index: Int, newName: String) {
        _uiState.update { state ->
            val mutable = state.players.toMutableList()
            if (index in mutable.indices) {
                mutable[index] = mutable[index].copy(name = newName)
            }
            state.copy(players = mutable)
        }
    }

    fun updatePlayerGender(index: Int, gender: HeroGender) {
        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        _uiState.update { state ->
            val mutable = state.players.toMutableList()
            if (index in mutable.indices) {
                mutable[index] = mutable[index].copy(gender = gender)
            }
            state.copy(players = mutable)
        }
    }

    fun updatePlayerSpecies(index: Int, species: HeroSpecies) {
        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        _uiState.update { state ->
            val mutable = state.players.toMutableList()
            if (index in mutable.indices) {
                val current = mutable[index]
                mutable[index] = current.copy(
                    species = species,
                    stats = HeroStats.baseFor(species, current.archetype)
                )
            }
            state.copy(players = mutable)
        }
    }

    fun updatePlayerArchetype(index: Int, archetype: EthicalArchetype) {
        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        _uiState.update { state ->
            val mutable = state.players.toMutableList()
            if (index in mutable.indices) {
                val current = mutable[index]
                mutable[index] = current.copy(
                    archetype = archetype,
                    stats = HeroStats.baseFor(current.species, archetype)
                )
            }
            state.copy(players = mutable)
        }
    }

    fun updatePlayerEmblemColor(index: Int, colorHex: Long) {
        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        _uiState.update { state ->
            val mutable = state.players.toMutableList()
            if (index in mutable.indices) {
                mutable[index] = mutable[index].copy(emblemColorHex = colorHex)
            }
            state.copy(players = mutable)
        }
    }

    fun randomizeHeroProfile(index: Int) {
        _soundEffectEvents.tryEmit(SoundEffect.DICE_ROLL)
        val nordicNamesMale = listOf("Sigurd", "Alaric", "Torvald", "Kaelen", "Soren", "Bram", "Fenris", "Halvar")
        val nordicNamesFemale = listOf("Freya", "Astrid", "Elowen", "Signe", "Yrsa", "Valkia", "Runa", "Sylvi")
        _uiState.update { state ->
            val mutable = state.players.toMutableList()
            if (index in mutable.indices) {
                val gender = HeroGender.entries.random()
                val species = HeroSpecies.entries.random()
                val archetype = EthicalArchetype.entries.random()
                val emblem = NORDIC_EMBLEM_SWATCHES.random()
                val randomName = if (gender == HeroGender.MUJER) {
                    nordicNamesFemale.random()
                } else {
                    nordicNamesMale.random()
                }
                mutable[index] = mutable[index].copy(
                    name = randomName,
                    gender = gender,
                    species = species,
                    archetype = archetype,
                    emblemColorHex = emblem,
                    stats = HeroStats.baseFor(species, archetype)
                )
            }
            state.copy(players = mutable)
        }
    }

    /**
     * Genera con IA tanto la descripción visual literaria como el avatar (retrato Base64 o emblema)
     * basados en la Especie, Género y Arquetipo Ético elegidos por el jugador.
     */
    fun generateHeroIdentityWithAi(index: Int) {
        val currentHero = _uiState.value.players.getOrNull(index) ?: return
        if (currentHero.isGeneratingIdentity) return

        viewModelScope.launch {
            _uiState.update { state ->
                val list = state.players.toMutableList()
                if (index in list.indices) {
                    list[index] = list[index].copy(isGeneratingIdentity = true)
                }
                state.copy(players = list, statusBannerMessage = null)
            }

            val heroSnapshot = _uiState.value.players.getOrNull(index) ?: return@launch
            val generatedDescription = dmEngine.generateHeroVisualDescription(heroSnapshot)
            val generatedAvatarBase64 = dmEngine.generateHeroAvatarBase64(heroSnapshot)

            _soundEffectEvents.tryEmit(SoundEffect.DILEMMA_RESOLVED)

            _uiState.update { state ->
                val list = state.players.toMutableList()
                if (index in list.indices) {
                    list[index] = list[index].copy(
                        visualDescription = generatedDescription,
                        avatarBase64 = generatedAvatarBase64 ?: list[index].avatarBase64,
                        isGeneratingIdentity = false
                    )
                }
                state.copy(
                    players = list,
                    statusBannerMessage = "✨ Retrato y semblanza visual generados para ${heroSnapshot.fullIdentityPhrase()}."
                )
            }
        }
    }

    /**
     * Genera las descripciones visuales y avatares con IA para todos los héroes del grupo simultáneamente.
     */
    fun generateAllHeroesIdentityWithAi() {
        val indices = _uiState.value.players.indices.toList()
        indices.forEach { idx ->
            generateHeroIdentityWithAi(idx)
        }
    }

    fun startCampaign() {
        val sanitizedPlayers = _uiState.value.players.mapIndexed { idx, hero ->
            val cleanHero = hero.copy(
                name = hero.name.trim().ifEmpty { "Héroe ${idx + 1}" },
                stats = HeroStats.baseFor(hero.species, hero.archetype)
            )
            if (cleanHero.visualDescription.isBlank()) {
                cleanHero.copy(visualDescription = dmEngine.buildProceduralVisualDescription(cleanHero))
            } else {
                cleanHero
            }
        }
        val newSeed = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                players = sanitizedPlayers,
                proceduralSeed = newSeed,
                phase = AdventurePhase.ACTIVE_TABLETOP,
                currentTurnNumber = 1,
                activePlayerIndex = 0,
                selectedOptionForTurn = null,
                isDiceLockedForTurn = false,
                pendingTurnResolution = null,
                storyLogs = emptyList(),
                moralScores = MoralScores(),
                scoresHistory = listOf(MoralScores(50, 50, 50, 50)),
                currentDiceRoll = null,
                manualDiceInput = "",
                isGeneratingTurn = true,
                statusBannerMessage = null
            )
        }

        viewModelScope.launch {
            val state = _uiState.value
            val payload = dmEngine.generateNextTurn(
                paceMode = state.selectedPaceMode,
                proceduralSeed = state.proceduralSeed,
                players = state.players,
                turnNumber = 1,
                activePlayer = state.activePlayer,
                previousLogs = emptyList(),
                lastChosenOption = null,
                lastDiceRoll = null,
                scores = state.moralScores
            )
            val initialEvent = SoundEffectManager.resolveNarrativeEventForRoll(
                diceRoll = null,
                isStoryConcluded = payload.isStoryConcluded
            )
            _uiState.update {
                it.copy(
                    currentTurnPayload = payload,
                    currentNarrativeEvent = initialEvent,
                    isGeneratingTurn = false
                )
            }
            _narrativeTurnEvents.tryEmit(initialEvent)
        }
    }

    /**
     * PASO 2 DEL TURNO: El grupo decide la solución al problema ANTES de tirar el dado.
     * Si el dado aún no se ha lanzado, permite seleccionar o cambiar la solución elegida.
     */
    fun selectOptionForTurn(option: DilemmaOption) {
        val snapshot = _uiState.value
        if (snapshot.isGeneratingTurn) return
        if (snapshot.isDiceLockedForTurn || snapshot.pendingTurnResolution != null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "🔒 La acción y el dado D20 de este turno ya están bloqueados. Revisa el desenlace y pulsa Continuar."
                )
            }
            return
        }

        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
        _uiState.update {
            it.copy(
                selectedOptionForTurn = option,
                statusBannerMessage = null
            )
        }
    }

    /**
     * Compatibilidad con llamadas a chooseDilemmaOption: selecciona la solución en el Paso 2.
     */
    fun chooseDilemmaOption(option: DilemmaOption) {
        selectOptionForTurn(option)
    }

    /**
     * PASO 3 DEL TURNO: Tirada única del dado D20 (bloqueada a 1 solo tiro tras elegir la solución).
     */
    fun rollVirtualD20() {
        val snapshot = _uiState.value
        if (snapshot.isRollingDice || snapshot.isGeneratingTurn) return

        if (snapshot.isDiceLockedForTurn || snapshot.pendingTurnResolution != null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "🔒 Modalidad de tiro único: Ya lanzaste el D20 en este turno (${snapshot.currentDiceRoll}). No se permite volver a tirar."
                )
            }
            return
        }

        val chosenOption = snapshot.selectedOptionForTurn
        if (chosenOption == null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "⚖️ Paso 2 primero: Elige la solución al problema antes de tirar el dado D20."
                )
            }
            return
        }

        _soundEffectEvents.tryEmit(SoundEffect.DICE_ROLL)
        viewModelScope.launch {
            _uiState.update { it.copy(isRollingDice = true, statusBannerMessage = null) }
            repeat(6) {
                _uiState.update { it.copy(currentDiceRoll = Random.nextInt(1, 21)) }
                delay(45)
            }
            val finalRoll = Random.nextInt(1, 21)
            _uiState.update {
                it.copy(
                    currentDiceRoll = finalRoll,
                    manualDiceInput = finalRoll.toString(),
                    isRollingDice = false,
                    isDiceLockedForTurn = true
                )
            }
            resolveActionWithLockedDice(chosenOption, finalRoll)
        }
    }

    /**
     * Registro de número de dado físico (1 a 20). Solo editable antes de bloquear el tiro del turno.
     */
    fun updateManualDiceInput(input: String) {
        val snapshot = _uiState.value
        if (snapshot.isDiceLockedForTurn || snapshot.pendingTurnResolution != null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            return
        }
        val filtered = input.filter { it.isDigit() }.take(2)
        val parsed = filtered.toIntOrNull()
        val validRoll = if (parsed != null && parsed in 1..20) parsed else null
        if (filtered.isNotEmpty() && validRoll == null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
        }
        _uiState.update {
            it.copy(
                manualDiceInput = filtered,
                currentDiceRoll = validRoll,
                statusBannerMessage = null
            )
        }
    }

    /**
     * Confirma y bloquea la tirada física de D20 (1 solo tiro por turno) y ejecuta los Pasos 4 y 5.
     */
    fun confirmPhysicalDiceRoll() {
        val snapshot = _uiState.value
        if (snapshot.isDiceLockedForTurn || snapshot.pendingTurnResolution != null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            return
        }
        val chosenOption = snapshot.selectedOptionForTurn
        if (chosenOption == null) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "⚖️ Paso 2 primero: Selecciona una solución antes de confirmar tu dado D20."
                )
            }
            return
        }
        val roll = snapshot.currentDiceRoll
        if (roll == null || roll !in 1..20) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "🎲 Ingresa un número válido del 1 al 20 para confirmar tu dado físico."
                )
            }
            return
        }

        _soundEffectEvents.tryEmit(SoundEffect.DICE_ROLL)
        _uiState.update { it.copy(isDiceLockedForTurn = true, statusBannerMessage = null) }
        resolveActionWithLockedDice(chosenOption, roll)
    }

    /**
     * PASOS 4 Y 5 DEL TURNO:
     * - Paso 4: Se da el resultado de la acción basándose en el número del dado que sacó.
     * - Paso 5: Se modifican las estadísticas del personaje de la acción y del grupo, mostrando el desenlace.
     */
    private fun resolveActionWithLockedDice(option: DilemmaOption, playerRoll: Int) {
        val snapshot = _uiState.value
        val currentPayload = snapshot.currentTurnPayload ?: return
        val activeHero = snapshot.activePlayer

        val transitionNarrativeEvent = SoundEffectManager.resolveNarrativeEventForRoll(
            diceRoll = playerRoll,
            isStoryConcluded = false
        )
        _narrativeTurnEvents.tryEmit(transitionNarrativeEvent)

        // Actualizar estadísticas grupales escaladas por el D20
        val updatedScores = snapshot.moralScores.applyChoice(option, playerRoll)
        val updatedHistory = snapshot.scoresHistory + updatedScores

        // Actualizar estadísticas individuales del personaje que ejecutó la acción
        val (updatedHero, heroDeltas) = activeHero.applyActionAndDiceRoll(option, playerRoll)
        val updatedPlayers = snapshot.players.toMutableList().apply {
            if (snapshot.activePlayerIndex in indices) {
                this[snapshot.activePlayerIndex] = updatedHero
            }
        }

        val tierLabel = dmEngine.evaluateDiceTierEs(playerRoll)
        val outcomeNarrative = buildActionOutcomeNarrative(
            hero = activeHero,
            option = option,
            diceRoll = playerRoll
        )

        val shortConsequence = when {
            playerRoll == 20 -> "👑 Crítico D20 • ${option.philosophyTag}"
            playerRoll >= 15 -> "🌟 Éxito sólido (D20:$playerRoll) • ${option.philosophyTag}"
            playerRoll >= 10 -> "⚖️ Éxito templado (D20:$playerRoll) • ${option.philosophyTag}"
            playerRoll >= 6 -> "🔥 Éxito con costo (D20:$playerRoll) • ${option.philosophyTag}"
            else -> "⚡ Revés crítico (D20:$playerRoll) • ${option.philosophyTag}"
        }

        val jDelta = updatedScores.justice - snapshot.moralScores.justice
        val cDelta = updatedScores.compassion - snapshot.moralScores.compassion
        val pDelta = updatedScores.pragmatism - snapshot.moralScores.pragmatism
        val lDelta = updatedScores.liberty - snapshot.moralScores.liberty

        val newLog = StoryTurnLog(
            turnNumber = snapshot.currentTurnNumber,
            chapterTitle = currentPayload.chapterTitle,
            dmNarrative = currentPayload.narrativeText,
            ethicalQuestion = currentPayload.ethicalQuestion,
            heroIdentity = activeHero.fullIdentityPhrase(),
            chosenOptionLabel = option.label,
            chosenPhilosophy = option.philosophyTag,
            diceRoll = playerRoll,
            diceOutcomeTier = tierLabel,
            scoresSnapshot = updatedScores,
            justiceDelta = jDelta,
            compassionDelta = cDelta,
            pragmatismDelta = pDelta,
            libertyDelta = lDelta,
            directConsequenceSummary = shortConsequence
        )

        val resolution = TurnOutcomeResolution(
            turnNumber = snapshot.currentTurnNumber,
            heroIndex = activeHero.index,
            heroName = activeHero.name,
            heroIdentity = activeHero.fullIdentityPhrase(),
            chosenOption = option,
            diceRoll = playerRoll,
            diceTierTitleEs = tierLabel,
            actionOutcomeNarrativeEs = outcomeNarrative,
            updatedHeroStats = updatedHero.stats,
            heroStatDeltas = heroDeltas,
            groupJusticeDelta = jDelta,
            groupCompassionDelta = cDelta,
            groupDutyDelta = pDelta,
            groupLibertyDelta = lDelta
        )

        val updatedLogs = snapshot.storyLogs + newLog

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                storyLogs = updatedLogs,
                moralScores = updatedScores,
                scoresHistory = updatedHistory,
                currentNarrativeEvent = transitionNarrativeEvent,
                pendingTurnResolution = resolution,
                statusBannerMessage = null
            )
        }

        viewModelScope.launch {
            delay(160)
            _soundEffectEvents.tryEmit(SoundEffect.EVENT_LOG_UPDATED)
        }
    }

    /**
     * Avanza al siguiente turno después de que el jugador revisa el Paso 4 (Resultado del D20)
     * y el Paso 5 (Modificación de Stats del Personaje).
     */
    fun advanceToNextTurn() {
        val snapshot = _uiState.value
        val resolution = snapshot.pendingTurnResolution ?: return
        if (snapshot.isGeneratingTurn) return

        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)

        val nextTurn = snapshot.currentTurnNumber + 1
        val nextPlayerIdx = (snapshot.activePlayerIndex + 1) % snapshot.players.size

        _uiState.update {
            it.copy(
                currentTurnNumber = nextTurn,
                activePlayerIndex = nextPlayerIdx,
                selectedOptionForTurn = null,
                isDiceLockedForTurn = false,
                pendingTurnResolution = null,
                currentDiceRoll = null,
                manualDiceInput = "",
                isGeneratingTurn = true,
                statusBannerMessage = null
            )
        }

        viewModelScope.launch {
            val latestState = _uiState.value
            val nextPayload = dmEngine.generateNextTurn(
                paceMode = latestState.selectedPaceMode,
                proceduralSeed = latestState.proceduralSeed,
                players = latestState.players,
                turnNumber = nextTurn,
                activePlayer = latestState.activePlayer,
                previousLogs = latestState.storyLogs,
                lastChosenOption = resolution.chosenOption,
                lastDiceRoll = resolution.diceRoll,
                scores = latestState.moralScores
            )
            val arrivalEvent = if (nextPayload.isStoryConcluded) {
                NarrativeEventType.EXITO
            } else {
                latestState.currentNarrativeEvent
            }
            _uiState.update {
                it.copy(
                    currentTurnPayload = nextPayload,
                    currentNarrativeEvent = arrivalEvent,
                    isGeneratingTurn = false
                )
            }
            if (nextPayload.isStoryConcluded) {
                _narrativeTurnEvents.tryEmit(NarrativeEventType.EXITO)
            }
        }
    }

    private fun buildActionOutcomeNarrative(
        hero: PlayerHero,
        option: DilemmaOption,
        diceRoll: Int
    ): String {
        return when {
            diceRoll == 20 ->
                "¡Crítico Legendario (D20 = 20)! ${hero.name} ejecuta «${option.label}» con maestría absoluta. Su determinación inspira a todo el grupo y potencia al máximo los frutos éticos de la acción."
            diceRoll in 15..19 ->
                "Con un D20 de $diceRoll (Éxito Sólido), ${hero.name} lleva a cabo «${option.label}» con firmeza y eficacia, consolidando su postura de ${option.philosophyTag} sin daños colaterales."
            diceRoll in 10..14 ->
                "Con un D20 de $diceRoll (Éxito Templado), ${hero.name} logra aplicar «${option.label}». El problema se resuelve según lo previsto, asumiendo el peso moral natural de la elección."
            diceRoll in 6..9 ->
                "Con un D20 de $diceRoll (Éxito con Costo), ${hero.name} consigue imponer «${option.label}», pero enfrenta resistencia y desgaste personal durante la maniobra."
            else ->
                "Con un D20 de $diceRoll (Revés Dramático), la ejecución de «${option.label}» por parte de ${hero.name} sufre complicaciones imprevistas que ponen a prueba el temple de sus estadísticas."
        }
    }

    fun updateCustomActionInput(text: String) {
        _uiState.update { it.copy(customActionInput = text) }
    }

    fun submitCustomAction() {
        val text = _uiState.value.customActionInput.trim()
        if (text.isEmpty()) {
            _soundEffectEvents.tryEmit(SoundEffect.DECISION_ERROR)
            return
        }

        val activeHero = _uiState.value.activePlayer
        val customOption = DilemmaOption(
            id = "custom_${System.currentTimeMillis()}",
            label = text,
            description = "Decisión libre propuesta por ${activeHero.fullIdentityPhrase()}.",
            philosophyTag = activeHero.archetype.titleEs,
            justiceDelta = 6,
            compassionDelta = 6,
            pragmatismDelta = 6,
            libertyDelta = 10
        )
        _uiState.update { it.copy(customActionInput = "") }
        selectOptionForTurn(customOption)
    }

    fun triggerEventLogSound() {
        _soundEffectEvents.tryEmit(SoundEffect.EVENT_LOG_UPDATED)
    }

    fun triggerDecisionButtonSound() {
        _soundEffectEvents.tryEmit(SoundEffect.DECISION_BUTTON_PRESS)
    }

    fun triggerProblemCardRevealedSound() {
        _soundEffectEvents.tryEmit(SoundEffect.PROBLEM_CARD_REVEALED)
    }

    fun triggerTutorialCompletedSound() {
        _soundEffectEvents.tryEmit(SoundEffect.TUTORIAL_COMPLETED)
    }

    fun concludeAndSaveChronicle() {
        val state = _uiState.value
        _soundEffectEvents.tryEmit(SoundEffect.DILEMMA_RESOLVED)
        viewModelScope.launch {
            val partySummary = state.players.joinToString(" • ") { it.fullIdentityPhrase() }
            val transcriptJson = try {
                GeminiRetrofitClient.json.encodeToString(state.storyLogs)
            } catch (_: Exception) {
                "[]"
            }
            val lastSnippet = state.currentTurnPayload?.narrativeText?.take(180)
                ?: state.storyLogs.lastOrNull()?.let {
                    "${it.chapterTitle}: ${it.heroIdentity} eligió «${it.chosenOptionLabel}»"
                }.orEmpty()

            val chronicleTitle = state.currentTurnPayload?.chapterTitle?.takeIf { it.isNotBlank() }
                ?: "Aventura Procedimental (${state.selectedPaceMode.titleEs})"

            val entity = ChronicleEntity(
                realmTitle = "$chronicleTitle • ${state.selectedPaceMode.titleEs}",
                playerCount = state.playerCount,
                partySummary = partySummary,
                dominantAlignment = state.moralScores.dominantAlignmentEs(),
                turnsCompleted = state.storyLogs.size.coerceAtLeast(1),
                justiceScore = state.moralScores.justice,
                compassionScore = state.moralScores.compassion,
                pragmatismScore = state.moralScores.pragmatism,
                libertyScore = state.moralScores.liberty,
                lastNarrativeSnippet = lastSnippet,
                fullTranscriptJson = transcriptJson
            )
            repository.saveChronicle(entity)
            _uiState.update {
                it.copy(
                    phase = AdventurePhase.CAMPAIGN_EPILOGUE,
                    statusBannerMessage = "¡Crónica ética de CORE I guardada en los Archivos!"
                )
            }
        }
    }

    fun resetToWelcome() {
        _uiState.update {
            it.copy(
                phase = AdventurePhase.WELCOME_PLAYER_COUNT,
                currentTurnPayload = null,
                selectedOptionForTurn = null,
                isDiceLockedForTurn = false,
                pendingTurnResolution = null,
                storyLogs = emptyList(),
                moralScores = MoralScores(),
                scoresHistory = listOf(MoralScores(50, 50, 50, 50)),
                currentDiceRoll = null,
                manualDiceInput = "",
                statusBannerMessage = null
            )
        }
    }

    fun navigateBackInAdventure(): Boolean {
        val currentPhase = _uiState.value.phase
        return when (currentPhase) {
            AdventurePhase.PARTY_FORGE -> {
                _uiState.update { it.copy(phase = AdventurePhase.WELCOME_PLAYER_COUNT) }
                true
            }
            AdventurePhase.ACTIVE_TABLETOP -> {
                _uiState.update { it.copy(phase = AdventurePhase.PARTY_FORGE) }
                true
            }
            AdventurePhase.CAMPAIGN_EPILOGUE -> {
                _uiState.update { it.copy(phase = AdventurePhase.ACTIVE_TABLETOP) }
                true
            }
            AdventurePhase.WELCOME_PLAYER_COUNT -> false
        }
    }

    fun openChronicleModal(chronicle: ChronicleEntity?) {
        _uiState.update { it.copy(selectedChronicleForModal = chronicle) }
    }

    fun deleteChronicle(id: Long) {
        viewModelScope.launch {
            repository.deleteChronicle(id)
            if (_uiState.value.selectedChronicleForModal?.id == id) {
                _uiState.update { it.copy(selectedChronicleForModal = null) }
            }
        }
    }

    fun composeMusicWithLyria(
        prompt: String,
        useFullLengthPro: Boolean,
        onAudioReady: (String) -> Unit
    ) {
        if (_uiState.value.isGeneratingLyriaMusic) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingLyriaMusic = true,
                    statusBannerMessage = "🎵 Componiendo pista ambiental con Lyria (${if (useFullLengthPro) "lyria-3-pro-preview" else "lyria-3-clip-preview"})..."
                )
            }
            val base64Audio = dmEngine.generateLyriaMusicClip(
                prompt = prompt,
                useFullLengthPro = useFullLengthPro
            )
            if (!base64Audio.isNullOrBlank()) {
                onAudioReady(base64Audio)
                _uiState.update {
                    it.copy(
                        isGeneratingLyriaMusic = false,
                        statusBannerMessage = "🎶 ¡Nueva pista generada con Lyria activada en el reproductor!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isGeneratingLyriaMusic = false,
                        statusBannerMessage = "🎵 Reproduciendo pista acústica local de res/raw/ (Configura GEMINI_API_KEY para síntesis Lyria en vivo)."
                    )
                }
            }
        }
    }

    class Factory(private val repository: ChronicleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DungeonsViewModel(repository) as T
        }
    }
}
