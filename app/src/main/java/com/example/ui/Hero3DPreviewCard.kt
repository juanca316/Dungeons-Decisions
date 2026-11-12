package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EthicalArchetype
import com.example.data.HeroGender
import com.example.data.HeroSpecies
import com.example.data.PlayerHero
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Punto 3D en el espacio del modelo del héroe.
 */
private data class Vec3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)

    fun cross(other: Vec3) = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    fun normalized(): Vec3 {
        val len = sqrt(x * x + y * y + z * z)
        return if (len > 1e-5f) Vec3(x / len, y / len, z / len) else Vec3(0f, 0f, 1f)
    }
}

/**
 * Polígono 3D con material PBR simplificado (color base, metalness, roughness, emisividad).
 */
private data class Face3D(
    val vertices: List<Vec3>,
    val baseColor: Color,
    val metallic: Float = 0.2f,
    val roughness: Float = 0.5f,
    val emissiveStrength: Float = 0f,
    val strokeColor: Color? = null
)

private data class ProjectedFace(
    val points2D: List<Offset>,
    val depthZ: Float,
    val shadedColor: Color,
    val strokeColor: Color?
)

/**
 * Visor 3D Interactivo de Miniatura Heroica sobre Pedestal Rúnico.
 *
 * Permite rotar 360° con gesto táctil libre + giro automático suave (turntable),
 * zoom de cuerpo completo o detalle, y refleja en tiempo real la silueta física de la Raza
 * (Estatura, complexión, orejas élficas, barba enana, colmillos orcos) y el equipamiento 3D
 * de la Vocación Ética (Báculo + Orbe, Escudo + Espada, Dagas + Capa Alada, Mandoble + Balanza,
 * Martillo de Forja + Linterna).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Hero3DPreviewStage(
    hero: PlayerHero,
    modifier: Modifier = Modifier
) {
    var userYawDeg by remember { mutableFloatStateOf(-22f) }
    var userPitchDeg by remember { mutableFloatStateOf(12f) }
    var isAutoRotating by remember { mutableStateOf(true) }
    var isDragging by remember { mutableStateOf(false) }
    var isZoomedIn by remember { mutableStateOf(false) }

    // Pulso reactivo cuando el jugador cambia Especie, Vocación, Género o Color de Emblema
    val morphPulse = remember { Animatable(0f) }
    LaunchedEffect(hero.species, hero.archetype, hero.gender, hero.emblemColorHex) {
        morphPulse.snapTo(1f)
        morphPulse.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hero_3d_turntable")
    val turntableAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "turntable_angle"
    )

    val idleBreath by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breath"
    )

    val zoomScale by animateFloatAsState(
        targetValue = if (isZoomedIn) 1.28f else 1.0f,
        animationSpec = spring(),
        label = "camera_zoom_scale"
    )

    val effectiveYawDeg = if (isAutoRotating && !isDragging) {
        (userYawDeg + turntableAngleDeg) % 360f
    } else {
        userYawDeg
    }

    val emblemColor = Color(hero.emblemColorHex)
    val vocationAccentColor = when (hero.archetype) {
        EthicalArchetype.BUSCADOR_DE_VERDAD -> AmethystWisdom
        EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> EmeraldVirtue
        EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> SapphireOrder
        EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> ImperialGold
        EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> TorchAmber
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ObsidianBackground,
        border = BorderStroke(
            width = (1.5f + morphPulse.value * 1.5f).dp,
            color = if (morphPulse.value > 0.1f) ImperialGoldLight else emblemColor.copy(alpha = 0.75f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_3d_preview_stage")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lienzo 3D Interactivo (100% ancho de la tarjeta con dock flotante compacto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(268.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                emblemColor.copy(alpha = 0.22f + 0.18f * morphPulse.value),
                                ArcaneSurfaceVariant.copy(alpha = 0.65f),
                                ObsidianBackground
                            )
                        )
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                userYawDeg = (userYawDeg + dragAmount.x * 0.65f) % 360f
                                userPitchDeg = (userPitchDeg - dragAmount.y * 0.35f).coerceIn(-18f, 34f)
                            }
                        )
                    }
                    .testTag("hero_3d_interactive_canvas")
            ) {
                // Escena 3D Multi-Parte proyectada en tiempo real
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawHero3DScene(
                        hero = hero,
                        yawDeg = effectiveYawDeg,
                        pitchDeg = userPitchDeg,
                        zoom = zoomScale,
                        breathPhase = idleBreath,
                        morphFlash = morphPulse.value,
                        emblemColor = emblemColor,
                        vocationColor = vocationAccentColor
                    )
                }

                // Etiqueta superior izquierda: Indicador 3D + Instrucción táctil
                Surface(
                    color = ObsidianBackground.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = ImperialGoldLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "VISOR 3D • Arrastra 360°",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = ImperialGoldLight
                        )
                    }
                }

                // Controles flotantes compactos en esquina superior derecha (Auto-giro, Zoom, Reset)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { isAutoRotating = !isAutoRotating },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ObsidianBackground.copy(alpha = 0.85f))
                            .testTag("hero_3d_toggle_autorotate_button")
                    ) {
                        Icon(
                            imageVector = if (isAutoRotating) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoRotating) "Pausar giro 3D" else "Reanudar giro 3D",
                            tint = if (isAutoRotating) EmeraldVirtue else ParchmentLight,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = { isZoomedIn = !isZoomedIn },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ObsidianBackground.copy(alpha = 0.85f))
                            .testTag("hero_3d_toggle_zoom_button")
                    ) {
                        Icon(
                            imageVector = if (isZoomedIn) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = "Alternar zoom 3D",
                            tint = ImperialGoldLight,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            userYawDeg = -18f
                            userPitchDeg = 12f
                            isZoomedIn = false
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ObsidianBackground.copy(alpha = 0.85f))
                            .testTag("hero_3d_reset_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Centrar cámara 3D",
                            tint = TorchAmber,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Insignia inferior flotante con el resumen de Malla 3D Activa (Raza + Vocación)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ObsidianBackground.copy(alpha = 0.86f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, emblemColor.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "${hero.species.emoji} Silueta: ${speciesMorphDescriptorEs(hero.species, hero.gender)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ParchmentLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = ObsidianBackground.copy(alpha = 0.86f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, vocationAccentColor.copy(alpha = 0.75f))
                    ) {
                        Text(
                            text = "${hero.archetype.emoji} ${vocationGearDescriptorEs(hero.archetype)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = vocationAccentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Barra inferior de desglose de componentes visuales 3D activos
            Surface(
                color = ArcaneSurfaceVariant.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PartBadgePill("🪨 Pedestal Rúnico Nórdico", ImperialGold)
                    PartBadgePill("🧬 ${speciesFeatureBadgeEs(hero.species, hero.gender)}", SapphireOrder)
                    PartBadgePill("⚔️ ${vocationWeaponBadgeEs(hero.archetype)}", TorchAmber)
                    PartBadgePill("✨ ${vocationAuraBadgeEs(hero.archetype)}", vocationAccentColor)
                }
            }
        }
    }
}

@Composable
private fun PartBadgePill(text: String, color: Color) {
    Surface(
        color = ObsidianBackground.copy(alpha = 0.72f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.55f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.5.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun speciesMorphDescriptorEs(species: HeroSpecies, gender: HeroGender): String = when (species) {
    HeroSpecies.HUMANO -> "Porte Equilibrado (${gender.labelEs})"
    HeroSpecies.ELFO -> "Esbelta + Orejas Élficas"
    HeroSpecies.ENANO -> "Robusta + Forja Enana"
    HeroSpecies.ORCO -> "Colosal + Colmillos"
    HeroSpecies.MEDIO_ELFO -> "Ágil + Rasgos Semiélficos"
}

private fun speciesFeatureBadgeEs(species: HeroSpecies, gender: HeroGender): String = when (species) {
    HeroSpecies.HUMANO -> "Corona/Diadema Real"
    HeroSpecies.ELFO -> "Orejas Puntiagudas 3D"
    HeroSpecies.ENANO -> if (gender == HeroGender.HOMBRE) "Barba Trenzada 3D + Yelmo" else "Trenzas de Forja + Yelmo"
    HeroSpecies.ORCO -> "Colmillos de Guerra + Hombreras"
    HeroSpecies.MEDIO_ELFO -> "Manto Bicolor + Orejas"
}

private fun vocationGearDescriptorEs(archetype: EthicalArchetype): String = when (archetype) {
    EthicalArchetype.BUSCADOR_DE_VERDAD -> "Báculo Rúnico y Orbe"
    EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> "Gran Escudo y Espada"
    EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> "Hojas Gemelas y Capa"
    EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> "Mandoble y Balanza"
    EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> "Martillo y Linterna"
}

private fun vocationWeaponBadgeEs(archetype: EthicalArchetype): String = when (archetype) {
    EthicalArchetype.BUSCADOR_DE_VERDAD -> "Báculo Cristalino + Orbe"
    EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> "Escudo Baluarte + Hoja"
    EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> "Dagas Boreales + Capa Alada"
    EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> "Mandoble Solar + Balanza"
    EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> "Martillo de Forja +Fanal"
}

private fun vocationAuraBadgeEs(archetype: EthicalArchetype): String = when (archetype) {
    EthicalArchetype.BUSCADOR_DE_VERDAD -> "Anillo Astrolabio"
    EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> "Halo Protector"
    EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> "Estela de Viento"
    EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> "Corona de Juicio"
    EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> "Runa de Hermandad"
}

/**
 * Renderiza la escena 3D completa del héroe (12+ piezas geométricas articuladas, iluminación PBR
 * direccional cálida + hemisférica fría y sombras de contacto sobre el pedestal rúnico).
 */
private fun DrawScope.drawHero3DScene(
    hero: PlayerHero,
    yawDeg: Float,
    pitchDeg: Float,
    zoom: Float,
    breathPhase: Float,
    morphFlash: Float,
    emblemColor: Color,
    vocationColor: Color
) {
    val faces = mutableListOf<Face3D>()

    // 1. Parámetros anatómicos según Especie y Género
    val heightScale = when (hero.species) {
        HeroSpecies.HUMANO -> 1.00f
        HeroSpecies.ELFO -> 1.10f
        HeroSpecies.ENANO -> 0.80f
        HeroSpecies.ORCO -> 1.07f
        HeroSpecies.MEDIO_ELFO -> 1.04f
    }
    val widthScale = when (hero.species) {
        HeroSpecies.HUMANO -> 1.00f
        HeroSpecies.ELFO -> 0.85f
        HeroSpecies.ENANO -> 1.26f
        HeroSpecies.ORCO -> 1.32f
        HeroSpecies.MEDIO_ELFO -> 0.94f
    } * (if (hero.gender == HeroGender.MUJER) 0.91f else 1.0f)

    val skinColor = when (hero.species) {
        HeroSpecies.HUMANO -> Color(0xFFE0B193)
        HeroSpecies.ELFO -> Color(0xFFE8E2F4)
        HeroSpecies.ENANO -> Color(0xFFD69A76)
        HeroSpecies.ORCO -> Color(0xFF6B9E78)
        HeroSpecies.MEDIO_ELFO -> Color(0xFFE5C1A5)
    }

    val hairColor = when (hero.species) {
        HeroSpecies.HUMANO -> Color(0xFF3E2B22)
        HeroSpecies.ELFO -> Color(0xFFDCEBFF)
        HeroSpecies.ENANO -> Color(0xFFC25929)
        HeroSpecies.ORCO -> Color(0xFF1E242B)
        HeroSpecies.MEDIO_ELFO -> Color(0xFF694873)
    }

    val armorPrimary = when (hero.archetype) {
        EthicalArchetype.BUSCADOR_DE_VERDAD -> Color(0xFF2B234A)
        EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> Color(0xFF7D93AC)
        EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> Color(0xFF214A52)
        EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> Color(0xFF3A4E6E)
        EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> Color(0xFF543D2C)
    }

    val steelColor = Color(0xFFCBD8E6)
    val darkSteelColor = Color(0xFF37475A)
    val pedestalStone = Color(0xFF1B2B44)
    val pedestalTopStone = Color(0xFF253B5C)

    val breathY = breathPhase * 0.025f

    // --- PIEZA 1: PEDESTAL RÚNICO NÓRDICO (Octágono 3D de doble nivel) ---
    addCylinderPrism3D(
        faces = faces,
        center = Vec3(0f, -1.16f, 0f),
        radiusTop = 0.95f,
        radiusBottom = 1.08f,
        height = 0.22f,
        segments = 10,
        sideColor = pedestalStone,
        capColor = pedestalTopStone,
        metallic = 0.25f,
        roughness = 0.7f
    )
    // Anillo rúnico luminoso superior en el pedestal
    addCylinderPrism3D(
        faces = faces,
        center = Vec3(0f, -1.02f, 0f),
        radiusTop = 0.80f,
        radiusBottom = 0.86f,
        height = 0.06f,
        segments = 10,
        sideColor = emblemColor,
        capColor = pedestalTopStone,
        metallic = 0.6f,
        roughness = 0.2f,
        emissive = 0.45f + 0.35f * morphFlash
    )

    // --- PIEZA 2 Y 3: PIERNAS Y BOTAS ACORAZADAS ---
    val legHeight = 0.62f * heightScale
    val legWidth = 0.16f * widthScale
    val hipX = 0.21f * widthScale
    val footY = -0.99f + legHeight / 2f

    // Pierna izquierda y derecha
    addBox3D(
        faces = faces,
        center = Vec3(-hipX, footY, 0f),
        size = Vec3(legWidth, legHeight, 0.19f),
        color = darkSteelColor,
        metallic = 0.65f,
        roughness = 0.35f
    )
    addBox3D(
        faces = faces,
        center = Vec3(hipX, footY, 0f),
        size = Vec3(legWidth, legHeight, 0.19f),
        color = darkSteelColor,
        metallic = 0.65f,
        roughness = 0.35f
    )
    // Botas biseladas
    addBox3D(
        faces = faces,
        center = Vec3(-hipX, -0.91f, 0.05f),
        size = Vec3(legWidth * 1.12f, 0.16f, 0.26f),
        color = armorPrimary,
        metallic = 0.5f,
        roughness = 0.4f
    )
    addBox3D(
        faces = faces,
        center = Vec3(hipX, -0.91f, 0.05f),
        size = Vec3(legWidth * 1.12f, 0.16f, 0.26f),
        color = armorPrimary,
        metallic = 0.5f,
        roughness = 0.4f
    )

    // --- PIEZA 4: TORSO, PETO Y FALDA DE ARMADURA / TÚNICA ---
    val torsoBottomY = -0.99f + legHeight
    val torsoHeight = 0.68f * heightScale
    val torsoCenterY = torsoBottomY + torsoHeight / 2f + breathY
    val chestWidth = 0.58f * widthScale
    val waistWidth = if (hero.gender == HeroGender.MUJER) chestWidth * 0.78f else chestWidth * 0.88f

    // Faldón o túnica inferior según vocación
    val SkirtHeight = if (hero.archetype == EthicalArchetype.BUSCADOR_DE_VERDAD ||
        hero.archetype == EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA
    ) 0.38f * heightScale else 0.22f * heightScale

    addTaperedBox3D(
        faces = faces,
        center = Vec3(0f, torsoBottomY + 0.02f + breathY * 0.5f, 0f),
        topWidth = waistWidth,
        bottomWidth = chestWidth * 1.08f,
        height = SkirtHeight,
        depth = 0.32f,
        color = emblemColor.copy(alpha = 0.9f),
        metallic = 0.3f,
        roughness = 0.5f
    )

    // Torso principal
    addTaperedBox3D(
        faces = faces,
        center = Vec3(0f, torsoCenterY, 0f),
        topWidth = chestWidth,
        bottomWidth = waistWidth,
        height = torsoHeight,
        depth = 0.31f * widthScale.coerceIn(0.85f, 1.2f),
        color = armorPrimary,
        metallic = 0.7f,
        roughness = 0.3f
    )

    // Emblema pectoral luminoso
    addBox3D(
        faces = faces,
        center = Vec3(0f, torsoCenterY + 0.06f, 0.17f * widthScale.coerceIn(0.85f, 1.2f)),
        size = Vec3(0.22f * widthScale, 0.24f * heightScale, 0.05f),
        color = emblemColor,
        metallic = 0.85f,
        roughness = 0.18f,
        emissive = 0.35f + 0.4f * morphFlash
    )

    // --- PIEZA 5: CAPA NÓRDICA EN LA ESPALDA ---
    val shoulderY = torsoBottomY + torsoHeight + breathY
    addTaperedBox3D(
        faces = faces,
        center = Vec3(0f, torsoCenterY - 0.12f, -0.20f),
        topWidth = chestWidth * 0.95f,
        bottomWidth = chestWidth * (if (hero.archetype == EthicalArchetype.EXPLORADOR_DE_LIBERTAD) 1.45f else 1.20f),
        height = (torsoHeight + legHeight * 0.68f),
        depth = 0.06f,
        color = emblemColor,
        metallic = 0.2f,
        roughness = 0.65f
    )

    // --- PIEZA 6: HOMBRERAS (Pauldrons) Y BRAZOS ---
    val shoulderX = chestWidth * 0.62f
    val pauldronSize = when (hero.species) {
        HeroSpecies.ORCO, HeroSpecies.ENANO -> 0.24f
        else -> 0.19f
    }

    addBox3D(
        faces = faces,
        center = Vec3(-shoulderX, shoulderY - 0.06f, 0f),
        size = Vec3(pauldronSize, pauldronSize * 0.82f, pauldronSize),
        color = steelColor,
        metallic = 0.85f,
        roughness = 0.22f
    )
    addBox3D(
        faces = faces,
        center = Vec3(shoulderX, shoulderY - 0.06f, 0f),
        size = Vec3(pauldronSize, pauldronSize * 0.82f, pauldronSize),
        color = steelColor,
        metallic = 0.85f,
        roughness = 0.22f
    )

    // Brazos articulados
    val armLength = 0.50f * heightScale
    val handY = shoulderY - armLength * 0.72f
    val leftHandPos = Vec3(-shoulderX - 0.06f, handY, 0.18f)
    val rightHandPos = Vec3(shoulderX + 0.06f, handY, 0.20f)

    addBox3D(
        faces = faces,
        center = Vec3(-shoulderX - 0.04f, shoulderY - armLength * 0.45f, 0.08f),
        size = Vec3(0.15f * widthScale, armLength, 0.16f),
        color = armorPrimary,
        metallic = 0.5f,
        roughness = 0.4f
    )
    addBox3D(
        faces = faces,
        center = Vec3(shoulderX + 0.04f, shoulderY - armLength * 0.45f, 0.09f),
        size = Vec3(0.15f * widthScale, armLength, 0.16f),
        color = armorPrimary,
        metallic = 0.5f,
        roughness = 0.4f
    )

    // --- PIEZA 7: CABEZA Y RASGOS RACIALES 3D (Orejas Élficas, Barba Enana, Colmillos Orcos, Corona) ---
    val headRadius = 0.22f
    val headCenterY = shoulderY + headRadius + 0.04f
    addBox3D(
        faces = faces,
        center = Vec3(0f, headCenterY, 0.02f),
        size = Vec3(headRadius * 1.65f, headRadius * 1.75f, headRadius * 1.65f),
        color = skinColor,
        metallic = 0.1f,
        roughness = 0.6f
    )

    // Cabello / Yelmo superior
    val hairHeight = if (hero.gender == HeroGender.MUJER) 0.26f else 0.16f
    addBox3D(
        faces = faces,
        center = Vec3(0f, headCenterY + headRadius * 0.65f, -0.02f),
        size = Vec3(headRadius * 1.80f, 0.14f, headRadius * 1.82f),
        color = hairColor,
        metallic = 0.2f,
        roughness = 0.55f
    )
    if (hero.gender == HeroGender.MUJER) {
        // Melena trasera estilizada en 3D
        addBox3D(
            faces = faces,
            center = Vec3(0f, headCenterY - 0.08f, -headRadius * 0.85f),
            size = Vec3(headRadius * 1.55f, hairHeight * 1.4f, 0.10f),
            color = hairColor,
            metallic = 0.2f,
            roughness = 0.55f
        )
    }

    // Rasgos específicos de la Especie (Raza):
    when (hero.species) {
        HeroSpecies.ELFO, HeroSpecies.MEDIO_ELFO -> {
            val earLength = if (hero.species == HeroSpecies.ELFO) 0.24f else 0.15f
            // Orejas élficas puntiagudas en 3D hacia los lados
            addBox3D(
                faces = faces,
                center = Vec3(-headRadius * 1.15f, headCenterY + 0.03f, 0f),
                size = Vec3(earLength, 0.07f, 0.06f),
                color = skinColor,
                metallic = 0.1f,
                roughness = 0.5f
            )
            addBox3D(
                faces = faces,
                center = Vec3(headRadius * 1.15f, headCenterY + 0.03f, 0f),
                size = Vec3(earLength, 0.07f, 0.06f),
                color = skinColor,
                metallic = 0.1f,
                roughness = 0.5f
            )
        }
        HeroSpecies.ENANO -> {
            // Yelmo nórdico de forja con cresta metálica
            addBox3D(
                faces = faces,
                center = Vec3(0f, headCenterY + headRadius * 0.85f, 0.02f),
                size = Vec3(headRadius * 1.9f, 0.14f, headRadius * 1.9f),
                color = steelColor,
                metallic = 0.88f,
                roughness = 0.2f
            )
            if (hero.gender == HeroGender.HOMBRE) {
                // Barba trenzada volumétrica en 3D
                addTaperedBox3D(
                    faces = faces,
                    center = Vec3(0f, headCenterY - headRadius * 0.95f, 0.13f),
                    topWidth = headRadius * 1.35f,
                    bottomWidth = headRadius * 0.75f,
                    height = 0.34f,
                    depth = 0.14f,
                    color = hairColor,
                    metallic = 0.15f,
                    roughness = 0.65f
                )
            } else {
                // Trenzas laterales enanas con broches de oro
                addBox3D(
                    faces = faces,
                    center = Vec3(-headRadius * 0.85f, headCenterY - 0.18f, 0.08f),
                    size = Vec3(0.09f, 0.28f, 0.09f),
                    color = hairColor
                )
                addBox3D(
                    faces = faces,
                    center = Vec3(headRadius * 0.85f, headCenterY - 0.18f, 0.08f),
                    size = Vec3(0.09f, 0.28f, 0.09f),
                    color = hairColor
                )
            }
        }
        HeroSpecies.ORCO -> {
            // Colmillos inferiores prominentes en 3D + cresta guerrera
            addBox3D(
                faces = faces,
                center = Vec3(-0.08f, headCenterY - 0.08f, headRadius * 0.95f),
                size = Vec3(0.045f, 0.11f, 0.045f),
                color = ParchmentLight,
                metallic = 0.3f,
                roughness = 0.2f
            )
            addBox3D(
                faces = faces,
                center = Vec3(0.08f, headCenterY - 0.08f, headRadius * 0.95f),
                size = Vec3(0.045f, 0.11f, 0.045f),
                color = ParchmentLight,
                metallic = 0.3f,
                roughness = 0.2f
            )
            // Púas sobre las hombreras orcas
            addBox3D(
                faces = faces,
                center = Vec3(-shoulderX - 0.05f, shoulderY + 0.10f, 0f),
                size = Vec3(0.06f, 0.18f, 0.06f),
                color = steelColor,
                metallic = 0.85f,
                roughness = 0.2f
            )
            addBox3D(
                faces = faces,
                center = Vec3(shoulderX + 0.05f, shoulderY + 0.10f, 0f),
                size = Vec3(0.06f, 0.18f, 0.06f),
                color = steelColor,
                metallic = 0.85f,
                roughness = 0.2f
            )
        }
        HeroSpecies.HUMANO -> {
            // Diadema / Corona real nórdica en Plata Lunar
            addBox3D(
                faces = faces,
                center = Vec3(0f, headCenterY + headRadius * 0.55f, headRadius * 0.88f),
                size = Vec3(headRadius * 1.5f, 0.07f, 0.05f),
                color = ImperialGoldLight,
                metallic = 0.9f,
                roughness = 0.15f,
                emissive = 0.3f
            )
        }
    }

    // --- PIEZAS 8 A 12: ARMAS 3D, RELIQUIAS Y AURA SEGÚN LA VOCACIÓN ÉTICA ---
    when (hero.archetype) {
        EthicalArchetype.BUSCADOR_DE_VERDAD -> {
            // Mano derecha: Báculo Rúnico de Cristal
            addBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.04f, 0.12f, 0.06f),
                size = Vec3(0.055f, 1.48f, 0.055f),
                color = Color(0xFF704E2E),
                metallic = 0.3f,
                roughness = 0.6f
            )
            // Cristal astral en la punta del báculo
            addOctahedronCrystal3D(
                faces = faces,
                center = rightHandPos + Vec3(0.04f, 0.96f, 0.06f),
                radius = 0.17f,
                color = AmethystWisdom,
                emissive = 0.65f
            )
            // Mano izquierda: Orbe de la Verdad levitando
            addOctahedronCrystal3D(
                faces = faces,
                center = leftHandPos + Vec3(-0.06f, 0.22f + breathY * 2f, 0.12f),
                radius = 0.14f,
                color = ImperialGoldLight,
                emissive = 0.75f
            )
        }

        EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> {
            // Mano izquierda: Gran Escudo Baluarte 3D
            addBox3D(
                faces = faces,
                center = leftHandPos + Vec3(-0.10f, 0.04f, 0.12f),
                size = Vec3(0.42f, 0.62f, 0.07f),
                color = steelColor,
                metallic = 0.88f,
                roughness = 0.2f
            )
            addBox3D(
                faces = faces,
                center = leftHandPos + Vec3(-0.10f, 0.04f, 0.16f),
                size = Vec3(0.26f, 0.42f, 0.04f),
                color = emblemColor,
                metallic = 0.75f,
                roughness = 0.25f,
                emissive = 0.35f
            )
            // Mano derecha: Espada Baluarte de Luz
            addBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.04f, 0.10f, 0.10f),
                size = Vec3(0.22f, 0.05f, 0.06f),
                color = TorchAmber,
                metallic = 0.9f,
                roughness = 0.2f
            )
            addTaperedBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.04f, 0.52f, 0.10f),
                topWidth = 0.02f,
                bottomWidth = 0.09f,
                height = 0.80f,
                depth = 0.035f,
                color = ImperialGoldLight,
                metallic = 0.92f,
                roughness = 0.15f,
                emissive = 0.3f
            )
        }

        EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> {
            // Dos Hojas Boreales Gemelas en ambas manos
            addTaperedBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.06f, 0.26f, 0.16f),
                topWidth = 0.015f,
                bottomWidth = 0.075f,
                height = 0.56f,
                depth = 0.03f,
                color = SapphireOrder,
                metallic = 0.9f,
                roughness = 0.15f,
                emissive = 0.45f
            )
            addTaperedBox3D(
                faces = faces,
                center = leftHandPos + Vec3(-0.06f, 0.26f, 0.16f),
                topWidth = 0.015f,
                bottomWidth = 0.075f,
                height = 0.56f,
                depth = 0.03f,
                color = EmeraldVirtue,
                metallic = 0.9f,
                roughness = 0.15f,
                emissive = 0.45f
            )
        }

        EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> {
            // Mano derecha: Mandoble Solar de la Justicia
            addBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.06f, 0.14f, 0.10f),
                size = Vec3(0.28f, 0.06f, 0.06f),
                color = TorchAmber,
                metallic = 0.9f,
                roughness = 0.18f
            )
            addTaperedBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.06f, 0.64f, 0.10f),
                topWidth = 0.03f,
                bottomWidth = 0.11f,
                height = 0.96f,
                depth = 0.04f,
                color = ImperialGoldLight,
                metallic = 0.95f,
                roughness = 0.12f,
                emissive = 0.4f
            )
            // Mano izquierda: Balanza 3D del Juicio Justo
            val scaleCenter = leftHandPos + Vec3(-0.10f, 0.26f, 0.12f)
            addBox3D(
                faces = faces,
                center = scaleCenter,
                size = Vec3(0.34f, 0.04f, 0.04f),
                color = TorchAmber,
                metallic = 0.9f,
                roughness = 0.2f,
                emissive = 0.35f
            )
            addBox3D(
                faces = faces,
                center = scaleCenter + Vec3(-0.14f, -0.14f, 0f),
                size = Vec3(0.11f, 0.04f, 0.11f),
                color = ImperialGoldLight,
                emissive = 0.4f
            )
            addBox3D(
                faces = faces,
                center = scaleCenter + Vec3(0.14f, -0.14f, 0f),
                size = Vec3(0.11f, 0.04f, 0.11f),
                color = ImperialGoldLight,
                emissive = 0.4f
            )
        }

        EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> {
            // Mano derecha: Gran Martillo Rúnico de Forja
            addBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.05f, 0.24f, 0.08f),
                size = Vec3(0.06f, 0.88f, 0.06f),
                color = Color(0xFF6B4C35),
                metallic = 0.3f,
                roughness = 0.6f
            )
            addBox3D(
                faces = faces,
                center = rightHandPos + Vec3(0.05f, 0.68f, 0.08f),
                size = Vec3(0.24f, 0.22f, 0.36f),
                color = steelColor,
                metallic = 0.88f,
                roughness = 0.22f
            )
            // Mano izquierda: Fanal / Linterna de Fraternidad
            addOctahedronCrystal3D(
                faces = faces,
                center = leftHandPos + Vec3(-0.08f, 0.08f, 0.14f),
                radius = 0.15f,
                color = TorchAmber,
                emissive = 0.8f
            )
        }
    }

    // Halo / Corona etérea flotante detrás de los hombros según la Vocación Ética
    addFloatingAuraSegments3D(
        faces = faces,
        center = Vec3(0f, headCenterY + 0.04f, -0.18f),
        radius = 0.46f * widthScale.coerceAtLeast(0.95f),
        color = vocationColor,
        yawSeed = yawDeg
    )

    // 2. Sombra radial de contacto en la base del pedestal
    val canvasCenter = Offset(size.width * 0.5f, size.height * 0.56f)
    val baseScale = size.minDimension * 0.34f * zoom

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                vocationColor.copy(alpha = 0.26f + 0.18f * morphFlash),
                emblemColor.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = Offset(canvasCenter.x, canvasCenter.y + baseScale * 0.95f),
            radius = baseScale * 1.35f
        ),
        center = Offset(canvasCenter.x, canvasCenter.y + baseScale * 0.95f),
        radius = baseScale * 1.35f
    )

    // 3. Proyección 3D -> 2D con rotación (Yaw, Pitch), ordenamiento Z-buffer (Painter's Algorithm)
    //    e iluminación PBR (Luz direccional cálida + Luz hemisférica fría + Especular)
    val yawRad = (yawDeg * PI / 180.0).toFloat()
    val pitchRad = (pitchDeg * PI / 180.0).toFloat()
    val cosY = cos(yawRad)
    val sinY = sin(yawRad)
    val cosP = cos(pitchRad)
    val sinP = sin(pitchRad)

    fun rotatePoint(v: Vec3): Vec3 {
        // Rotación en Y (Turntable 360°)
        val rx = v.x * cosY + v.z * sinY
        val rz = -v.x * sinY + v.z * cosY
        val ry = v.y
        // Rotación en X (Inclinación de cámara)
        val finalY = ry * cosP - rz * sinP
        val finalZ = ry * sinP + rz * cosP
        return Vec3(rx, finalY, finalZ)
    }

    val keyLightDir = Vec3(0.55f, 0.68f, 0.48f).normalized()
    val viewDir = Vec3(0f, 0f, 1f)
    val cameraDist = 4.2f

    val projectedFaces = faces.mapNotNull { face ->
        val rotatedVerts = face.vertices.map(::rotatePoint)
        if (rotatedVerts.size < 3) return@mapNotNull null

        val v0 = rotatedVerts[0]
        val v1 = rotatedVerts[1]
        val v2 = rotatedVerts[2]
        val normal = (v1 - v0).cross(v2 - v0).normalized()

        val avgZ = rotatedVerts.sumOf { it.z.toDouble() }.toFloat() / rotatedVerts.size

        // Iluminación estudio PBR: Key Light cálida + Fill hemisférica nórdica + Rim/Specular
        val ndotl = normal.dot(keyLightDir).coerceAtLeast(0f)
        val hemiFactor = (normal.y * 0.5f + 0.5f).coerceIn(0f, 1f)
        val halfVec = (keyLightDir + viewDir).normalized()
        val ndoth = normal.dot(halfVec).coerceAtLeast(0f)
        val specPower = if (face.roughness < 0.3f) 16f else 6f
        val specular = Math.pow(ndoth.toDouble(), specPower.toDouble()).toFloat() * face.metallic * 0.45f

        val ambientCool = 0.34f + 0.18f * hemiFactor
        val diffuseWarm = ndotl * 0.62f
        val lightIntensity = (ambientCool + diffuseWarm + face.emissiveStrength).coerceIn(0.22f, 1.35f)

        val r = (face.baseColor.red * lightIntensity + specular).coerceIn(0f, 1f)
        val g = (face.baseColor.green * lightIntensity + specular).coerceIn(0f, 1f)
        val b = (face.baseColor.blue * (lightIntensity + 0.05f * (1f - hemiFactor)) + specular).coerceIn(0f, 1f)

        val shaded = Color(r, g, b, face.baseColor.alpha)

        val pts2D = rotatedVerts.map { rv ->
            val perspective = cameraDist / (cameraDist - rv.z).coerceAtLeast(1.2f)
            Offset(
                x = canvasCenter.x + rv.x * baseScale * perspective,
                y = canvasCenter.y - rv.y * baseScale * perspective
            )
        }

        ProjectedFace(
            points2D = pts2D,
            depthZ = avgZ,
            shadedColor = shaded,
            strokeColor = face.strokeColor
        )
    }.sortedBy { it.depthZ }

    // Dibujar caras ordenadas de atrás hacia adelante
    val path = Path()
    projectedFaces.forEach { pf ->
        path.reset()
        val first = pf.points2D.first()
        path.moveTo(first.x, first.y)
        for (i in 1 until pf.points2D.size) {
            val pt = pf.points2D[i]
            path.lineTo(pt.x, pt.y)
        }
        path.close()

        drawPath(
            path = path,
            color = pf.shadedColor
        )
        drawPath(
            path = path,
            color = pf.strokeColor ?: ObsidianBackground.copy(alpha = 0.28f),
            style = Stroke(width = 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // 4. Partículas rúnicas flotantes alrededor del pedestal
    val runeCount = 6
    for (i in 0 until runeCount) {
        val angle = yawRad + (i * 2.0 * PI / runeCount).toFloat()
        val orbX = cos(angle) * 0.92f
        val orbZ = sin(angle) * 0.92f
        val orbY = -0.88f + 0.14f * sin(angle * 2f + breathPhase * 2f)
        val rotOrb = rotatePoint(Vec3(orbX, orbY, orbZ))
        val persp = cameraDist / (cameraDist - rotOrb.z).coerceAtLeast(1.2f)
        val pt = Offset(
            x = canvasCenter.x + rotOrb.x * baseScale * persp,
            y = canvasCenter.y - rotOrb.y * baseScale * persp
        )
        drawCircle(
            color = vocationColor.copy(alpha = 0.72f),
            radius = 3.2f * persp,
            center = pt
        )
    }
}

private fun addBox3D(
    faces: MutableList<Face3D>,
    center: Vec3,
    size: Vec3,
    color: Color,
    metallic: Float = 0.3f,
    roughness: Float = 0.5f,
    emissive: Float = 0f
) {
    addTaperedBox3D(
        faces = faces,
        center = center,
        topWidth = size.x,
        bottomWidth = size.x,
        height = size.y,
        depth = size.z,
        color = color,
        metallic = metallic,
        roughness = roughness,
        emissive = emissive
    )
}

private fun addTaperedBox3D(
    faces: MutableList<Face3D>,
    center: Vec3,
    topWidth: Float,
    bottomWidth: Float,
    height: Float,
    depth: Float,
    color: Color,
    metallic: Float = 0.3f,
    roughness: Float = 0.5f,
    emissive: Float = 0f
) {
    val hy = height / 2f
    val hz = depth / 2f
    val tw = topWidth / 2f
    val bw = bottomWidth / 2f

    // 8 vértices
    val v000 = center + Vec3(-bw, -hy, -hz)
    val v100 = center + Vec3(bw, -hy, -hz)
    val v101 = center + Vec3(bw, -hy, hz)
    val v001 = center + Vec3(-bw, -hy, hz)

    val v010 = center + Vec3(-tw, hy, -hz)
    val v110 = center + Vec3(tw, hy, -hz)
    val v111 = center + Vec3(tw, hy, hz)
    val v011 = center + Vec3(-tw, hy, hz)

    // Frente (+Z)
    faces += Face3D(listOf(v001, v101, v111, v011), color, metallic, roughness, emissive)
    // Espalda (-Z)
    faces += Face3D(listOf(v100, v000, v010, v110), color, metallic, roughness, emissive)
    // Derecha (+X)
    faces += Face3D(listOf(v101, v100, v110, v111), color, metallic, roughness, emissive)
    // Izquierda (-X)
    faces += Face3D(listOf(v000, v001, v011, v010), color, metallic, roughness, emissive)
    // Arriba (+Y)
    faces += Face3D(listOf(v011, v111, v110, v010), color, metallic, roughness, emissive)
    // Abajo (-Y)
    faces += Face3D(listOf(v000, v100, v101, v001), color, metallic, roughness, emissive)
}

private fun addCylinderPrism3D(
    faces: MutableList<Face3D>,
    center: Vec3,
    radiusTop: Float,
    radiusBottom: Float,
    height: Float,
    segments: Int,
    sideColor: Color,
    capColor: Color,
    metallic: Float = 0.3f,
    roughness: Float = 0.5f,
    emissive: Float = 0f
) {
    val hy = height / 2f
    val topVerts = mutableListOf<Vec3>()
    val botVerts = mutableListOf<Vec3>()

    for (i in 0 until segments) {
        val a = (i * 2.0 * PI / segments).toFloat()
        val ca = cos(a)
        val sa = sin(a)
        topVerts += center + Vec3(ca * radiusTop, hy, sa * radiusTop)
        botVerts += center + Vec3(ca * radiusBottom, -hy, sa * radiusBottom)
    }

    for (i in 0 until segments) {
        val next = (i + 1) % segments
        faces += Face3D(
            vertices = listOf(botVerts[i], botVerts[next], topVerts[next], topVerts[i]),
            baseColor = sideColor,
            metallic = metallic,
            roughness = roughness,
            emissiveStrength = emissive
        )
    }
    faces += Face3D(topVerts, capColor, metallic, roughness, emissive * 0.5f)
}

private fun addOctahedronCrystal3D(
    faces: MutableList<Face3D>,
    center: Vec3,
    radius: Float,
    color: Color,
    emissive: Float = 0.6f
) {
    val top = center + Vec3(0f, radius * 1.25f, 0f)
    val bot = center + Vec3(0f, -radius * 1.25f, 0f)
    val f = center + Vec3(0f, 0f, radius)
    val b = center + Vec3(0f, 0f, -radius)
    val l = center + Vec3(-radius, 0f, 0f)
    val r = center + Vec3(radius, 0f, 0f)

    faces += Face3D(listOf(f, r, top), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
    faces += Face3D(listOf(r, b, top), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
    faces += Face3D(listOf(b, l, top), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
    faces += Face3D(listOf(l, f, top), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)

    faces += Face3D(listOf(r, f, bot), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
    faces += Face3D(listOf(b, r, bot), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
    faces += Face3D(listOf(l, b, bot), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
    faces += Face3D(listOf(f, l, bot), color, metallic = 0.9f, roughness = 0.1f, emissiveStrength = emissive)
}

private fun addFloatingAuraSegments3D(
    faces: MutableList<Face3D>,
    center: Vec3,
    radius: Float,
    color: Color,
    yawSeed: Float
) {
    val count = 6
    val offsetRad = (yawSeed * 0.5f * PI / 180.0).toFloat()
    for (i in 0 until count) {
        val a = offsetRad + (i * PI / (count - 1) - PI * 0.1f).toFloat()
        val px = cos(a) * radius
        val py = sin(a) * radius * 0.85f
        addBox3D(
            faces = faces,
            center = center + Vec3(px, py, 0f),
            size = Vec3(0.065f, 0.065f, 0.03f),
            color = color,
            metallic = 0.9f,
            roughness = 0.1f,
            emissive = 0.7f
        )
    }
}
