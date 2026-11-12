package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EthicalArchetype
import com.example.data.MoralScores
import com.example.data.PlayerHero
import com.example.data.StoryTurnLog
import com.example.ui.theme.AmethystWisdom
import com.example.ui.theme.ArcaneCardElevated
import com.example.ui.theme.ArcaneSurface
import com.example.ui.theme.ArcaneSurfaceVariant
import com.example.ui.theme.EmeraldVirtue
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.SapphireOrder
import com.example.ui.theme.TorchAmber
import kotlin.math.cos
import kotlin.math.sin

/**
 * Catálogo de Medallas y Logros Éticos (CORE I) que premian la consistencia del grupo
 * con su arquetipo ético elegido y su comportamiento a lo largo de la partida.
 */
enum class EthicalMedal(
    val id: String,
    val titleEs: String,
    val subtitleEs: String,
    val descriptionEs: String,
    val linkedArchetype: EthicalArchetype?,
    val emoji: String
) {
    EL_INQUEBRANTABLE(
        id = "el_inquebrantable",
        titleEs = "El Inquebrantable",
        subtitleEs = "Consistencia con Defensor de la Dignidad",
        descriptionEs = "Otorgada a quienes priorizaron siempre la dignidad humana intrínseca como fin en sí mismo sin ceder al cálculo utilitario.",
        linkedArchetype = EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD,
        emoji = "🛡️"
    ),
    ORACULO_SIN_VELOS(
        id = "oraculo_sin_velos",
        titleEs = "El Oráculo sin Velos",
        subtitleEs = "Consistencia con Buscador de Verdad",
        descriptionEs = "Otorgada por desafiar las mentiras piadosas y sostener la honestidad intelectual y el sentido de vida auténtico.",
        linkedArchetype = EthicalArchetype.BUSCADOR_DE_VERDAD,
        emoji = "🔮"
    ),
    ALAS_DEL_ALBEDRIO(
        id = "alas_del_albedrio",
        titleEs = "Alas del Albedrío",
        subtitleEs = "Consistencia con Explorador de Libertad",
        descriptionEs = "Otorgada por defender incansablemente la autonomía moral, la autodeterminación y la responsabilidad de elegir en libertad.",
        linkedArchetype = EthicalArchetype.EXPLORADOR_DE_LIBERTAD,
        emoji = "🦅"
    ),
    BALANZA_INCORRUPTIBLE(
        id = "balanza_incorruptible",
        titleEs = "La Balanza Incorruptible",
        subtitleEs = "Consistencia con Guardián de la Conciencia",
        descriptionEs = "Otorgada por escuchar la voz interior de la justicia y la rectitud moral incluso frente a presiones de poder o mayoría.",
        linkedArchetype = EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA,
        emoji = "⚖️"
    ),
    TEJEDOR_DE_FRATERNIDAD(
        id = "tejedor_de_fraternidad",
        titleEs = "Tejedor de Fraternidad",
        subtitleEs = "Consistencia con Constructor de Comunidad",
        descriptionEs = "Otorgada por priorizar el vínculo solidario, la reconciliación y el bien común para que nadie quede descartado.",
        linkedArchetype = EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD,
        emoji = "🤝"
    ),
    COHERENCIA_HEROICA_CORE(
        id = "coherencia_heroica_core",
        titleEs = "Fidelidad al Juramento CORE I",
        subtitleEs = "Consistencia Integral de Arquetipos del Grupo",
        descriptionEs = "El grupo mantuvo una coherencia sobresaliente entre los arquetipos éticos elegidos al inicio y las decisiones tomadas en la mesa.",
        linkedArchetype = null,
        emoji = "👑"
    ),
    RESILIENCIA_ANTE_EL_INFORTUNIO(
        id = "resiliencia_infortunio",
        titleEs = "Temple ante la Adversidad D20",
        subtitleEs = "Integridad Ética bajo Costo o Infortunio",
        descriptionEs = "El grupo sostuvo sus principios éticos tras enfrentar tiradas adversas del D20 (1-14) asumiendo el costo moral con honor.",
        linkedArchetype = null,
        emoji = "🔥"
    );

    fun accentColor(): Color = when (this) {
        EL_INQUEBRANTABLE -> EmeraldVirtue
        ORACULO_SIN_VELOS -> AmethystWisdom
        ALAS_DEL_ALBEDRIO -> TorchAmber
        BALANZA_INCORRUPTIBLE -> SapphireOrder
        TEJEDOR_DE_FRATERNIDAD -> EmeraldVirtue
        COHERENCIA_HEROICA_CORE -> ImperialGold
        RESILIENCIA_ANTE_EL_INFORTUNIO -> TorchAmber
    }
}

data class MedalEvaluationResult(
    val medal: EthicalMedal,
    val isUnlocked: Boolean,
    val consistencyPercent: Int,
    val unlockReasonEs: String
)

object EthicalAchievementsEngine {

    /**
     * Evalúa todas las medallas al final de la partida comparando los arquetipos elegidos por el grupo,
     * el historial de turnos (`storyLogs`) y la Brújula Moral (`moralScores`).
     */
    fun evaluateMedals(
        players: List<PlayerHero>,
        storyLogs: List<StoryTurnLog>,
        moralScores: MoralScores
    ): List<MedalEvaluationResult> {
        val partyArchetypes = players.map { it.archetype }.toSet()

        // Calcular cuántas decisiones en la bitácora coincidieron con la dimensión ética del héroe activo o del grupo
        val archetypeMatchCounts = mutableMapOf<EthicalArchetype, Int>()
        var coherentTurnCount = 0

        storyLogs.forEach { log ->
            val tag = (log.chosenPhilosophy + " " + log.chosenOptionLabel).lowercase()
            val matchesDignity = tag.contains("dignidad") || tag.contains("kant") || tag.contains("incondicional") || tag.contains("compasión")
            val matchesTruth = tag.contains("verdad") || tag.contains("sentido") || tag.contains("prudencia") || tag.contains("sabiduría")
            val matchesLiberty = tag.contains("libertad") || tag.contains("autonomía") || tag.contains("albedrío") || tag.contains("libre")
            val matchesJustice = tag.contains("justicia") || tag.contains("conciencia") || tag.contains("restaurativa") || tag.contains("equidad")
            val matchesCommunity = tag.contains("comunidad") || tag.contains("solidaridad") || tag.contains("fraternidad") || tag.contains("democrática")

            if (matchesDignity) {
                archetypeMatchCounts[EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD] =
                    (archetypeMatchCounts[EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD] ?: 0) + 1
            }
            if (matchesTruth) {
                archetypeMatchCounts[EthicalArchetype.BUSCADOR_DE_VERDAD] =
                    (archetypeMatchCounts[EthicalArchetype.BUSCADOR_DE_VERDAD] ?: 0) + 1
            }
            if (matchesLiberty) {
                archetypeMatchCounts[EthicalArchetype.EXPLORADOR_DE_LIBERTAD] =
                    (archetypeMatchCounts[EthicalArchetype.EXPLORADOR_DE_LIBERTAD] ?: 0) + 1
            }
            if (matchesJustice) {
                archetypeMatchCounts[EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA] =
                    (archetypeMatchCounts[EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA] ?: 0) + 1
            }
            if (matchesCommunity) {
                archetypeMatchCounts[EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD] =
                    (archetypeMatchCounts[EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD] ?: 0) + 1
            }

            // Verificar si el turno fue fiel a alguno de los arquetipos presentes en el grupo
            val matchedPartyArchetype =
                (matchesDignity && EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD in partyArchetypes) ||
                (matchesTruth && EthicalArchetype.BUSCADOR_DE_VERDAD in partyArchetypes) ||
                (matchesLiberty && EthicalArchetype.EXPLORADOR_DE_LIBERTAD in partyArchetypes) ||
                (matchesJustice && EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA in partyArchetypes) ||
                (matchesCommunity && EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD in partyArchetypes)

            if (matchedPartyArchetype) {
                coherentTurnCount++
            }
        }

        val totalTurns = storyLogs.size.coerceAtLeast(1)
        val overallCoherenceRatio = ((coherentTurnCount.toFloat() / totalTurns) * 100f).toInt().coerceIn(25, 100)

        val results = EthicalMedal.entries.map { medal ->
            when (medal) {
                EthicalMedal.EL_INQUEBRANTABLE -> {
                    val hasArchetype = EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD in partyArchetypes
                    val dignityMatches = archetypeMatchCounts[EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD] ?: 0
                    val score = moralScores.compassion
                    val unlocked = score >= 60 || (hasArchetype && dignityMatches >= 1 && score >= 52)
                    val pct = maxOf(score, if (hasArchetype) 55 + dignityMatches * 18 else score).coerceIn(15, 100)
                    val heroNames = players.filter { it.archetype == EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD }
                        .joinToString(", ") { it.name }
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (unlocked) {
                            if (heroNames.isNotBlank()) {
                                "Desbloqueado por $heroNames al sostener la Dignidad Humana ($score% en Brújula Moral)."
                            } else {
                                "Desbloqueado por el grupo al priorizar la Dignidad Humana ($score%)."
                            }
                        } else {
                            "Requiere priorizar la Dignidad Humana (≥60% o consistencia con Defensor de la Dignidad)."
                        }
                    )
                }

                EthicalMedal.ORACULO_SIN_VELOS -> {
                    val hasArchetype = EthicalArchetype.BUSCADOR_DE_VERDAD in partyArchetypes
                    val truthMatches = archetypeMatchCounts[EthicalArchetype.BUSCADOR_DE_VERDAD] ?: 0
                    val score = moralScores.pragmatism
                    val unlocked = score >= 58 || (hasArchetype && truthMatches >= 1 && score >= 50)
                    val pct = maxOf(score, if (hasArchetype) 52 + truthMatches * 18 else score).coerceIn(15, 100)
                    val heroNames = players.filter { it.archetype == EthicalArchetype.BUSCADOR_DE_VERDAD }
                        .joinToString(", ") { it.name }
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (unlocked) {
                            if (heroNames.isNotBlank()) {
                                "Desbloqueado por $heroNames por su fidelidad a la Verdad y el Sentido ($score%)."
                            } else {
                                "Desbloqueado por iluminar la verdad detrás de cada dilema ($score%)."
                            }
                        } else {
                            "Requiere priorizar el Sentido y la Verdad (≥58% o consistencia con Buscador de Verdad)."
                        }
                    )
                }

                EthicalMedal.ALAS_DEL_ALBEDRIO -> {
                    val hasArchetype = EthicalArchetype.EXPLORADOR_DE_LIBERTAD in partyArchetypes
                    val libertyMatches = archetypeMatchCounts[EthicalArchetype.EXPLORADOR_DE_LIBERTAD] ?: 0
                    val score = moralScores.liberty
                    val unlocked = score >= 60 || (hasArchetype && libertyMatches >= 1 && score >= 52)
                    val pct = maxOf(score, if (hasArchetype) 55 + libertyMatches * 18 else score).coerceIn(15, 100)
                    val heroNames = players.filter { it.archetype == EthicalArchetype.EXPLORADOR_DE_LIBERTAD }
                        .joinToString(", ") { it.name }
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (unlocked) {
                            if (heroNames.isNotBlank()) {
                                "Desbloqueado por $heroNames al defender el libre albedrío responsable ($score%)."
                            } else {
                                "Desbloqueado por defender la Libertad y Autonomía del mundo ($score%)."
                            }
                        } else {
                            "Requiere priorizar la Libertad y Autonomía (≥60% o consistencia con Explorador de Libertad)."
                        }
                    )
                }

                EthicalMedal.BALANZA_INCORRUPTIBLE -> {
                    val hasArchetype = EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA in partyArchetypes
                    val justiceMatches = archetypeMatchCounts[EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA] ?: 0
                    val score = moralScores.justice
                    val unlocked = score >= 60 || (hasArchetype && justiceMatches >= 1 && score >= 52)
                    val pct = maxOf(score, if (hasArchetype) 55 + justiceMatches * 18 else score).coerceIn(15, 100)
                    val heroNames = players.filter { it.archetype == EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA }
                        .joinToString(", ") { it.name }
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (unlocked) {
                            if (heroNames.isNotBlank()) {
                                "Desbloqueado por $heroNames por escuchar siempre la voz de la Justicia ($score%)."
                            } else {
                                "Desbloqueado por sostener la Justicia y la Conciencia recta ($score%)."
                            }
                        } else {
                            "Requiere priorizar la Justicia y Conciencia (≥60% o consistencia con Guardián de la Conciencia)."
                        }
                    )
                }

                EthicalMedal.TEJEDOR_DE_FRATERNIDAD -> {
                    val hasArchetype = EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD in partyArchetypes
                    val commMatches = archetypeMatchCounts[EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD] ?: 0
                    val combinedCommScore = ((moralScores.compassion + moralScores.justice) / 2)
                    val unlocked = combinedCommScore >= 58 || (hasArchetype && commMatches >= 1)
                    val pct = maxOf(combinedCommScore, if (hasArchetype) 56 + commMatches * 18 else combinedCommScore).coerceIn(15, 100)
                    val heroNames = players.filter { it.archetype == EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD }
                        .joinToString(", ") { it.name }
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (unlocked) {
                            if (heroNames.isNotBlank()) {
                                "Desbloqueado por $heroNames al tejer comunidad y reconciliación ($combinedCommScore%)."
                            } else {
                                "Desbloqueado por fortalecer el Bien Común y la Fraternidad ($combinedCommScore%)."
                            }
                        } else {
                            "Requiere priorizar decisiones de solidaridad comunitaria y reconciliación."
                        }
                    )
                }

                EthicalMedal.COHERENCIA_HEROICA_CORE -> {
                    val maxAxis = maxOf(
                        moralScores.justice,
                        moralScores.compassion,
                        moralScores.pragmatism,
                        moralScores.liberty
                    )
                    val unlocked = coherentTurnCount >= 1 || maxAxis >= 56
                    val pct = maxOf(overallCoherenceRatio, maxAxis).coerceIn(40, 100)
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (unlocked) {
                            "El grupo actuó en fiel consonancia con sus Arquetipos Éticos elegidos ($pct% de consistencia)."
                        } else {
                            "Alinea tus decisiones con el Arquetipo Ético elegido al inicio de la partida."
                        }
                    )
                }

                EthicalMedal.RESILIENCIA_ANTE_EL_INFORTUNIO -> {
                    val adverseRolls = storyLogs.count { it.diceRoll in 1..14 }
                    val unlocked = adverseRolls >= 1 || storyLogs.isNotEmpty()
                    val pct = if (adverseRolls > 0) (70 + adverseRolls * 10).coerceAtMost(100) else 65
                    MedalEvaluationResult(
                        medal = medal,
                        isUnlocked = unlocked,
                        consistencyPercent = pct,
                        unlockReasonEs = if (adverseRolls > 0) {
                            "Superasteis $adverseRolls ${if (adverseRolls == 1) "tirada exigente" else "tiradas exigentes"} del D20 asumiendo el costo ético sin rendiros."
                        } else {
                            "Mantuvisteis la honestidad en dados D20 durante toda la travesía."
                        }
                    )
                }
            }
        }

        // Garantizar que al menos la medalla principal del eje ético dominante esté desbloqueada al concluir
        if (results.none { it.isUnlocked }) {
            return results.mapIndexed { idx, item ->
                if (idx == 0) {
                    item.copy(
                        isUnlocked = true,
                        consistencyPercent = 75,
                        unlockReasonEs = "Concedida por completar la deliberación ética de CORE I con honor."
                    )
                } else {
                    item
                }
            }
        }

        return results.sortedByDescending { it.isUnlocked }
    }

    /**
     * Evalúa las medallas a partir de una crónica guardada en Room (`ChronicleEntity`).
     */
    fun evaluateMedalsFromScoresAndLogs(
        partySummary: String,
        storyLogs: List<StoryTurnLog>,
        moralScores: MoralScores
    ): List<MedalEvaluationResult> {
        val inferredPlayers = EthicalArchetype.entries.mapIndexedNotNull { idx, archetype ->
            if (partySummary.contains(archetype.maleForm, ignoreCase = true) ||
                partySummary.contains(archetype.femaleForm, ignoreCase = true)
            ) {
                PlayerHero(
                    index = idx,
                    name = "Héroe CORE",
                    gender = com.example.data.HeroGender.HOMBRE,
                    species = com.example.data.HeroSpecies.HUMANO,
                    archetype = archetype
                )
            } else {
                null
            }
        }
        return evaluateMedals(
            players = inferredPlayers,
            storyLogs = storyLogs,
            moralScores = moralScores
        )
    }
}

/**
 * Medallón heráldico octogonal dibujado en Canvas para cada logro ético.
 */
@Composable
fun HeraldicMedalBadge(
    medal: EthicalMedal,
    isUnlocked: Boolean,
    sizeDp: Int = 56,
    modifier: Modifier = Modifier
) {
    val accent = if (isUnlocked) medal.accentColor() else ParchmentMuted.copy(alpha = 0.45f)

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = size.minDimension / 2f * 0.94f

            val starPath = Path()
            val points = 8
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerRadius else outerRadius * 0.82f
                val angleRad = Math.toRadians((i * 360.0 / (points * 2)) - 90.0)
                val x = cx + (r * cos(angleRad)).toFloat()
                val y = cy + (r * sin(angleRad)).toFloat()
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()

            drawPath(
                path = starPath,
                brush = Brush.radialGradient(
                    colors = if (isUnlocked) {
                        listOf(accent.copy(alpha = 0.42f), ObsidianBackground)
                    } else {
                        listOf(ArcaneSurfaceVariant, ObsidianBackground)
                    }
                )
            )
            drawPath(
                path = starPath,
                color = if (isUnlocked) ImperialGold else ParchmentBorder,
                style = Stroke(width = if (isUnlocked) 4f else 2f)
            )
        }

        Text(
            text = if (isUnlocked) medal.emoji else "🔒",
            fontSize = (sizeDp * 0.36f).sp
        )
    }
}

/**
 * Vitrina de Medallas y Logros Éticos desbloqueados al final de la partida según la consistencia
 * del grupo con su arquetipo ético elegido.
 */
@Composable
fun EthicalAchievementsShowcaseCard(
    evaluations: List<MedalEvaluationResult>,
    showLockedMedals: Boolean = true,
    modifier: Modifier = Modifier
) {
    val unlockedCount = evaluations.count { it.isUnlocked }
    val visibleItems = if (showLockedMedals) evaluations else evaluations.filter { it.isUnlocked }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ethical_achievements_showcase_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
        border = BorderStroke(1.5.dp, ImperialGold.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = ImperialGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Logros Éticos",
                        style = MaterialTheme.typography.titleMedium,
                        color = ImperialGold
                    )
                }

                Surface(
                    color = ImperialGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ImperialGold)
                ) {
                    Text(
                        text = "🏅 $unlockedCount/${evaluations.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ImperialGoldLight,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("unlocked_medals_counter")
                    )
                }
            }

            visibleItems.forEach { item ->
                val medal = item.medal
                val accent = medal.accentColor()
                val animatedProgress by animateFloatAsState(
                    targetValue = (item.consistencyPercent / 100f).coerceIn(0.08f, 1f),
                    animationSpec = spring(),
                    label = "medal_consistency_${medal.id}"
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (item.isUnlocked) {
                        ArcaneCardElevated.copy(alpha = 0.95f)
                    } else {
                        ObsidianBackground.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        width = if (item.isUnlocked) 1.5.dp else 1.dp,
                        color = if (item.isUnlocked) ImperialGold.copy(alpha = 0.75f) else ParchmentBorder.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("achievement_medal_card_${medal.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeraldicMedalBadge(
                            medal = medal,
                            isUnlocked = item.isUnlocked,
                            sizeDp = 46
                        )

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
                                    text = medal.titleEs,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isUnlocked) ImperialGoldLight else ParchmentMuted
                                )

                                Text(
                                    text = "${item.consistencyPercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isUnlocked) accent else ParchmentMuted
                                )
                            }

                            Text(
                                text = medal.subtitleEs,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isUnlocked) TorchAmber else ParchmentMuted
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianBackground)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedProgress)
                                        .height(5.dp)
                                        .clip(CircleShape)
                                        .background(if (item.isUnlocked) accent else ParchmentBorder)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

