package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.NarrativeEventType
import com.example.data.DilemmaOption
import com.example.data.DmTurnPayload
import com.example.data.MoralScores
import com.example.data.VoterDilemmaComplexity
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
import kotlin.math.abs
import kotlin.math.max

data class StructuredProblemPresentation(
    val situationText: String,
    val conflictText: String,
    val keyQuestionText: String,
    val isConcluded: Boolean
)

data class ValueAtStakeTag(
    val label: String,
    val emoji: String,
    val tensionBadge: String,
    val accentColor: Color
)

enum class GroupMoralRiskLevel(
    val labelEs: String,
    val summaryEs: String,
    val activeSegments: Int,
    val accentColor: Color
) {
    BAJO(
        labelEs = "Riesgo Bajo",
        summaryEs = "Impacto contenido",
        activeSegments = 1,
        accentColor = EmeraldVirtue
    ),
    MODERADO(
        labelEs = "Riesgo Moderado",
        summaryEs = "Cruce de valores",
        activeSegments = 2,
        accentColor = SapphireOrder
    ),
    ALTO(
        labelEs = "Riesgo Alto",
        summaryEs = "Fricción moral severa",
        activeSegments = 3,
        accentColor = TorchAmber
    ),
    CRITICO(
        labelEs = "Riesgo Crítico",
        summaryEs = "Fractura ética inminente",
        activeSegments = 4,
        accentColor = CrimsonChaos
    )
}

data class MoralStakesSummary(
    val valuesAtStake: List<ValueAtStakeTag>,
    val riskLevel: GroupMoralRiskLevel,
    val riskDetailEs: String
)

internal fun parseProblemPresentation(payload: DmTurnPayload): StructuredProblemPresentation {
    val cleanNarrative = payload.narrativeText.trim()
    val paragraphs = cleanNarrative
        .split(Regex("\\n\\s*\\n+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val (situation, conflict) = when {
        paragraphs.size >= 2 -> {
            paragraphs.first() to paragraphs.drop(1).joinToString("\n\n")
        }
        paragraphs.size == 1 -> {
            val single = paragraphs.first()
            val sentences = single
                .split(Regex("(?<=[.!?…])\\s+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (sentences.size >= 2) {
                val splitIdx = (sentences.size / 2).coerceAtLeast(1)
                sentences.take(splitIdx).joinToString(" ") to sentences.drop(splitIdx).joinToString(" ")
            } else {
                single to if (payload.isStoryConcluded) {
                    "Las consecuencias éticas de vuestro viaje quedan grabadas en la memoria del reino."
                } else {
                    "Las posturas del grupo chocan ante una decisión que exigirá asumir un costo moral real."
                }
            }
        }
        else -> {
            "El grupo observa la escena en silencio mientras evalúa el terreno." to
                "Cada camino posible pondrá a prueba las convicciones de la compañía."
        }
    }

    val keyQuestion = payload.ethicalQuestion.trim().ifEmpty {
        if (payload.isStoryConcluded) {
            "¿Qué enseñanza ética perdurará tras el desenlace de esta crónica?"
        } else {
            "¿Qué camino elegirá el grupo frente a este dilema?"
        }
    }

    return StructuredProblemPresentation(
        situationText = situation,
        conflictText = conflict,
        keyQuestionText = keyQuestion,
        isConcluded = payload.isStoryConcluded
    )
}

internal fun computeMoralStakes(
    options: List<DilemmaOption>,
    scores: MoralScores,
    voterCount: Int
): MoralStakesSummary {
    val complexity = VoterDilemmaComplexity.forVoterCount(voterCount)

    if (options.isEmpty()) {
        val topValues = listOf(
            Triple("Justicia", "⚖️", scores.justice to SapphireOrder),
            Triple("Compasión", "💚", scores.compassion to EmeraldVirtue),
            Triple("Deber", "🧭", scores.pragmatism to AmethystWisdom),
            Triple("Libertad", "🕊️", scores.liberty to TorchAmber)
        ).sortedByDescending { it.third.first }
            .take(2)
            .map { (name, emoji, pair) ->
                ValueAtStakeTag(
                    label = name,
                    emoji = emoji,
                    tensionBadge = "${pair.first}%",
                    accentColor = pair.second
                )
            }
        return MoralStakesSummary(
            valuesAtStake = topValues,
            riskLevel = GroupMoralRiskLevel.MODERADO,
            riskDetailEs = "Balance final de la crónica"
        )
    }

    data class PillarAnalysis(
        val label: String,
        val emoji: String,
        val accentColor: Color,
        val totalSwing: Int,
        val minDelta: Int,
        val maxDelta: Int
    )

    fun analyzePillar(
        label: String,
        emoji: String,
        color: Color,
        selector: (DilemmaOption) -> Int
    ): PillarAnalysis {
        val deltas = options.map(selector)
        return PillarAnalysis(
            label = label,
            emoji = emoji,
            accentColor = color,
            totalSwing = deltas.sumOf { abs(it) },
            minDelta = deltas.minOrNull() ?: 0,
            maxDelta = deltas.maxOrNull() ?: 0
        )
    }

    val pillars = listOf(
        analyzePillar("Justicia", "⚖️", SapphireOrder) { it.justiceDelta },
        analyzePillar("Compasión", "💚", EmeraldVirtue) { it.compassionDelta },
        analyzePillar("Deber", "🧭", AmethystWisdom) { it.pragmatismDelta },
        analyzePillar("Libertad", "🕊️", TorchAmber) { it.libertyDelta }
    )

    val activeTags = pillars
        .filter { it.totalSwing > 0 }
        .sortedByDescending { it.totalSwing }
        .take(3)
        .map { pillar ->
            val badge = when {
                pillar.maxDelta > 0 && pillar.minDelta < 0 -> "En disputa"
                pillar.minDelta <= -6 -> "En riesgo"
                pillar.maxDelta >= 6 -> "Clave"
                else -> "En juego"
            }
            ValueAtStakeTag(
                label = pillar.label,
                emoji = pillar.emoji,
                tensionBadge = badge,
                accentColor = pillar.accentColor
            )
        }
        .ifEmpty {
            listOf(
                ValueAtStakeTag("Justicia", "⚖️", "En juego", SapphireOrder),
                ValueAtStakeTag("Compasión", "💚", "En juego", EmeraldVirtue)
            )
        }

    val maxOptionSwing = options.maxOfOrNull { opt ->
        abs(opt.justiceDelta) + abs(opt.compassionDelta) + abs(opt.pragmatismDelta) + abs(opt.libertyDelta)
    } ?: 12

    val minScore = minOf(scores.justice, scores.compassion, scores.pragmatism, scores.liberty)
    val maxScore = maxOf(scores.justice, scores.compassion, scores.pragmatism, scores.liberty)
    val polarization = maxScore - minScore
    val disputedCount = pillars.count { it.maxDelta > 0 && it.minDelta < 0 }

    val weightedIntensity = (maxOptionSwing * complexity.stakeMultiplier) +
        (if (minScore < 35) 8f else 0f) +
        (if (polarization >= 28) 5f else 0f) +
        (disputedCount * 3f)

    val riskLevel = when {
        weightedIntensity >= 34f || minScore <= 22 -> GroupMoralRiskLevel.CRITICO
        weightedIntensity >= 24f || disputedCount >= 2 -> GroupMoralRiskLevel.ALTO
        weightedIntensity >= 15f -> GroupMoralRiskLevel.MODERADO
        else -> GroupMoralRiskLevel.BAJO
    }

    val riskDetail = when {
        minScore < 35 -> "Pilar grupal vulnerable ($minScore%)"
        disputedCount >= 2 -> "$disputedCount pilares enfrentados"
        voterCount >= 3 -> "Mesa de $voterCount votantes (${complexity.shortTierEs})"
        else -> riskLevel.summaryEs
    }

    return MoralStakesSummary(
        valuesAtStake = activeTags,
        riskLevel = riskLevel,
        riskDetailEs = riskDetail
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RemodeledProblemPresentationCard(
    turnNumber: Int,
    payload: DmTurnPayload,
    currentEvent: NarrativeEventType,
    moralScores: MoralScores,
    voterCount: Int,
    onAdvanceStepSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val presentation = remember(turnNumber, payload.chapterTitle, payload.narrativeText, payload.ethicalQuestion, payload.isStoryConcluded) {
        parseProblemPresentation(payload)
    }
    val stakes = remember(turnNumber, payload.options, moralScores, voterCount) {
        computeMoralStakes(payload.options, moralScores, voterCount)
    }

    // Lectura progresiva por tarjetas: 1 = Situación, 2 = Situación + Conflicto, 3 = Todos + Pregunta Clave
    var revealedStepCount by remember(turnNumber, payload.chapterTitle, payload.ethicalQuestion) {
        mutableIntStateOf(1)
    }

    val eventColor = when (currentEvent) {
        NarrativeEventType.EXITO -> EmeraldVirtue
        NarrativeEventType.FALLO_CRITICO -> CrimsonChaos
        NarrativeEventType.DILEMA -> ImperialGoldLight
    }

    fun advanceStep() {
        if (revealedStepCount < 3) {
            revealedStepCount += 1
            onAdvanceStepSound()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 280))
            .testTag("dm_narrative_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
        border = BorderStroke(
            width = if (payload.isStoryConcluded) 2.dp else 1.5.dp,
            color = if (payload.isStoryConcluded) EmeraldVirtue else ImperialGold.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera: Insignia Paso 1 + Título del Capítulo + Selector de Revelado Rápido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = eventColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, eventColor.copy(alpha = 0.7f)),
                    modifier = Modifier.testTag("narrative_event_badge")
                ) {
                    Text(
                        text = if (presentation.isConcluded) {
                            "🏁 EPÍLOGO • ${currentEvent.labelEs}"
                        } else {
                            "📌 PASO 1: PROBLEMA • ${currentEvent.labelEs}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = eventColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = ObsidianBackground.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ParchmentBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            revealedStepCount = if (revealedStepCount < 3) 3 else 1
                            onAdvanceStepSound()
                        }
                        .testTag("problem_reveal_all_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = ImperialGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (revealedStepCount < 3) "Ver todo" else "Paso $revealedStepCount/3",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ImperialGoldLight
                        )
                    }
                }
            }

            // Título del capítulo + Progreso visual de tarjetas (1/3, 2/3, 3/3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payload.chapterTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGoldLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StepProgressDotsBar(
                    currentRevealed = revealedStepCount,
                    onSelectStep = { targetStep ->
                        revealedStepCount = targetStep
                        onAdvanceStepSound()
                    }
                )
            }

            // Apoyo Visual Rápido: Valores en Juego + Riesgo Moral del Grupo
            Surface(
                color = ObsidianBackground.copy(alpha = 0.72f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ParchmentBorder.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("problem_moral_stakes_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = stakes.riskLevel.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stakes.riskLevel.labelEs,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = stakes.riskLevel.accentColor
                            )
                            Text(
                                text = "• ${stakes.riskDetailEs}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Medidor de intensidad en 4 segmentos
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (segment in 1..4) {
                                val filled = segment <= stakes.riskLevel.activeSegments
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (filled) stakes.riskLevel.accentColor
                                            else ParchmentBorder.copy(alpha = 0.45f)
                                        )
                                )
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        stakes.valuesAtStake.forEach { tag ->
                            Surface(
                                color = tag.accentColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, tag.accentColor.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = tag.emoji, fontSize = 11.sp)
                                    Text(
                                        text = tag.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ParchmentLight
                                    )
                                    Text(
                                        text = "· ${tag.tensionBadge}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = tag.accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BLOQUE 1: SITUACIÓN EN ESCENA (Siempre visible al iniciar turno)
            ProblemNarrativeBlockCard(
                stepLabel = if (presentation.isConcluded) "1/3 • DESENLACE" else "1/3 • SITUACIÓN",
                icon = Icons.Default.Place,
                accentColor = SapphireOrder,
                bodyText = presentation.situationText,
                isEmphasized = revealedStepCount == 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = revealedStepCount < 3) { advanceStep() }
                    .testTag("problem_block_situation")
            )

            // BLOQUE 2: NUDO DEL CONFLICTO (Revelado al tocar paso 2)
            AnimatedVisibility(
                visible = revealedStepCount >= 2,
                enter = fadeIn(tween(240)) + expandVertically(tween(260)),
                exit = fadeOut(tween(160)) + shrinkVertically(tween(200))
            ) {
                ProblemNarrativeBlockCard(
                    stepLabel = if (presentation.isConcluded) "2/3 • ECO MORAL" else "2/3 • CONFLICTO",
                    icon = Icons.Default.Bolt,
                    accentColor = TorchAmber,
                    bodyText = presentation.conflictText,
                    isEmphasized = revealedStepCount == 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = revealedStepCount < 3) { advanceStep() }
                        .testTag("problem_block_conflict")
                )
            }

            // BLOQUE 3: PREGUNTA CLAVE (Revelado al tocar paso 3)
            AnimatedVisibility(
                visible = revealedStepCount >= 3,
                enter = fadeIn(tween(260)) + expandVertically(tween(280)),
                exit = fadeOut(tween(160)) + shrinkVertically(tween(200))
            ) {
                ProblemNarrativeBlockCard(
                    stepLabel = if (presentation.isConcluded) "3/3 • REFLEXIÓN FINAL" else "3/3 • PREGUNTA CLAVE",
                    icon = Icons.Default.Gavel,
                    accentColor = ImperialGold,
                    bodyText = presentation.keyQuestionText,
                    isEmphasized = true,
                    isKeyQuestion = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("problem_block_question")
                )
            }

            // Botón / Tarjeta táctil para revelar el siguiente bloque al ritmo de los jugadores
            if (revealedStepCount < 3) {
                val nextPrompt = if (revealedStepCount == 1) {
                    if (presentation.isConcluded) "Toca para revelar el Eco Moral (2/3)"
                    else "Toca para revelar el Conflicto (2/3)"
                } else {
                    if (presentation.isConcluded) "Toca para revelar la Reflexión Final (3/3)"
                    else "Toca para revelar la Pregunta Clave (3/3)"
                }
                val ctaAccent = if (revealedStepCount == 1) TorchAmber else ImperialGold

                Surface(
                    color = ctaAccent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, ctaAccent.copy(alpha = 0.72f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { advanceStep() }
                        .testTag("advance_problem_step_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = ctaAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = nextPrompt,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Avanzar lectura",
                            tint = ctaAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldVirtue,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (presentation.isConcluded) {
                            "Lectura del epílogo completada."
                        } else {
                            "Problema presentado • Elige una solución en el Paso 2."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldVirtue
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgressDotsBar(
    currentRevealed: Int,
    onSelectStep: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(1, 2, 3).forEach { step ->
            val isUnlocked = step <= currentRevealed
            val dotColor by animateColorAsState(
                targetValue = when {
                    step == currentRevealed -> ImperialGold
                    isUnlocked -> EmeraldVirtue
                    else -> ParchmentBorder
                },
                label = "stepDotColor"
            )
            Surface(
                color = dotColor.copy(alpha = if (isUnlocked) 0.22f else 0.1f),
                shape = CircleShape,
                border = BorderStroke(1.dp, dotColor),
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onSelectStep(step) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) ParchmentLight else ParchmentMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemNarrativeBlockCard(
    stepLabel: String,
    icon: ImageVector,
    accentColor: Color,
    bodyText: String,
    isEmphasized: Boolean,
    isKeyQuestion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isKeyQuestion -> ArcaneCardElevated.copy(alpha = 0.92f)
        isEmphasized -> ArcaneSurfaceVariant.copy(alpha = 0.92f)
        else -> ObsidianBackground.copy(alpha = 0.72f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isKeyQuestion) 1.5.dp else 1.dp,
            color = if (isEmphasized) accentColor.copy(alpha = 0.7f) else ParchmentBorder.copy(alpha = 0.75f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Franja vertical escaneable de color por bloque
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stepLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        color = accentColor
                    )
                }
                Text(
                    text = bodyText,
                    style = if (isKeyQuestion) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isKeyQuestion) FontWeight.SemiBold else FontWeight.Normal,
                    lineHeight = if (isKeyQuestion) 21.sp else 20.sp,
                    color = if (isKeyQuestion) ParchmentLight else ParchmentLight.copy(alpha = 0.92f)
                )
            }
        }
    }
}
