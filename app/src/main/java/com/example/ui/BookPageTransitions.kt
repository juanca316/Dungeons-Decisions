package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.DmTurnPayload
import com.example.ui.theme.ArcaneSurface
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.TorchAmber

enum class NarrativeTransitionStyle(
    val labelEs: String,
    val subtitleEs: String,
    val badgeEmoji: String
) {
    PAGE_CURL(
        labelEs = "Page Curl (Grimorio)",
        subtitleEs = "Giro tridimensional de página con pliegue de pergamino y sombra en el lomo",
        badgeEmoji = "📖"
    ),
    CROSS_FADE(
        labelEs = "Cross-Fade (Arcano)",
        subtitleEs = "Fundido cruzado suave entre capítulos narrativos",
        badgeEmoji = "✨"
    )
}

/**
 * Estado inmutable de una página del libro de rol para impulsar las transiciones animadas entre turnos.
 */
data class NarrativeBookPageState(
    val turnNumber: Int,
    val isGeneratingTurn: Boolean,
    val payload: DmTurnPayload?,
    val activeHeroIdentity: String
)

/**
 * Selector interactivo para alternar entre las transiciones 'Page Curl (Grimorio)' y 'Cross-Fade (Arcano)'.
 */
@Composable
fun NarrativeTransitionStyleSelector(
    selectedStyle: NarrativeTransitionStyle,
    currentTurnNumber: Int,
    onSelectStyle: (NarrativeTransitionStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ArcaneSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("narrative_transition_selector")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = ImperialGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Transición del Libro de Rol",
                        style = MaterialTheme.typography.labelLarge,
                        color = ImperialGoldLight
                    )
                }

                Surface(
                    color = ObsidianBackground.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = TorchAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Página #$currentTurnNumber",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentLight,
                            modifier = Modifier.testTag("book_page_indicator")
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NarrativeTransitionStyle.entries.forEach { style ->
                    val isSelected = style == selectedStyle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectStyle(style) },
                        label = {
                            Text(
                                text = "${style.badgeEmoji} ${style.labelEs}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ImperialGold.copy(alpha = 0.26f),
                            selectedLabelColor = ImperialGoldLight,
                            containerColor = ObsidianBackground.copy(alpha = 0.65f),
                            labelColor = ParchmentMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ParchmentBorder,
                            selectedBorderColor = ImperialGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("transition_style_chip_${style.name.lowercase()}")
                    )
                }
            }
        }
    }
}

/**
 * Contenedor animado de páginas de libro de rol que envuelve cada turno narrativo usando
 * Jetpack Compose Animation (`AnimatedContent` con efecto 3D Page Curl + pliegue de pergamino en Canvas
 * o `Crossfade` suave entre capítulos).
 */
@Composable
fun RoleplayBookPageTransitionContainer(
    pageState: NarrativeBookPageState,
    transitionStyle: NarrativeTransitionStyle,
    modifier: Modifier = Modifier,
    pageContent: @Composable (NarrativeBookPageState) -> Unit
) {
    // Animación de pliegue de página ("Page Curl") que se ejecuta cada vez que cambia el turno o llega el relato del DM
    val curlProgress = remember { Animatable(1f) }

    LaunchedEffect(pageState.turnNumber, pageState.isGeneratingTurn, transitionStyle) {
        if (transitionStyle == NarrativeTransitionStyle.PAGE_CURL) {
            curlProgress.snapTo(0f)
            curlProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 680,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            curlProgress.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("roleplay_book_page_transition_container")
    ) {
        when (transitionStyle) {
            NarrativeTransitionStyle.CROSS_FADE -> {
                Crossfade(
                    targetState = pageState,
                    animationSpec = tween(
                        durationMillis = 560,
                        easing = FastOutSlowInEasing
                    ),
                    label = "narrative_turn_crossfade"
                ) { currentPage ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBookSpineAccents()
                    ) {
                        pageContent(currentPage)
                    }
                }
            }

            NarrativeTransitionStyle.PAGE_CURL -> {
                AnimatedContent(
                    targetState = pageState,
                    transitionSpec = {
                        val isAdvancing = targetState.turnNumber >= initialState.turnNumber
                        val enterTransition = slideInHorizontally(
                            animationSpec = tween(640, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> if (isAdvancing) fullWidth / 3 else -fullWidth / 3 }
                        ) + fadeIn(
                            animationSpec = tween(480, easing = FastOutSlowInEasing)
                        ) + scaleIn(
                            initialScale = 0.94f,
                            transformOrigin = TransformOrigin(if (isAdvancing) 0f else 1f, 0.5f),
                            animationSpec = tween(640, easing = FastOutSlowInEasing)
                        )

                        val exitTransition = slideOutHorizontally(
                            animationSpec = tween(560, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> if (isAdvancing) -fullWidth / 2 else fullWidth / 2 }
                        ) + fadeOut(
                            animationSpec = tween(440, easing = FastOutSlowInEasing)
                        )

                        (enterTransition togetherWith exitTransition).using(
                            SizeTransform(clip = false)
                        )
                    },
                    label = "narrative_turn_page_curl"
                ) { currentPage ->
                    val progress = curlProgress.value
                    // Ángulo 3D desde el lomo izquierdo del grimorio (simula la hoja posándose al pasar de página)
                    val rotationYDegrees = (1f - progress) * -26f
                    val pageElevationScale = 0.96f + (0.04f * progress)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                rotationY = rotationYDegrees
                                scaleX = pageElevationScale
                                scaleY = pageElevationScale
                                cameraDistance = 18f * density
                            }
                            .drawWithContent {
                                drawContent()
                                drawGrimoireSpineAndPageCurlEffect(curlProgress = progress)
                            }
                    ) {
                        pageContent(currentPage)
                    }
                }
            }
        }
    }
}

/**
 * Dibuja los detalles sutiles del lomo del libro en modo Cross-Fade.
 */
private fun Modifier.drawBookSpineAccents(): Modifier = this.drawWithContent {
    drawContent()
    val spineWidth = 14.dp.toPx()
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                ObsidianBackground.copy(alpha = 0.55f),
                ImperialGold.copy(alpha = 0.14f),
                Color.Transparent
            ),
            startX = 0f,
            endX = spineWidth
        ),
        topLeft = Offset.Zero,
        size = Size(spineWidth, size.height)
    )
}

/**
 * Dibuja el sombreado del lomo del grimorio y el pliegue triangular dinámico ('Page Curl')
 * en la esquina inferior derecha mientras la página se despliega.
 */
private fun DrawScope.drawGrimoireSpineAndPageCurlEffect(curlProgress: Float) {
    val spineWidth = 18.dp.toPx()

    // 1. Sombra de encuadernación en el lomo izquierdo del libro de rol
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                ObsidianBackground.copy(alpha = 0.68f),
                ImperialGold.copy(alpha = 0.18f),
                Color.Transparent
            ),
            startX = 0f,
            endX = spineWidth
        ),
        topLeft = Offset.Zero,
        size = Size(spineWidth, size.height)
    )

    // 2. Pliegue de esquina ('Page Curl') animado durante la transición y sutil marca de esquina de pergamino en reposo
    val activeCurlFactor = (1f - curlProgress).coerceIn(0f, 1f)
    val baseCornerDp = 22.dp.toPx()
    val dynamicCurlSize = baseCornerDp + (activeCurlFactor * 92.dp.toPx())

    val w = size.width
    val h = size.height
    if (w <= dynamicCurlSize * 1.5f || h <= dynamicCurlSize * 1.5f) return

    // Sombra proyectada bajo el pliegue de la hoja
    val shadowSize = dynamicCurlSize * 1.18f
    val shadowPath = Path().apply {
        moveTo(w - shadowSize, h)
        lineTo(w, h - shadowSize)
        lineTo(w, h)
        close()
    }
    drawPath(
        path = shadowPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                ObsidianBackground.copy(alpha = 0.45f + activeCurlFactor * 0.35f)
            ),
            start = Offset(w - shadowSize, h - shadowSize),
            end = Offset(w, h)
        )
    )

    // Reverso del pergamino plegado (triángulo del Page Curl)
    val curlFlapPath = Path().apply {
        moveTo(w - dynamicCurlSize, h)
        lineTo(w - dynamicCurlSize * 0.18f, h - dynamicCurlSize * 0.18f)
        lineTo(w, h - dynamicCurlSize)
        close()
    }

    drawPath(
        path = curlFlapPath,
        brush = Brush.linearGradient(
            colors = listOf(
                ParchmentLight.copy(alpha = 0.32f + activeCurlFactor * 0.42f),
                ImperialGold.copy(alpha = 0.48f + activeCurlFactor * 0.35f),
                TorchAmber.copy(alpha = 0.28f)
            ),
            start = Offset(w - dynamicCurlSize, h - dynamicCurlSize),
            end = Offset(w, h)
        )
    )

    drawPath(
        path = curlFlapPath,
        color = ImperialGold.copy(alpha = 0.75f),
        style = Stroke(width = 1.5.dp.toPx())
    )
}
