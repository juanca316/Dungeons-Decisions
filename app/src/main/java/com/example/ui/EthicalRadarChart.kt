package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MoralScores
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Los 4 valores éticos fundamentales evaluados en el gráfico de radar estilo Recharts:
 * - Justicia (Conciencia y Rectitud)
 * - Compasión (Dignidad y Comunidad)
 * - Deber (Responsabilidad Moral y Pragmatismo)
 * - Libertad (Autonomía y Libre Albedrío)
 */
enum class EthicalRadarAxis(
    val titleEs: String,
    val shortLabel: String,
    val emoji: String,
    val color: Color,
    val coreConceptEs: String
) {
    JUSTICIA(
        titleEs = "Justicia",
        shortLabel = "Justicia",
        emoji = "⚖️",
        color = SapphireOrder,
        coreConceptEs = "Rectitud moral, equidad y fidelidad a la conciencia interior."
    ),
    COMPASION(
        titleEs = "Compasión",
        shortLabel = "Compasión",
        emoji = "💚",
        color = EmeraldVirtue,
        coreConceptEs = "Dignidad humana incondicional, empatía y cuidado mutuo."
    ),
    DEBER(
        titleEs = "Deber",
        shortLabel = "Deber",
        emoji = "🛡️",
        color = AmethystWisdom,
        coreConceptEs = "Responsabilidad asumida, sentido de vida y prudencia práctica."
    ),
    LIBERTAD(
        titleEs = "Libertad",
        shortLabel = "Libertad",
        emoji = "🦅",
        color = TorchAmber,
        coreConceptEs = "Autonomía consciente, libre albedrío y valentía para elegir."
    );

    fun getValue(scores: MoralScores): Int = when (this) {
        JUSTICIA -> scores.justice
        COMPASION -> scores.compassion
        DEBER -> scores.pragmatism
        LIBERTAD -> scores.liberty
    }
}

/**
 * Gráfico de radar en tiempo real estilo Recharts (RadarChart / PolarGrid / Radar / PolarAngleAxis)
 * con soporte interactivo para la evolución histórica de valores éticos a lo largo de la campaña.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EthicalRadarChart(
    currentScores: MoralScores,
    scoresHistory: List<MoralScores> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Normalizar historial: si está vacío, crear el punto base inicial (50%) y el actual
    val safeHistory = remember(scoresHistory, currentScores) {
        if (scoresHistory.isEmpty()) {
            listOf(MoralScores(50, 50, 50, 50), currentScores)
        } else if (scoresHistory.size == 1 && scoresHistory.first() != currentScores) {
            scoresHistory + currentScores
        } else {
            scoresHistory
        }
    }

    var selectedTurnIndex by remember(safeHistory.size) {
        mutableIntStateOf(safeHistory.lastIndex)
    }

    // Modo reproducción automática de la evolución
    var isPlayingEvolution by remember { mutableStateOf(false) }
    var selectedAxisDetail by remember { mutableStateOf<EthicalRadarAxis?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Manejar animación paso a paso
    LaunchedEffect(isPlayingEvolution) {
        if (isPlayingEvolution) {
            for (idx in safeHistory.indices) {
                selectedTurnIndex = idx
                delay(750)
            }
            isPlayingEvolution = false
        }
    }

    val displayScores = safeHistory.getOrElse(selectedTurnIndex) { currentScores }
    val baselineScores = safeHistory.firstOrNull() ?: MoralScores(50, 50, 50, 50)
    val previousTurnScores = if (selectedTurnIndex > 0) {
        safeHistory.getOrElse(selectedTurnIndex - 1) { baselineScores }
    } else {
        displayScores
    }
    val turnTriggerKey = safeHistory.size * 100 + selectedTurnIndex

    // Valores animados con transición suave estilo Recharts
    val animatedJustice by animateFloatAsState(
        targetValue = displayScores.justice.toFloat(),
        animationSpec = spring(stiffness = 500f),
        label = "radar_justice"
    )
    val animatedCompassion by animateFloatAsState(
        targetValue = displayScores.compassion.toFloat(),
        animationSpec = spring(stiffness = 500f),
        label = "radar_compassion"
    )
    val animatedDeber by animateFloatAsState(
        targetValue = displayScores.pragmatism.toFloat(),
        animationSpec = spring(stiffness = 500f),
        label = "radar_deber"
    )
    val animatedLiberty by animateFloatAsState(
        targetValue = displayScores.liberty.toFloat(),
        animationSpec = spring(stiffness = 500f),
        label = "radar_liberty"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_radar_chart_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
        border = BorderStroke(1.5.dp, ImperialGold.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Encabezado del Gráfico de Radar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ImperialGold.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, ImperialGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = ImperialGold,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Radar Ético en Tiempo Real",
                                style = MaterialTheme.typography.titleMedium,
                                color = ImperialGoldLight
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ObsidianBackground.copy(alpha = 0.8f),
                                border = BorderStroke(0.8.dp, ImperialGold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "Recharts Re-Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ImperialGoldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Justicia • Compasión • Deber • Libertad",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentMuted
                        )
                    }
                }

                // Indicador de Turno seleccionado
                Surface(
                    color = ImperialGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = if (selectedTurnIndex == 0) "T0: Inicial (50%)" else "Turno #$selectedTurnIndex",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGoldLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Lienzo interactivo del Gráfico de Radar (Canvas Polar Grid + Polígono sombreado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarCanvas(
                    justice = animatedJustice,
                    compassion = animatedCompassion,
                    deber = animatedDeber,
                    liberty = animatedLiberty,
                    baseline = baselineScores,
                    onAxisClick = { axis ->
                        selectedAxisDetail = if (selectedAxisDetail == axis) null else axis
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Etiquetas de Vértices (PolarAngleAxis) posicionadas en los extremos
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    // Norte: Justicia
                    RadarAxisBadge(
                        axis = EthicalRadarAxis.JUSTICIA,
                        value = displayScores.justice,
                        delta = displayScores.justice - previousTurnScores.justice,
                        turnKey = turnTriggerKey,
                        isSelected = selectedAxisDetail == EthicalRadarAxis.JUSTICIA,
                        onClick = {
                            selectedAxisDetail = if (selectedAxisDetail == EthicalRadarAxis.JUSTICIA) null else EthicalRadarAxis.JUSTICIA
                        },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    // Este: Compasión
                    RadarAxisBadge(
                        axis = EthicalRadarAxis.COMPASION,
                        value = displayScores.compassion,
                        delta = displayScores.compassion - previousTurnScores.compassion,
                        turnKey = turnTriggerKey,
                        isSelected = selectedAxisDetail == EthicalRadarAxis.COMPASION,
                        onClick = {
                            selectedAxisDetail = if (selectedAxisDetail == EthicalRadarAxis.COMPASION) null else EthicalRadarAxis.COMPASION
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )

                    // Sur: Deber
                    RadarAxisBadge(
                        axis = EthicalRadarAxis.DEBER,
                        value = displayScores.pragmatism,
                        delta = displayScores.pragmatism - previousTurnScores.pragmatism,
                        turnKey = turnTriggerKey,
                        isSelected = selectedAxisDetail == EthicalRadarAxis.DEBER,
                        onClick = {
                            selectedAxisDetail = if (selectedAxisDetail == EthicalRadarAxis.DEBER) null else EthicalRadarAxis.DEBER
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    // Oeste: Libertad
                    RadarAxisBadge(
                        axis = EthicalRadarAxis.LIBERTAD,
                        value = displayScores.liberty,
                        delta = displayScores.liberty - previousTurnScores.liberty,
                        turnKey = turnTriggerKey,
                        isSelected = selectedAxisDetail == EthicalRadarAxis.LIBERTAD,
                        onClick = {
                            selectedAxisDetail = if (selectedAxisDetail == EthicalRadarAxis.LIBERTAD) null else EthicalRadarAxis.LIBERTAD
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }

            // Detalle interactivo del eje seleccionado (Tooltip explicativo)
            AnimatedVisibility(visible = selectedAxisDetail != null) {
                selectedAxisDetail?.let { axis ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianBackground.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, axis.color.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = axis.emoji, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = axis.titleEs,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = axis.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val netChange = axis.getValue(displayScores) - axis.getValue(baselineScores)
                                    val sign = if (netChange >= 0) "+$netChange%" else "$netChange%"
                                    Text(
                                        text = "(${axis.getValue(displayScores)}% • $sign vs inicio)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ImperialGoldLight
                                    )
                                }
                                Text(
                                    text = axis.coreConceptEs,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentLight
                                )
                            }
                        }
                    }
                }
            }

            // Timeline de evolución si hay más de 1 punto en el historial
            if (safeHistory.size > 1) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TorchAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Línea Temporal de la Campaña (${safeHistory.size} estados):",
                                style = MaterialTheme.typography.labelMedium,
                                color = ImperialGoldLight
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isPlayingEvolution) {
                                    isPlayingEvolution = false
                                } else {
                                    coroutineScope.launch {
                                        isPlayingEvolution = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ImperialGold.copy(alpha = 0.2f))
                                .testTag("play_radar_evolution_button")
                        ) {
                            Icon(
                                imageVector = if (isPlayingEvolution) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Reproducir evolución",
                                tint = ImperialGoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(safeHistory) { idx, _ ->
                            val isSelected = idx == selectedTurnIndex
                            val label = when (idx) {
                                0 -> "Inicio"
                                safeHistory.lastIndex -> "Actual (#$idx)"
                                else -> "T#$idx"
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isPlayingEvolution = false
                                    selectedTurnIndex = idx
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ImperialGold.copy(alpha = 0.28f),
                                    selectedLabelColor = ImperialGoldLight,
                                    containerColor = ObsidianBackground.copy(alpha = 0.6f),
                                    labelColor = ParchmentMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = ParchmentBorder,
                                    selectedBorderColor = ImperialGold
                                ),
                                modifier = Modifier.testTag("radar_turn_chip_$idx")
                            )
                        }
                    }
                }
            }

            // Leyenda de métricas comparativas estilo Recharts Legend
            Surface(
                color = ObsidianBackground.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("Justicia", displayScores.justice, SapphireOrder)
                    LegendItem("Compasión", displayScores.compassion, EmeraldVirtue)
                    LegendItem("Deber", displayScores.pragmatism, AmethystWisdom)
                    LegendItem("Libertad", displayScores.liberty, TorchAmber)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentMuted
            )
        }
        Text(
            text = "$score%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Chip interactivo posicionado alrededor del gráfico de radar para cada vértice
 */
@Composable
private fun RadarAxisBadge(
    axis: EthicalRadarAxis,
    value: Int,
    delta: Int,
    turnKey: Int = 0,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flashState = rememberStatFlashState(
        score = value,
        historyDelta = delta,
        turnTriggerKey = turnKey
    )
    val isFlashing = flashState.flashAlpha > 0.04f

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = when {
            isFlashing -> axis.color.copy(alpha = (0.3f + 0.45f * flashState.flashAlpha).coerceAtMost(0.8f))
            isSelected -> axis.color.copy(alpha = 0.25f)
            else -> ObsidianBackground.copy(alpha = 0.88f)
        },
        border = BorderStroke(
            width = if (isFlashing) (1.5f + 1.5f * flashState.flashAlpha).dp else if (isSelected) 1.5.dp else 1.dp,
            color = when {
                isFlashing -> ImperialGoldLight
                isSelected -> axis.color
                else -> ParchmentBorder
            }
        ),
        modifier = modifier
            .testTag("radar_axis_badge_${axis.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = flashState.scale
                    scaleY = flashState.scale
                }
            ) {
                Text(text = axis.emoji, fontSize = 13.sp)
            }
            Text(
                text = "${axis.shortLabel}: $value%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected || isFlashing) ImperialGoldLight else ParchmentLight
            )
            if (delta != 0) {
                val deltaSign = if (delta > 0) "+$delta" else "$delta"
                Text(
                    text = deltaSign,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (delta > 0) EmeraldVirtue else CrimsonChaos
                )
            }
        }
    }
}

/**
 * Renderizado en Canvas del gráfico de radar:
 * - Telaraña concéntrica estilo Recharts PolarGrid (25%, 50%, 75%, 100%)
 * - Ejes cardinales radiales
 * - Polígono base punteado de inicio (50%)
 * - Polígono dinámico relleno con gradiente radial y borde dorado
 * - Nodos brillantes en cada vértice
 */
@Composable
private fun RadarCanvas(
    justice: Float,
    compassion: Float,
    deber: Float,
    liberty: Float,
    baseline: MoralScores,
    onAxisClick: (EthicalRadarAxis) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        // Radio máximo dejando margen para las etiquetas exteriores
        val maxRadius = (minOf(size.width, size.height) / 2f) * 0.72f

        // 1. Dibujar cuadrícula polar concéntrica (Recharts PolarGrid) en 25%, 50%, 75%, 100%
        val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        gridLevels.forEach { level ->
            val r = maxRadius * level
            val gridPath = Path().apply {
                moveTo(cx, cy - r)         // Norte
                lineTo(cx + r, cy)         // Este
                lineTo(cx, cy + r)         // Sur
                lineTo(cx - r, cy)         // Oeste
                close()
            }

            // Relleno sutil para nivel 100%
            if (level == 1.0f) {
                drawPath(
                    path = gridPath,
                    color = ArcaneSurfaceVariant.copy(alpha = 0.25f)
                )
            }

            // Línea de cuadrícula
            drawPath(
                path = gridPath,
                color = if (level == 0.50f) ImperialGold.copy(alpha = 0.35f) else ParchmentBorder.copy(alpha = 0.35f),
                style = Stroke(
                    width = if (level == 0.50f) 1.5f else 1f,
                    pathEffect = if (level == 0.50f) PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) else null
                )
            )
        }

        // 2. Líneas de Ejes Radiales (PolarRadiusAxis)
        drawLine(
            color = SapphireOrder.copy(alpha = 0.4f),
            start = Offset(cx, cy),
            end = Offset(cx, cy - maxRadius),
            strokeWidth = 1.5f
        )
        drawLine(
            color = EmeraldVirtue.copy(alpha = 0.4f),
            start = Offset(cx, cy),
            end = Offset(cx + maxRadius, cy),
            strokeWidth = 1.5f
        )
        drawLine(
            color = AmethystWisdom.copy(alpha = 0.4f),
            start = Offset(cx, cy),
            end = Offset(cx, cy + maxRadius),
            strokeWidth = 1.5f
        )
        drawLine(
            color = TorchAmber.copy(alpha = 0.4f),
            start = Offset(cx, cy),
            end = Offset(cx - maxRadius, cy),
            strokeWidth = 1.5f
        )

        // 3. Polígono de Base de Inicio (T0: 50/50/50/50 o baseline) trazado tenue punteado
        val baseJ = (baseline.justice / 100f).coerceIn(0.05f, 1f) * maxRadius
        val baseC = (baseline.compassion / 100f).coerceIn(0.05f, 1f) * maxRadius
        val baseD = (baseline.pragmatism / 100f).coerceIn(0.05f, 1f) * maxRadius
        val baseL = (baseline.liberty / 100f).coerceIn(0.05f, 1f) * maxRadius

        val baselinePath = Path().apply {
            moveTo(cx, cy - baseJ)
            lineTo(cx + baseC, cy)
            lineTo(cx, cy + baseD)
            lineTo(cx - baseL, cy)
            close()
        }
        drawPath(
            path = baselinePath,
            color = ParchmentMuted.copy(alpha = 0.3f),
            style = Stroke(
                width = 1.8f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
        )

        // 4. Polígono Dinámico en Tiempo Real (Recharts Radar Fill & Stroke)
        val rJ = (justice / 100f).coerceIn(0.05f, 1f) * maxRadius
        val rC = (compassion / 100f).coerceIn(0.05f, 1f) * maxRadius
        val rD = (deber / 100f).coerceIn(0.05f, 1f) * maxRadius
        val rL = (liberty / 100f).coerceIn(0.05f, 1f) * maxRadius

        val pNorth = Offset(cx, cy - rJ)
        val pEast = Offset(cx + rC, cy)
        val pSouth = Offset(cx, cy + rD)
        val pWest = Offset(cx - rL, cy)

        val radarPath = Path().apply {
            moveTo(pNorth.x, pNorth.y)
            lineTo(pEast.x, pEast.y)
            lineTo(pSouth.x, pSouth.y)
            lineTo(pWest.x, pWest.y)
            close()
        }

        // Relleno degradado translúcido con iluminación dorada/esmeralda
        drawPath(
            path = radarPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    ImperialGold.copy(alpha = 0.45f),
                    TorchAmber.copy(alpha = 0.28f),
                    ArcaneCardElevated.copy(alpha = 0.15f)
                ),
                center = Offset(cx, cy),
                radius = maxRadius
            )
        )

        // Trazo perimetral con brillo imperial
        drawPath(
            path = radarPath,
            color = ImperialGoldLight,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 5. Nodos brillantes en cada vértice
        drawRadarNode(pNorth, SapphireOrder)
        drawRadarNode(pEast, EmeraldVirtue)
        drawRadarNode(pSouth, AmethystWisdom)
        drawRadarNode(pWest, TorchAmber)

        // Centro del radar
        drawCircle(
            color = ImperialGold,
            radius = 3.5.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

private fun DrawScope.drawRadarNode(center: Offset, accentColor: Color) {
    // Halo exterior brillante
    drawCircle(
        color = accentColor.copy(alpha = 0.35f),
        radius = 8.dp.toPx(),
        center = center
    )
    // Círculo central
    drawCircle(
        color = ImperialGoldLight,
        radius = 4.5.dp.toPx(),
        center = center
    )
    // Borde de acento
    drawCircle(
        color = accentColor,
        radius = 4.5.dp.toPx(),
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
}
