package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.example.R
import com.example.audio.NarrativeEventType
import com.example.data.ChronicleEntity
import com.example.data.DilemmaOption
import com.example.data.EthicalArchetype
import com.example.data.HeroSpecies
import com.example.data.MoralScores
import com.example.data.StoryTurnLog
import com.example.data.TurnOutcomeResolution
import com.example.data.VoterDilemmaComplexity
import com.example.network.GeminiRetrofitClient
import com.example.ui.theme.AmethystWisdom
import com.example.ui.theme.ArcaneCardElevated
import com.example.ui.theme.ArcaneSurface
import com.example.ui.theme.ArcaneSurfaceVariant
import com.example.ui.theme.CrimsonChaos
import com.example.ui.theme.EmeraldVirtue
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.SapphireOrder
import com.example.ui.theme.TorchAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveTabletopScreen(
    uiState: DungeonsUiState,
    onSelectTransitionStyle: (NarrativeTransitionStyle) -> Unit = {},
    onRollVirtualD20: () -> Unit,
    onManualDiceChange: (String) -> Unit,
    onConfirmPhysicalDiceRoll: () -> Unit = {},
    onChooseOption: (DilemmaOption) -> Unit,
    onAdvanceToNextTurn: () -> Unit = {},
    onCustomActionChange: (String) -> Unit,
    onSubmitCustomAction: () -> Unit,
    onConcludeAndSave: () -> Unit,
    onNewAdventure: () -> Unit,
    onTriggerEventLogSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val payload = uiState.currentTurnPayload
    val activeHero = uiState.activePlayer
    val resolution = uiState.pendingTurnResolution

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_tabletop_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Barra Superior Compacta: Héroe Activo + Stats del Personaje + Iconos Grupales con Flash
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeHeroDelta = activeHero.archetype.statDeltaFromHistory(
                        current = uiState.moralScores,
                        history = uiState.scoresHistory
                    )
                    val activeHeroFlash = rememberStatFlashState(
                        score = uiState.moralScores.justice + uiState.moralScores.compassion + uiState.moralScores.pragmatism + uiState.moralScores.liberty,
                        historyDelta = activeHeroDelta,
                        turnTriggerKey = uiState.scoresHistory.size
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = 1f + (0.12f * activeHeroFlash.flashAlpha)
                                    scaleY = 1f + (0.12f * activeHeroFlash.flashAlpha)
                                }
                            ) {
                                HeroAvatarPortrait(
                                    hero = activeHero,
                                    sizeDp = 44,
                                    modifier = Modifier.testTag("active_hero_tabletop_avatar")
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                scaleX = activeHeroFlash.scale
                                                scaleY = activeHeroFlash.scale
                                            }
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (activeHeroFlash.flashAlpha > 0.04f) {
                                                    ImperialGold.copy(alpha = 0.45f * activeHeroFlash.flashAlpha)
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = "${activeHero.species.emoji}${activeHero.archetype.emoji}",
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(
                                        text = activeHero.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ImperialGoldLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "Turno #${uiState.currentTurnNumber} • Héroe de la acción",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TorchAmber,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(
                                onClick = onConcludeAndSave,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("finish_and_save_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdded,
                                    contentDescription = null,
                                    tint = ImperialGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardar",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ImperialGold
                                )
                            }
                            IconButton(
                                onClick = onNewAdventure,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("reset_adventure_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reiniciar partida",
                                    tint = ParchmentMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Barra de Stats Individuales del Personaje de la Acción (con deltas si acaba de resolver)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_hero_personal_stats_row"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val hDeltas = resolution?.heroStatDeltas
                        HeroStatMiniPill(
                            emoji = "⚖️",
                            label = "Honor",
                            value = activeHero.stats.honor,
                            color = SapphireOrder,
                            delta = hDeltas?.honorDelta ?: 0,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatMiniPill(
                            emoji = "💚",
                            label = "Empatía",
                            value = activeHero.stats.empathy,
                            color = EmeraldVirtue,
                            delta = hDeltas?.empathyDelta ?: 0,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatMiniPill(
                            emoji = "🛡️",
                            label = "Disciplina",
                            value = activeHero.stats.discipline,
                            color = AmethystWisdom,
                            delta = hDeltas?.disciplineDelta ?: 0,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatMiniPill(
                            emoji = "🦅",
                            label = "Audacia",
                            value = activeHero.stats.audacity,
                            color = TorchAmber,
                            delta = hDeltas?.audacityDelta ?: 0,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Barra de Iconos de Estado Grupal con animación de Flash ante cambios significativos
                    CharacterStatusIconsBar(
                        scores = uiState.moralScores,
                        history = uiState.scoresHistory
                    )

                    if (uiState.players.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uiState.players, key = { it.index }) { hero ->
                                val isActive = hero.index == uiState.activePlayerIndex
                                val heroDelta = hero.archetype.statDeltaFromHistory(
                                    current = uiState.moralScores,
                                    history = uiState.scoresHistory
                                )
                                val heroFlash = rememberStatFlashState(
                                    score = heroDelta,
                                    historyDelta = heroDelta,
                                    turnTriggerKey = uiState.scoresHistory.size
                                )
                                val flashHighlight = heroFlash.flashAlpha > 0.04f

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = when {
                                        flashHighlight -> ImperialGold.copy(alpha = (0.28f + 0.35f * heroFlash.flashAlpha).coerceAtMost(0.7f))
                                        isActive -> ImperialGold.copy(alpha = 0.25f)
                                        else -> ObsidianBackground.copy(alpha = 0.6f)
                                    },
                                    border = BorderStroke(
                                        width = if (flashHighlight || isActive) 1.5.dp else 1.dp,
                                        color = when {
                                            flashHighlight -> ImperialGoldLight
                                            isActive -> ImperialGold
                                            else -> ParchmentBorder
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = heroFlash.scale
                                                scaleY = heroFlash.scale
                                            }
                                        ) {
                                            Text(
                                                text = "${hero.species.emoji}${hero.archetype.emoji}",
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(
                                            text = hero.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isActive || flashHighlight) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isActive || flashHighlight) ImperialGoldLight else ParchmentMuted
                                        )
                                        if (abs(heroDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD) {
                                            val sign = if (heroDelta > 0) "+$heroDelta" else "$heroDelta"
                                            Text(
                                                text = sign,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (heroDelta > 0) EmeraldVirtue else CrimsonChaos
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Indicador Visual del Orden de Toma de Decisiones (Pasos 1 → 2 → 3 → 4 → 5)
        item {
            TurnFiveStepOrderBanner(
                currentStep = uiState.currentDecisionStepNumber,
                hasSelectedOption = uiState.selectedOptionForTurn != null,
                isDiceLocked = uiState.isDiceLockedForTurn,
                hasResolution = resolution != null,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        // 3. Selector Compacto de Transición
        item {
            NarrativeTransitionStyleSelector(
                selectedStyle = uiState.narrativeTransitionStyle,
                currentTurnNumber = uiState.currentTurnNumber,
                onSelectStyle = onSelectTransitionStyle,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        // 4. PASO 1: SE PRESENTA EL PROBLEMA
        item {
            val bookPageState = NarrativeBookPageState(
                turnNumber = uiState.currentTurnNumber,
                isGeneratingTurn = uiState.isGeneratingTurn,
                payload = payload,
                activeHeroIdentity = activeHero.fullIdentityPhrase()
            )

            RoleplayBookPageTransitionContainer(
                pageState = bookPageState,
                transitionStyle = uiState.narrativeTransitionStyle,
                modifier = Modifier.widthIn(max = 640.dp)
            ) { animatedPage ->
                val pagePayload = animatedPage.payload
                if (animatedPage.isGeneratingTurn || pagePayload == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                        border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = ImperialGold,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Generando Turno #${animatedPage.turnNumber}...",
                                style = MaterialTheme.typography.titleMedium,
                                color = ImperialGoldLight
                            )
                        }
                    }
                } else {
                    RemodeledProblemPresentationCard(
                        turnNumber = animatedPage.turnNumber,
                        payload = pagePayload,
                        currentEvent = uiState.currentNarrativeEvent,
                        moralScores = uiState.moralScores,
                        voterCount = uiState.players.size,
                        onAdvanceStepSound = onTriggerEventLogSound
                    )
                }
            }
        }

        if (!uiState.isGeneratingTurn && payload != null) {
            if (payload.isStoryConcluded) {
                item {
                    val unlockedEvaluations = EthicalAchievementsEngine.evaluateMedals(
                        players = uiState.players,
                        storyLogs = uiState.storyLogs,
                        moralScores = uiState.moralScores
                    )
                    EthicalAchievementsShowcaseCard(
                        evaluations = unlockedEvaluations,
                        showLockedMedals = false,
                        modifier = Modifier.widthIn(max = 640.dp)
                    )
                }

                item {
                    Button(
                        onClick = onConcludeAndSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .height(52.dp)
                            .testTag("conclude_epilogue_save_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImperialGold,
                            contentColor = Color(0xFF07111E)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar Crónica Final",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // Cabecera de Paso 2 + Complejidad según Votantes
                item {
                    val voterCount = uiState.players.size.coerceIn(1, 6)
                    val complexity = VoterDilemmaComplexity.forVoterCount(voterCount)
                    val complexityColor = when (complexity) {
                        VoterDilemmaComplexity.SOLITARIO_INTROSPECTIVO -> EmeraldVirtue
                        VoterDilemmaComplexity.DUO_DIALECTICO -> SapphireOrder
                        VoterDilemmaComplexity.CONSEJO_DELIBERATIVO -> TorchAmber
                        VoterDilemmaComplexity.ASAMBLEA_FACCIONES -> CrimsonChaos
                    }

                    Surface(
                        color = ObsidianBackground.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, complexityColor.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .testTag("voter_complexity_tabletop_badge")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "⚖️", fontSize = 14.sp)
                                Text(
                                    text = if (uiState.selectedOptionForTurn == null) {
                                        "Paso 2: Decide la solución al problema"
                                    } else {
                                        "Paso 2 completado: Solución seleccionada"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.selectedOptionForTurn != null) EmeraldVirtue else ImperialGoldLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                color = complexityColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, complexityColor.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "🗳️ ${complexity.quorumLabelEs(voterCount)} • ${payload.options.size} opc",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = complexityColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // PASO 2: SE DECIDE LA SOLUCIÓN AL PROBLEMA
                items(payload.options, key = { it.id }) { option ->
                    val isSelectedOption = uiState.selectedOptionForTurn?.id == option.id
                    DilemmaOptionCard(
                        option = option,
                        isSelected = isSelectedOption,
                        isLocked = uiState.isDiceLockedForTurn,
                        onSelect = { onChooseOption(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .testTag("dilemma_option_${option.id}")
                    )
                }

                // Acción Libre Compacta (solo editable antes de bloquear el dado)
                if (!uiState.isDiceLockedForTurn) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 640.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.customActionInput,
                                onValueChange = onCustomActionChange,
                                placeholder = {
                                    Text(
                                        text = "✍️ O escribe otra solución de ${activeHero.name}...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImperialGold,
                                    unfocusedBorderColor = ParchmentBorder,
                                    focusedTextColor = ParchmentLight,
                                    unfocusedTextColor = ParchmentLight
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_ethical_action_input")
                            )
                            IconButton(
                                onClick = onSubmitCustomAction,
                                enabled = uiState.customActionInput.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (uiState.customActionInput.isNotBlank()) ImperialGold
                                        else ParchmentBorder
                                    )
                                    .testTag("send_custom_action_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.send_action_desc),
                                    tint = Color(0xFF07111E)
                                )
                            }
                        }
                    }
                }

                // PASO 3: SE TIRA EL DADO D20 (ÚNICO TIRO BLOQUEADO POR TURNO)
                item {
                    HonestD20InputCard(
                        currentRoll = uiState.currentDiceRoll,
                        manualInput = uiState.manualDiceInput,
                        isRolling = uiState.isRollingDice,
                        isStepUnlocked = uiState.selectedOptionForTurn != null,
                        isDiceLocked = uiState.isDiceLockedForTurn,
                        selectedOptionLabel = uiState.selectedOptionForTurn?.label,
                        onRollVirtualClick = onRollVirtualD20,
                        onManualInputChange = onManualDiceChange,
                        onConfirmPhysicalRoll = onConfirmPhysicalDiceRoll,
                        modifier = Modifier.widthIn(max = 640.dp)
                    )
                }

                // PASOS 4 Y 5: TARJETA DE DESENLACE EN MESA (RESULTADO SEGÚN EL D20 + MODIFICACIÓN DE STATS DEL PERSONAJE)
                if (resolution != null) {
                    item {
                        TurnOutcomeAndStatsCard(
                            resolution = resolution,
                            onContinueNextTurn = onAdvanceToNextTurn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 640.dp)
                                .testTag("turn_outcome_resolution_card")
                        )
                    }
                }
            }
        }

        // 5. Gráfico de Radar Recharts / Brújula Moral en Tiempo Real
        item {
            MoralCompassCard(
                scores = uiState.moralScores,
                history = uiState.scoresHistory,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        // 6. Panel de Log de Eventos Cronológico (Decisiones → Consecuencias Directas)
        item {
            ChronologicalEventLogPanel(
                storyLogs = uiState.storyLogs,
                currentTurnNumber = uiState.currentTurnNumber,
                onTriggerEventLogSound = onTriggerEventLogSound,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }
    }
}

/**
 * Cinta visual de 5 pasos que guía el orden exacto de la toma de decisiones:
 * 1. Problema -> 2. Solución -> 3. Tiro D20 -> 4. Resultado -> 5. Stats
 */
@Composable
private fun TurnFiveStepOrderBanner(
    currentStep: Int,
    hasSelectedOption: Boolean,
    isDiceLocked: Boolean,
    hasResolution: Boolean,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        1 to "1. Problema",
        2 to "2. Solución",
        3 to "3. Dado D20",
        4 to "4. Resultado",
        5 to "5. Stats"
    )

    Surface(
        color = ArcaneSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("turn_five_step_order_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEach { (stepNum, label) ->
                val isCompleted = when (stepNum) {
                    1 -> true
                    2 -> hasSelectedOption
                    3 -> isDiceLocked
                    4, 5 -> hasResolution
                    else -> false
                }
                val isCurrent = when (stepNum) {
                    2 -> !hasSelectedOption
                    3 -> hasSelectedOption && !isDiceLocked
                    4, 5 -> hasResolution
                    else -> currentStep == stepNum
                }

                val pillColor = when {
                    isCurrent -> ImperialGold
                    isCompleted -> EmeraldVirtue
                    else -> ParchmentBorder
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        isCurrent -> ImperialGold.copy(alpha = 0.24f)
                        isCompleted -> EmeraldVirtue.copy(alpha = 0.16f)
                        else -> ObsidianBackground.copy(alpha = 0.6f)
                    },
                    border = BorderStroke(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = pillColor.copy(alpha = if (isCurrent || isCompleted) 0.85f else 0.45f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.5.sp,
                        fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCurrent -> ImperialGoldLight
                            isCompleted -> EmeraldVirtue
                            else -> ParchmentMuted
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de Desenlace en Mesa (Pasos 4 y 5):
 * - Paso 4: Muestra el resultado de la acción basándose en el número del dado que sacó.
 * - Paso 5: Muestra la modificación de las estadísticas del personaje de la acción y del grupo, con botón de continuar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TurnOutcomeAndStatsCard(
    resolution: TurnOutcomeResolution,
    onContinueNextTurn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rollColor = when {
        resolution.diceRoll == 20 -> ImperialGoldLight
        resolution.diceRoll >= 15 -> EmeraldVirtue
        resolution.diceRoll >= 10 -> SapphireOrder
        resolution.diceRoll >= 6 -> TorchAmber
        else -> CrimsonChaos
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneCardElevated),
        border = BorderStroke(2.dp, rollColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera del Paso 4: Resultado basado en el número del dado D20
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = rollColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, rollColor)
                ) {
                    Text(
                        text = "🎯 PASO 4: RESULTADO DE LA ACCIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = rollColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = ObsidianBackground,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, rollColor)
                ) {
                    Text(
                        text = "🎲 D20 = ${resolution.diceRoll}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = rollColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Narrativa directa del resultado según el número del dado
            Surface(
                color = ObsidianBackground.copy(alpha = 0.78f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, rollColor.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${resolution.heroName} → «${resolution.chosenOption.label}»",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGoldLight
                    )
                    Text(
                        text = resolution.actionOutcomeNarrativeEs,
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentLight
                    )
                }
            }

            // PASO 5: Modificación de Estadísticas del Personaje de la Acción
            Surface(
                color = ArcaneSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_stat_modifications_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ PASO 5: MODIFICACIÓN DE STATS DE ${resolution.heroName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ImperialGoldLight
                        )
                        Text(
                            text = "Escalado por D20 (${resolution.diceRoll})",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = TorchAmber
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HeroStatMiniPill(
                            emoji = "⚖️",
                            label = "Honor",
                            value = resolution.updatedHeroStats.honor,
                            color = SapphireOrder,
                            delta = resolution.heroStatDeltas.honorDelta,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatMiniPill(
                            emoji = "💚",
                            label = "Empatía",
                            value = resolution.updatedHeroStats.empathy,
                            color = EmeraldVirtue,
                            delta = resolution.heroStatDeltas.empathyDelta,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatMiniPill(
                            emoji = "🛡️",
                            label = "Disciplina",
                            value = resolution.updatedHeroStats.discipline,
                            color = AmethystWisdom,
                            delta = resolution.heroStatDeltas.disciplineDelta,
                            modifier = Modifier.weight(1f)
                        )
                        HeroStatMiniPill(
                            emoji = "🦅",
                            label = "Audacia",
                            value = resolution.updatedHeroStats.audacity,
                            color = TorchAmber,
                            delta = resolution.heroStatDeltas.audacityDelta,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Impacto en el Radar Ético del Grupo
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Radar Grupal:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        DeltaPill("⚖️ Justicia", resolution.groupJusticeDelta, SapphireOrder)
                        DeltaPill("💚 Compasión", resolution.groupCompassionDelta, EmeraldVirtue)
                        DeltaPill("🛡️ Deber", resolution.groupDutyDelta, AmethystWisdom)
                        DeltaPill("🦅 Libertad", resolution.groupLibertyDelta, TorchAmber)
                    }
                }
            }

            // Botón para avanzar al siguiente turno tras revisar resultado y stats
            Button(
                onClick = onContinueNextTurn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_next_turn_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImperialGold,
                    contentColor = Color(0xFF07111E)
                )
            ) {
                Text(
                    text = "Continuar al Siguiente Turno",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

/**
 * Panel compacto de Log de Eventos Cronológico:
 * Muestra la línea temporal de decisiones tomadas (T1 → Tn) y sus consecuencias directas
 * (nivel de resultado D20 + impacto directo en estadísticas éticas) con iconos, efectos de sonido
 * de fantasía y mínimo texto.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChronologicalEventLogPanel(
    storyLogs: List<StoryTurnLog>,
    currentTurnNumber: Int = storyLogs.size + 1,
    onTriggerEventLogSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var chronologicalAscending by remember { mutableStateOf(true) }

    val logUpdateFlash = rememberStatFlashState(
        score = storyLogs.size * 10,
        historyDelta = if (storyLogs.isNotEmpty()) 10 else 0,
        turnTriggerKey = storyLogs.size
    )

    val enrichedLogs = remember(storyLogs) {
        val sortedAsc = storyLogs.sortedBy { it.turnNumber }
        var prevScores = MoralScores(50, 50, 50, 50)
        sortedAsc.map { log ->
            val snap = log.scoresSnapshot
            val jDelta = if (log.justiceDelta != 0 || snap == null) log.justiceDelta else (snap.justice - prevScores.justice)
            val cDelta = if (log.compassionDelta != 0 || snap == null) log.compassionDelta else (snap.compassion - prevScores.compassion)
            val pDelta = if (log.pragmatismDelta != 0 || snap == null) log.pragmatismDelta else (snap.pragmatism - prevScores.pragmatism)
            val lDelta = if (log.libertyDelta != 0 || snap == null) log.libertyDelta else (snap.liberty - prevScores.liberty)
            if (snap != null) {
                prevScores = snap
            }
            log.copy(
                justiceDelta = jDelta,
                compassionDelta = cDelta,
                pragmatismDelta = pDelta,
                libertyDelta = lDelta
            )
        }
    }

    val displayedLogs = remember(enrichedLogs, chronologicalAscending) {
        if (chronologicalAscending) enrichedLogs else enrichedLogs.reversed()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chronological_event_log_panel"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
        border = BorderStroke(
            width = if (logUpdateFlash.flashAlpha > 0.05f) 1.8.dp else 1.dp,
            color = if (logUpdateFlash.flashAlpha > 0.05f) ImperialGoldLight else ImperialGold.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isExpanded = !isExpanded
                            onTriggerEventLogSound()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (logUpdateFlash.flashAlpha > 0.05f) {
                            ImperialGold.copy(alpha = 0.45f)
                        } else {
                            ImperialGold.copy(alpha = 0.2f)
                        },
                        border = BorderStroke(1.dp, ImperialGold),
                        modifier = Modifier.graphicsLayer {
                            scaleX = logUpdateFlash.scale
                            scaleY = logUpdateFlash.scale
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = ImperialGold,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(16.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Log de Eventos (${storyLogs.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight
                            )
                            if (logUpdateFlash.flashAlpha > 0.05f) {
                                Text(
                                    text = "📜✨",
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "Decisiones, D20 y consecuencias directas",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (storyLogs.size > 1 && isExpanded) {
                        Surface(
                            onClick = {
                                chronologicalAscending = !chronologicalAscending
                                onTriggerEventLogSound()
                            },
                            shape = RoundedCornerShape(50),
                            color = ObsidianBackground.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("toggle_event_log_order_button")
                        ) {
                            Text(
                                text = if (chronologicalAscending) "⏳ T1→T${storyLogs.size}" else "🔽 Recientes",
                                style = MaterialTheme.typography.labelSmall,
                                color = ImperialGoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isExpanded = !isExpanded
                            onTriggerEventLogSound()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("toggle_event_log_expand_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Ocultar log" else "Mostrar log",
                            tint = ParchmentMuted
                        )
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_log_progress_stepper")
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ObsidianBackground.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, ParchmentBorder)
                    ) {
                        Text(
                            text = "🚀 T0",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                items(enrichedLogs, key = { it.turnNumber }) { log ->
                    val tierColor = when {
                        log.diceRoll >= 15 -> EmeraldVirtue
                        log.diceRoll >= 6 -> TorchAmber
                        else -> CrimsonChaos
                    }
                    val tierIcon = when {
                        log.diceRoll >= 15 -> "🌟"
                        log.diceRoll >= 6 -> "⚖️"
                        else -> "⚡"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("→", style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = tierColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, tierColor.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "$tierIcon T${log.turnNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tierColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("→", style = MaterialTheme.typography.labelSmall, color = ParchmentMuted)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ImperialGold.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, ImperialGold)
                        ) {
                            Text(
                                text = "🎯 T$currentTurnNumber",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                if (displayedLogs.isEmpty()) {
                    Surface(
                        color = ObsidianBackground.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ParchmentBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🧭", fontSize = 16.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "T0: Inicio de Campaña • Valores base 50%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ParchmentLight
                                )
                                Text(
                                    text = "Elige una solución y lanza tu único D20 para registrar las consecuencias.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        displayedLogs.forEachIndexed { idx, log ->
                            ChronologicalEventNodeRow(
                                log = log,
                                isLastNode = idx == displayedLogs.lastIndex,
                                modifier = Modifier.testTag("event_log_item_${log.turnNumber}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChronologicalEventNodeRow(
    log: StoryTurnLog,
    isLastNode: Boolean,
    modifier: Modifier = Modifier
) {
    val tierColor = when {
        log.diceRoll >= 15 -> EmeraldVirtue
        log.diceRoll >= 6 -> TorchAmber
        else -> CrimsonChaos
    }
    val consequenceBadgeText = when {
        log.directConsequenceSummary.isNotBlank() -> log.directConsequenceSummary
        log.diceRoll >= 15 -> "🌟 Éxito pleno • ${log.chosenPhilosophy}"
        log.diceRoll >= 6 -> "⚖️ Éxito con costo • ${log.chosenPhilosophy}"
        else -> "⚡ Infortunio • ${log.chosenPhilosophy}"
    }
    val shortHeroName = log.heroIdentity.substringBefore(" ")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = tierColor.copy(alpha = 0.22f),
                border = BorderStroke(1.5.dp, tierColor),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "T${log.turnNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = tierColor
                    )
                }
            }
            if (!isLastNode) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(42.dp)
                        .background(ParchmentBorder.copy(alpha = 0.65f))
                )
            }
        }

        Surface(
            color = ObsidianBackground.copy(alpha = 0.72f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$shortHeroName → «${log.chosenOptionLabel}»",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ParchmentLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = tierColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "🎲 ${log.diceRoll}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = ArcaneSurfaceVariant.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, tierColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = consequenceBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ImperialGoldLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    DeltaPill("⚖️", log.justiceDelta, SapphireOrder)
                    DeltaPill("💚", log.compassionDelta, EmeraldVirtue)
                    DeltaPill("🛡️", log.pragmatismDelta, AmethystWisdom)
                    DeltaPill("🦅", log.libertyDelta, TorchAmber)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DilemmaOptionCard(
    option: DilemmaOption,
    isSelected: Boolean,
    isLocked: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        enabled = !isLocked,
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSelected -> ArcaneCardElevated
            isLocked -> ArcaneSurface.copy(alpha = 0.55f)
            else -> ArcaneSurfaceVariant.copy(alpha = 0.90f)
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> EmeraldVirtue
                isLocked -> ParchmentBorder.copy(alpha = 0.45f)
                else -> ImperialGold.copy(alpha = 0.55f)
            }
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isSelected -> EmeraldVirtue
                    else -> ObsidianBackground
                },
                border = BorderStroke(1.dp, if (isSelected) EmeraldVirtue else ImperialGold),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF061A12),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = option.id.takeLast(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImperialGoldLight
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ImperialGoldLight else ParchmentLight,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Surface(
                            color = EmeraldVirtue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, EmeraldVirtue)
                        ) {
                            Text(
                                text = "SOLUCIÓN ELEGIDA",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldVirtue,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ParchmentMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = ImperialGold.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = option.philosophyTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = ImperialGoldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    DeltaPill("⚖️", option.justiceDelta, SapphireOrder)
                    DeltaPill("💚", option.compassionDelta, EmeraldVirtue)
                    DeltaPill("🛡️", option.pragmatismDelta, AmethystWisdom)
                    DeltaPill("🦅", option.libertyDelta, TorchAmber)
                }
            }
        }
    }
}

@Composable
private fun DeltaPill(icon: String, value: Int, color: Color) {
    if (value == 0) return
    val sign = if (value > 0) "+$value" else value.toString()
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = "$icon $sign",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CampaignEpilogueScreen(
    uiState: DungeonsUiState,
    onStartNewGame: () -> Unit,
    onViewChroniclesTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("campaign_epilogue_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.5.dp, ImperialGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ImperialGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "AVENTURA COMPLETADA",
                        style = MaterialTheme.typography.titleLarge,
                        color = ImperialGold,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = ImperialGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, ImperialGold)
                    ) {
                        Text(
                            text = "${uiState.moralScores.dominantAlignmentEs()} • ${uiState.storyLogs.size.coerceAtLeast(1)} Turnos",
                            style = MaterialTheme.typography.labelLarge,
                            color = ImperialGoldLight,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            MoralCompassCard(
                scores = uiState.moralScores,
                history = uiState.scoresHistory,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        item {
            ChronologicalEventLogPanel(
                storyLogs = uiState.storyLogs,
                currentTurnNumber = uiState.storyLogs.size,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        item {
            val evaluations = EthicalAchievementsEngine.evaluateMedals(
                players = uiState.players,
                storyLogs = uiState.storyLogs,
                moralScores = uiState.moralScores
            )
            EthicalAchievementsShowcaseCard(
                evaluations = evaluations,
                showLockedMedals = true,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewChroniclesTab,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, ImperialGold)
                ) {
                    Text("Crónicas", color = ImperialGoldLight)
                }
                Button(
                    onClick = onStartNewGame,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("epilogue_new_game_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color(0xFF07111E)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.new_campaign_btn),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SavedChroniclesScreen(
    chronicles: List<ChronicleEntity>,
    selectedChronicle: ChronicleEntity?,
    onOpenChronicle: (ChronicleEntity) -> Unit,
    onDismissModal: () -> Unit,
    onDeleteChronicle: (Long) -> Unit,
    onGoToAdventure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("es-ES"))

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("saved_chronicles_screen")
    ) {
        if (chronicles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = ImperialGold.copy(alpha = 0.7f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin Crónicas Guardadas",
                    style = MaterialTheme.typography.titleLarge,
                    color = ImperialGoldLight,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGoToAdventure,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color(0xFF07111E)
                    )
                ) {
                    Text("Jugar Partida", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Crónicas Guardadas (${chronicles.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = ImperialGold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                    )
                }

                items(chronicles, key = { it.id }) { item ->
                    Card(
                        onClick = { onOpenChronicle(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp)
                            .testTag("chronicle_item_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                        border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.realmTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ImperialGoldLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = ImperialGold.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = item.dominantAlignment,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ImperialGoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${item.turnsCompleted} turnos • ${dateFormatter.format(Date(item.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ParchmentMuted
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteChronicle(item.id) },
                                modifier = Modifier.testTag("delete_chronicle_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar crónica",
                                    tint = ParchmentMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedChronicle != null) {
            val parsedLogs = try {
                GeminiRetrofitClient.json.decodeFromString<List<StoryTurnLog>>(
                    selectedChronicle.fullTranscriptJson
                )
            } catch (_: Exception) {
                emptyList()
            }

            AlertDialog(
                onDismissRequest = onDismissModal,
                containerColor = ArcaneSurface,
                titleContentColor = ImperialGold,
                textContentColor = ParchmentLight,
                title = {
                    Text(
                        text = selectedChronicle.realmTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(340.dp)
                    ) {
                        item {
                            val chronicleHistory = remember(parsedLogs, selectedChronicle) {
                                val initial = MoralScores(50, 50, 50, 50)
                                val snapshots = parsedLogs.mapNotNull { it.scoresSnapshot }
                                if (snapshots.isEmpty()) {
                                    listOf(
                                        initial,
                                        MoralScores(
                                            justice = selectedChronicle.justiceScore,
                                            compassion = selectedChronicle.compassionScore,
                                            pragmatism = selectedChronicle.pragmatismScore,
                                            liberty = selectedChronicle.libertyScore
                                        )
                                    )
                                } else {
                                    listOf(initial) + snapshots
                                }
                            }
                            MoralCompassCard(
                                scores = MoralScores(
                                    justice = selectedChronicle.justiceScore,
                                    compassion = selectedChronicle.compassionScore,
                                    pragmatism = selectedChronicle.pragmatismScore,
                                    liberty = selectedChronicle.libertyScore
                                ),
                                history = chronicleHistory
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ChronologicalEventLogPanel(
                                storyLogs = parsedLogs,
                                currentTurnNumber = parsedLogs.size
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismissModal) {
                        Text("Cerrar", color = ImperialGold)
                    }
                }
            )
        }
    }
}

@Composable
fun EthicalCodexScreen(
    modifier: Modifier = Modifier
) {
    var expandedArchetype by remember { mutableStateOf<EthicalArchetype?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ethical_codex_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = ImperialGold
                )
                Text(
                    text = "Guía Rápida Visual",
                    style = MaterialTheme.typography.titleLarge,
                    color = ImperialGold
                )
            }
        }

        items(EthicalArchetype.entries) { archetype ->
            val isExpanded = expandedArchetype == archetype
            Card(
                onClick = { expandedArchetype = if (isExpanded) null else archetype },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurfaceVariant.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, if (isExpanded) ImperialGold else ParchmentBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = archetype.emoji, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = archetype.maleForm,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialGoldLight
                                )
                                Text(
                                    text = "${archetype.coreDimensionEs} • ${archetype.statBonusLabelEs}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TorchAmber
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = ParchmentMuted
                        )
                    }
                    AnimatedVisibility(visible = isExpanded) {
                        Text(
                            text = archetype.mottoEs,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = ParchmentLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = ImperialGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Especies y Bonificadores",
                            style = MaterialTheme.typography.titleMedium,
                            color = ImperialGold
                        )
                    }
                    HeroSpecies.entries.forEach { sp ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = sp.emoji, fontSize = 18.sp)
                            Text(
                                text = sp.labelEs,
                                style = MaterialTheme.typography.labelLarge,
                                color = ImperialGoldLight
                            )
                            Text(
                                text = "• ${sp.statBonusLabelEs}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TorchAmber,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
