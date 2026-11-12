package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.NarrativeEventType
import com.example.audio.SoundEffect
import com.example.data.DilemmaOption
import com.example.data.DmTurnPayload
import com.example.data.EthicalArchetype
import com.example.data.GamePaceMode
import com.example.data.HeroGender
import com.example.data.HeroSpecies
import com.example.data.HeroStats
import com.example.data.MoralScores
import com.example.data.PlayerHero
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
import kotlin.random.Random

/**
 * Diálogo inicial opcional que pregunta al jugador si desea realizar o continuar con el tutorial.
 * Si elige "No, omitir", no se inicia el tutorial y se respeta su elección.
 */
@Composable
fun TutorialPromptDialog(
    onAcceptTutorial: () -> Unit,
    onDeclineTutorial: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeclineTutorial,
        containerColor = ArcaneSurface,
        titleContentColor = ImperialGoldLight,
        textContentColor = ParchmentLight,
        shape = RoundedCornerShape(22.dp),
        icon = {
            Surface(
                color = ImperialGold.copy(alpha = 0.16f),
                shape = CircleShape,
                border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.65f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = ImperialGold,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "¿Quieres hacer el Tutorial Interactivo?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Si es tu primera partida, puedes recorrer una guía paso a paso con todas las mecánicas, entidades, visor 3D, apartados de la app y un simulador de turno en vivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentLight
                )
                Surface(
                    color = ObsidianBackground.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Text(
                        text = "Si prefieres ir directo a jugar, elige «No, omitir». Siempre podrás abrirlo más tarde desde la pantalla de inicio o el botón (?) arriba.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentMuted,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAcceptTutorial,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImperialGold,
                    contentColor = Color(0xFF07111E)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("tutorial_prompt_accept_button")
            ) {
                Text("Sí, iniciar tutorial", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDeclineTutorial,
                border = BorderStroke(1.dp, ParchmentBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentMuted),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("tutorial_prompt_decline_button")
            ) {
                Text("No, omitir tutorial")
            }
        }
    )
}

private enum class TutorialChapter(
    val index: Int,
    val shortTitleEs: String,
    val fullTitleEs: String,
    val emoji: String
) {
    APP_OVERVIEW(
        index = 0,
        shortTitleEs = "1. App",
        fullTitleEs = "Funcionamiento y Apartados de la App",
        emoji = "🗺️"
    ),
    VOTERS_AND_PACE(
        index = 1,
        shortTitleEs = "2. Votantes",
        fullTitleEs = "Votantes (1–6), Complejidad y Ritmo",
        emoji = "👥"
    ),
    ENTITIES_AND_3D(
        index = 2,
        shortTitleEs = "3. Entidades",
        fullTitleEs = "Héroes, Razas, Arquetipos, Stats y 3D",
        emoji = "🛡️"
    ),
    TURN_SIMULATOR(
        index = 3,
        shortTitleEs = "4. Simulador",
        fullTitleEs = "Simulador Interactivo de Turno (5 Pasos)",
        emoji = "🎲"
    ),
    COMPASS_AND_CODEX(
        index = 4,
        shortTitleEs = "5. Brújula",
        fullTitleEs = "Brújula Moral, Medallas, Crónicas y Audio",
        emoji = "🏆"
    )
}

@Composable
fun InteractiveGameTutorialOverlay(
    onCloseTutorial: () -> Unit,
    onPlaySoundEffect: (SoundEffect) -> Unit,
    onPlayCardRevealSound: (Int) -> Unit = { onPlaySoundEffect(SoundEffect.PROBLEM_CARD_REVEALED) },
    modifier: Modifier = Modifier
) {
    var currentChapterIndex by remember { mutableIntStateOf(0) }
    val chapters = TutorialChapter.entries
    val currentChapter = chapters[currentChapterIndex]

    BackHandler {
        onCloseTutorial()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBackground,
                        ArcaneSurface,
                        ObsidianBackground
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("interactive_tutorial_overlay")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabecera fija con opción permanente de No Continuar / Salir
            Surface(
                color = ObsidianBackground.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, ParchmentBorder.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = ImperialGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "TUTORIAL INTERACTIVO",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ImperialGoldLight
                                )
                                Text(
                                    text = "Capítulo ${currentChapterIndex + 1} de ${chapters.size} • ${currentChapter.fullTitleEs}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        TextButton(
                            onClick = onCloseTutorial,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("tutorial_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "No continuar tutorial",
                                tint = CrimsonChaos,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Salir",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonChaos
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (currentChapterIndex + 1).toFloat() / chapters.size.toFloat() },
                        color = ImperialGold,
                        trackColor = ParchmentBorder.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(chapters) { index, chapter ->
                            val isSelected = index == currentChapterIndex
                            val isCompleted = index < currentChapterIndex
                            Surface(
                                onClick = {
                                    currentChapterIndex = index
                                    onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS)
                                },
                                shape = RoundedCornerShape(50),
                                color = when {
                                    isSelected -> ImperialGold.copy(alpha = 0.24f)
                                    isCompleted -> EmeraldVirtue.copy(alpha = 0.14f)
                                    else -> ArcaneSurfaceVariant.copy(alpha = 0.65f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = when {
                                        isSelected -> ImperialGold
                                        isCompleted -> EmeraldVirtue.copy(alpha = 0.7f)
                                        else -> ParchmentBorder
                                    }
                                )
                            ) {
                                Text(
                                    text = "${chapter.emoji} ${chapter.shortTitleEs}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ImperialGoldLight else ParchmentLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Contenido principal del capítulo activo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentChapter,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "tutorialChapterTransition"
                ) { chapter ->
                    when (chapter) {
                        TutorialChapter.APP_OVERVIEW -> TutorialChapterAppOverview()
                        TutorialChapter.VOTERS_AND_PACE -> TutorialChapterVotersAndPace(
                            onInteractionSound = { onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS) }
                        )
                        TutorialChapter.ENTITIES_AND_3D -> TutorialChapterEntitiesAnd3D(
                            onInteractionSound = { onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS) }
                        )
                        TutorialChapter.TURN_SIMULATOR -> TutorialChapterTurnSimulator(
                            onPlaySoundEffect = onPlaySoundEffect,
                            onPlayCardRevealSound = onPlayCardRevealSound
                        )
                        TutorialChapter.COMPASS_AND_CODEX -> TutorialChapterCompassAndCodex()
                    }
                }
            }

            // Barra inferior de navegación del tutorial
            Surface(
                color = ObsidianBackground.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, ParchmentBorder.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentChapterIndex > 0) {
                        OutlinedButton(
                            onClick = {
                                currentChapterIndex -= 1
                                onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS)
                            },
                            border = BorderStroke(1.dp, ParchmentBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentLight),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anterior")
                        }
                    } else {
                        TextButton(
                            onClick = onCloseTutorial,
                            modifier = Modifier.testTag("tutorial_skip_footer_button")
                        ) {
                            Text(
                                text = "No hacer tutorial",
                                color = ParchmentMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    val isLastChapter = currentChapterIndex == chapters.lastIndex
                    Button(
                        onClick = {
                            if (isLastChapter) {
                                onPlaySoundEffect(SoundEffect.TUTORIAL_COMPLETED)
                                onCloseTutorial()
                            } else {
                                currentChapterIndex += 1
                                onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastChapter) EmeraldVirtue else ImperialGold,
                            contentColor = Color(0xFF07111E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("tutorial_next_or_finish_button")
                    ) {
                        Text(
                            text = if (isLastChapter) "¡Listo para Jugar!" else "Siguiente",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isLastChapter) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialChapterAppOverview() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TutorialHeroBannerCard(
                badge = "VISIÓN GENERAL",
                title = "¿Qué es Dungeons & Decisions?",
                subtitle = "Es un juego de rol narrativo y dilemas éticos para 1 a 6 jugadores. Cada turno propone un conflicto real donde tus valores, el consenso del grupo y un dado D20 moldean la historia y a tus héroes.",
                accentColor = ImperialGold
            )
        }

        item {
            TutorialSectionCard(
                title = "1. Los 3 Apartados Principales (Barra Inferior)",
                accentColor = SapphireOrder,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                TutorialInfoRow(
                    icon = Icons.Default.Explore,
                    iconColor = ImperialGold,
                    title = "Aventura (Mesa de Juego)",
                    description = "Es el corazón del juego. Aquí eliges participantes (1–6), personalizas a cada héroe en la Forja 3D, juegas los turnos en mesa y alcanzas el Epílogo final."
                )
                HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                TutorialInfoRow(
                    icon = Icons.Default.AutoStories,
                    iconColor = EmeraldVirtue,
                    title = "Crónicas (Archivo de Partidas)",
                    description = "Guarda automáticamente o bajo demanda todas tus aventuras completadas con su alineamiento dominante, puntajes finales e historial turno por turno."
                )
                HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                TutorialInfoRow(
                    icon = Icons.Default.MenuBook,
                    iconColor = AmethystWisdom,
                    title = "Códice (Enciclopedia Ética)",
                    description = "Consulta el significado filosófico de las razas, vocaciones, pilares éticos y las medallas de virtud que puedes desbloquear."
                )
            }
        }

        item {
            TutorialSectionCard(
                title = "2. Controles de la Barra Superior",
                accentColor = TorchAmber,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                TutorialInfoRow(
                    icon = Icons.Default.MusicNote,
                    iconColor = ImperialGoldLight,
                    title = "Estudio Musical Dinámico y Lyria",
                    description = "Toca la nota musical (o el título superior) para explorar la banda sonora adaptativa (Barroco, Medieval, Fantasía, Suspenso, Tensión) o probar efectos de sonido."
                )
                HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                TutorialInfoRow(
                    icon = Icons.Default.VolumeUp,
                    iconColor = EmeraldVirtue,
                    title = "Silenciar / Activar Audio",
                    description = "Activa o silencia en un toque tanto la música ambiental como los efectos de sonido de decisiones, log de eventos y tiradas."
                )
                HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                TutorialInfoRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconColor = SapphireOrder,
                    title = "Botón de Ayuda (?)",
                    description = "Reabre este Tutorial Interactivo en cualquier momento de la partida sin perder tu progreso."
                )
            }
        }

        item {
            TutorialSectionCard(
                title = "3. El Flujo de una Partida Completa",
                accentColor = EmeraldVirtue,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                Text(
                    text = "1️⃣ Inicio (Participantes y Ritmo)  →  2️⃣ Forja de Héroes (Editor + Visor 3D)  →  3️⃣ Mesa Activa (Turnos de 5 pasos)  →  4️⃣ Epílogo y Medallas Guardadas en Crónicas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentLight
                )
            }
        }
    }
}

@Composable
private fun TutorialChapterVotersAndPace(
    onInteractionSound: () -> Unit
) {
    var demoVoterCount by remember { mutableIntStateOf(3) }
    val complexity = VoterDilemmaComplexity.forVoterCount(demoVoterCount)
    val complexityColor = when (complexity) {
        VoterDilemmaComplexity.SOLITARIO_INTROSPECTIVO -> EmeraldVirtue
        VoterDilemmaComplexity.DUO_DIALECTICO -> SapphireOrder
        VoterDilemmaComplexity.CONSEJO_DELIBERATIVO -> TorchAmber
        VoterDilemmaComplexity.ASAMBLEA_FACCIONES -> CrimsonChaos
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TutorialHeroBannerCard(
                badge = "ESCALADO DINÁMICO",
                title = "Cuántos Juegan Cambia la Complejidad",
                subtitle = "El juego adapta automáticamente el número de opciones éticas, el quórum necesario y el peso moral según cuántas personas estén votando en la mesa (de 1 a 6). ¡Pruébalo aquí abajo!",
                accentColor = SapphireOrder
            )
        }

        item {
            TutorialSectionCard(
                title = "Simulador de Participantes (Toca del 1 al 6)",
                accentColor = complexityColor,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..6).forEach { count ->
                        val selected = demoVoterCount == count
                        Surface(
                            onClick = {
                                demoVoterCount = count
                                onInteractionSound()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) ImperialGold else ObsidianBackground.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, if (selected) ImperialGoldLight else ParchmentBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) ObsidianBackground else ParchmentLight
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = ObsidianBackground.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, complexityColor.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${complexity.badgeEmoji} ${complexity.titleEs} (Nivel ${complexity.levelIndex}/4)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ImperialGoldLight
                        )
                        Text(
                            text = "• Quórum requerido: ${complexity.quorumLabelEs(demoVoterCount)}\n• Opciones por turno: ${complexity.optionsPerTurn} posturas éticas\n• Multiplicador de riesgo moral: x${complexity.stakeMultiplier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentLight
                        )
                    }
                }
            }
        }

        item {
            TutorialSectionCard(
                title = "Modalidades de Ritmo de Campaña",
                accentColor = TorchAmber,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                GamePaceMode.entries.forEachIndexed { idx, mode ->
                    if (idx > 0) HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = mode.badgeEmoji, fontSize = 22.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${mode.titleEs} (~${mode.targetIdealTurns} turnos orientativos)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight
                            )
                            Text(
                                text = mode.descriptionEs,
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentMuted
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
private fun TutorialChapterEntitiesAnd3D(
    onInteractionSound: () -> Unit
) {
    var selectedSpecies by remember { mutableStateOf(HeroSpecies.ELFO) }
    var selectedArchetype by remember { mutableStateOf(EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD) }
    val previewStats = remember(selectedSpecies, selectedArchetype) {
        HeroStats.baseFor(selectedSpecies, selectedArchetype)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TutorialHeroBannerCard(
                badge = "ENTIDADES DEL JUEGO",
                title = "Anatomía de Cada Héroe y Visor 3D",
                subtitle = "En la Forja de Héroes puedes editar a cada jugador por pestañas. Su Raza y su Vocación Ética definen sus estadísticas base, cómo reacciona el mundo a él y la apariencia de su miniatura 3D.",
                accentColor = EmeraldVirtue
            )
        }

        item {
            TutorialSectionCard(
                title = "1. Raza (Especie) — Toca para inspeccionar",
                accentColor = SapphireOrder,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HeroSpecies.entries.forEach { species ->
                        val active = selectedSpecies == species
                        Surface(
                            onClick = {
                                selectedSpecies = species
                                onInteractionSound()
                            },
                            shape = RoundedCornerShape(50),
                            color = if (active) ImperialGold.copy(alpha = 0.24f) else ObsidianBackground.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, if (active) ImperialGold else ParchmentBorder)
                        ) {
                            Text(
                                text = "${species.emoji} ${species.labelEs}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) ImperialGoldLight else ParchmentLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Surface(
                    color = ObsidianBackground.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SapphireOrder.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${selectedSpecies.emoji} ${selectedSpecies.labelEs} • Bonificación: ${selectedSpecies.statBonusLabelEs}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SapphireOrder
                        )
                        Text(
                            text = selectedSpecies.culturalTraitEs,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentLight
                        )
                    }
                }
            }
        }

        item {
            TutorialSectionCard(
                title = "2. Vocación Ética (Arquetipo) — Toca para inspeccionar",
                accentColor = AmethystWisdom,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EthicalArchetype.entries.forEach { archetype ->
                        val active = selectedArchetype == archetype
                        Surface(
                            onClick = {
                                selectedArchetype = archetype
                                onInteractionSound()
                            },
                            shape = RoundedCornerShape(50),
                            color = if (active) AmethystWisdom.copy(alpha = 0.24f) else ObsidianBackground.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, if (active) AmethystWisdom else ParchmentBorder)
                        ) {
                            Text(
                                text = "${archetype.emoji} ${archetype.titleEs}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) ImperialGoldLight else ParchmentLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Surface(
                    color = ObsidianBackground.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AmethystWisdom.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${selectedArchetype.emoji} ${selectedArchetype.titleEs} • ${selectedArchetype.statBonusLabelEs}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmethystWisdom
                        )
                        Text(
                            text = selectedArchetype.philosophicalDescriptionEs,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentLight
                        )
                        Text(
                            text = selectedArchetype.mottoEs,
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = ImperialGoldLight
                        )
                    }
                }
            }
        }

        item {
            TutorialSectionCard(
                title = "3. Las 4 Estadísticas Base Resultantes & Visor 3D",
                accentColor = ImperialGold,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HeroStatMiniPill("⚖️", "Honor", previewStats.honor, SapphireOrder, modifier = Modifier.weight(1f))
                    HeroStatMiniPill("💚", "Empatía", previewStats.empathy, EmeraldVirtue, modifier = Modifier.weight(1f))
                    HeroStatMiniPill("🛡️", "Disciplina", previewStats.discipline, AmethystWisdom, modifier = Modifier.weight(1f))
                    HeroStatMiniPill("🦅", "Audacia", previewStats.audacity, TorchAmber, modifier = Modifier.weight(1f))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = ImperialGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "En el editor, el Visor 3D sobre pedestal rúnico cambia de silueta física según la Raza y transforma armadura, arma y aura según la Vocación. Puedes arrastrar con el dedo para rotarlo 360°.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialChapterTurnSimulator(
    onPlaySoundEffect: (SoundEffect) -> Unit,
    onPlayCardRevealSound: (Int) -> Unit
) {
    val practicePayload = remember {
        DmTurnPayload(
            chapterTitle = "Práctica: El Puente de Hielo",
            narrativeText = "Una tormenta nocturna amenaza con derrumbar el puente de hielo que une dos aldeas fronterizas, mientras un convoy herido intenta cruzar antes del amanecer.\n\nEl capitán de guardia exige dinamitar los arcos laterales para salvar la muralla norte, pero eso atraparía a los rezagados en la ventisca.",
            ethicalQuestion = "¿Sacrificas el paso lateral para proteger la muralla o arriesgas al pelotón para rescatar a cada rezagado?",
            isStoryConcluded = false,
            options = listOf(
                DilemmaOption(
                    id = "sim_opt_1",
                    label = "Rescatar a los rezagados",
                    description = "Prioriza la dignidad de cada vida sobre la seguridad táctica.",
                    philosophyTag = "Dignidad y Compasión",
                    justiceDelta = 4,
                    compassionDelta = 9,
                    pragmatismDelta = -4,
                    libertyDelta = 2
                ),
                DilemmaOption(
                    id = "sim_opt_2",
                    label = "Sellar el puente con orden",
                    description = "Contiene el colapso para resguardar a toda la ciudadela.",
                    philosophyTag = "Deber y Prudencia",
                    justiceDelta = 3,
                    compassionDelta = -5,
                    pragmatismDelta = 9,
                    libertyDelta = -2
                ),
                DilemmaOption(
                    id = "sim_opt_3",
                    label = "Improvisar cuerdas libres",
                    description = "Da autonomía al convoy para cruzar sin destruir el arco.",
                    philosophyTag = "Libertad y Audacia",
                    justiceDelta = 1,
                    compassionDelta = 4,
                    pragmatismDelta = -2,
                    libertyDelta = 8
                )
            )
        )
    }

    var simHero by remember {
        mutableStateOf(
            PlayerHero(
                index = 0,
                name = "Lyra",
                gender = HeroGender.MUJER,
                species = HeroSpecies.MEDIO_ELFO,
                archetype = EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD
            )
        )
    }
    var simScores by remember { mutableStateOf(MoralScores()) }
    var simSelectedOption by remember { mutableStateOf<DilemmaOption?>(null) }
    var simDiceRoll by remember { mutableStateOf<Int?>(null) }
    var simDeltas by remember { mutableStateOf<com.example.data.HeroStatDeltas?>(null) }

    val currentSimStep = when {
        simDiceRoll != null && simSelectedOption != null -> 5
        simSelectedOption != null -> 3
        else -> 1
    }

    fun executePracticeRoll(roll: Int) {
        val option = simSelectedOption ?: return
        if (simDiceRoll != null) return // Tiro único bloqueado
        val clampedRoll = roll.coerceIn(1, 20)
        val (updatedHero, heroDeltas) = simHero.applyActionAndDiceRoll(option, clampedRoll)
        simHero = updatedHero
        simDeltas = heroDeltas
        simScores = simScores.applyChoice(option, clampedRoll)
        simDiceRoll = clampedRoll
        onPlaySoundEffect(SoundEffect.DICE_ROLL)
    }

    fun resetSimulator() {
        simHero = PlayerHero(
            index = 0,
            name = "Lyra",
            gender = HeroGender.MUJER,
            species = HeroSpecies.MEDIO_ELFO,
            archetype = EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD
        )
        simScores = MoralScores()
        simSelectedOption = null
        simDiceRoll = null
        simDeltas = null
        onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tutorial_turn_simulator_list"),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TutorialHeroBannerCard(
                badge = "ENTRENAMIENTO EN VIVO",
                title = "Simulador de Turno: Los 5 Pasos",
                subtitle = "Practica aquí sin afectar tu partida real: 1) Lee el problema por tarjetas → 2) Elige solución → 3) Tira 1 único D20 → 4) Ve el desenlace → 5) Observa el cambio de stats.",
                accentColor = TorchAmber
            )
        }

        // Indicador de 5 pasos en vivo
        item {
            TurnFiveStepOrderBanner(
                currentStep = currentSimStep,
                hasSelectedOption = simSelectedOption != null,
                isDiceLocked = simDiceRoll != null,
                hasResolution = simDiceRoll != null,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        // PASO 1: Tarjeta remodelada de problema
        item {
            RemodeledProblemPresentationCard(
                turnNumber = 1,
                payload = practicePayload,
                currentEvent = NarrativeEventType.DILEMA,
                moralScores = simScores,
                voterCount = 3,
                onAdvanceStepSound = onPlayCardRevealSound,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        // PASO 2: Elegir solución
        item {
            TutorialSectionCard(
                title = "Paso 2: Elige una Solución de Práctica",
                accentColor = SapphireOrder,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                practicePayload.options.forEach { option ->
                    val isSelected = simSelectedOption?.id == option.id
                    val isLocked = simDiceRoll != null
                    Surface(
                        onClick = {
                            if (!isLocked) {
                                simSelectedOption = option
                                onPlaySoundEffect(SoundEffect.DECISION_BUTTON_PRESS)
                            }
                        },
                        enabled = !isLocked || isSelected,
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ImperialGold.copy(alpha = 0.18f) else ObsidianBackground.copy(alpha = 0.75f),
                        border = BorderStroke(
                            width = if (isSelected) 1.6.dp else 1.dp,
                            color = if (isSelected) ImperialGold else ParchmentBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_option_${option.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
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
                                    color = if (isSelected) ImperialGoldLight else ParchmentLight
                                )
                                Text(
                                    text = if (isSelected) "✓ Elegida" else option.philosophyTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) EmeraldVirtue else TorchAmber
                                )
                            }
                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentMuted
                            )
                        }
                    }
                }
            }
        }

        // PASO 3: Dado D20 único y bloqueado
        item {
            TutorialSectionCard(
                title = "Paso 3: Tira el Dado D20 (Tiro Único Bloqueado)",
                accentColor = if (simSelectedOption == null) ParchmentMuted else ImperialGold,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                if (simSelectedOption == null) {
                    Text(
                        text = "🔒 Primero elige una solución en el Paso 2 para desbloquear el dado D20.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentMuted
                    )
                } else if (simDiceRoll == null) {
                    Text(
                        text = "Solo puedes tirar UNA vez por turno. Puedes lanzar al azar o probar cualquiera de los 3 tramos de resultado:",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParchmentLight
                    )
                    Button(
                        onClick = { executePracticeRoll(Random.nextInt(1, 21)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImperialGold,
                            contentColor = Color(0xFF07111E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_roll_d20_button")
                    ) {
                        Icon(imageVector = Icons.Default.Casino, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lanzar D20 Aleatorio (1–20)", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { executePracticeRoll(3) },
                            border = BorderStroke(1.dp, CrimsonChaos.copy(alpha = 0.7f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Probar 3\n(1–5 Fallo)", fontSize = 10.sp, textAlign = TextAlign.Center, color = CrimsonChaos)
                        }
                        OutlinedButton(
                            onClick = { executePracticeRoll(11) },
                            border = BorderStroke(1.dp, TorchAmber.copy(alpha = 0.7f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Probar 11\n(6–14 Parcial)", fontSize = 10.sp, textAlign = TextAlign.Center, color = TorchAmber)
                        }
                        OutlinedButton(
                            onClick = { executePracticeRoll(19) },
                            border = BorderStroke(1.dp, EmeraldVirtue.copy(alpha = 0.7f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Probar 19\n(15–20 Pleno)", fontSize = 10.sp, textAlign = TextAlign.Center, color = EmeraldVirtue)
                        }
                    }
                } else {
                    val roll = simDiceRoll!!
                    val tierColor = when {
                        roll <= 5 -> CrimsonChaos
                        roll <= 14 -> TorchAmber
                        else -> EmeraldVirtue
                    }
                    val tierName = when {
                        roll <= 5 -> "1–5: Fallo / Infortunio"
                        roll <= 14 -> "6–14: Éxito parcial con costo"
                        else -> "15–20: Éxito pleno o revelación"
                    }
                    Surface(
                        color = tierColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, tierColor.copy(alpha = 0.75f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = tierColor)
                            Column {
                                Text(
                                    text = "Dado Bloqueado en D20 = $roll ($tierName)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                                Text(
                                    text = "No se puede volver a tirar en este turno: el azar se acepta con honestidad.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // PASOS 4 y 5: Desenlace y Modificación de Stats
        if (simDiceRoll != null && simDeltas != null && simSelectedOption != null) {
            val roll = simDiceRoll!!
            val deltas = simDeltas!!
            item {
                TutorialSectionCard(
                    title = "Pasos 4 y 5: Desenlace y Stats de Lyra",
                    accentColor = EmeraldVirtue,
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .testTag("sim_outcome_card")
                ) {
                    val outcomeSummary = when {
                        roll <= 5 -> "El viento glaciar complica la maniobra (${simSelectedOption!!.label}). Lyra sufre desgaste físico y un costo moral adicional."
                        roll <= 14 -> "La acción (${simSelectedOption!!.label}) logra salvar la situación, pero deja una huella mixta entre los pilares."
                        else -> "¡Ejecución ejemplar! Con D20 = $roll, Lyra inspira a toda la guardia y potencia sus virtudes personales."
                    }
                    Text(
                        text = outcomeSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParchmentLight
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HeroStatMiniPill("⚖️", "Honor", simHero.stats.honor, SapphireOrder, deltas.honorDelta, Modifier.weight(1f))
                        HeroStatMiniPill("💚", "Empatía", simHero.stats.empathy, EmeraldVirtue, deltas.empathyDelta, Modifier.weight(1f))
                        HeroStatMiniPill("🛡️", "Disciplina", simHero.stats.discipline, AmethystWisdom, deltas.disciplineDelta, Modifier.weight(1f))
                        HeroStatMiniPill("🦅", "Audacia", simHero.stats.audacity, TorchAmber, deltas.audacityDelta, Modifier.weight(1f))
                    }
                    OutlinedButton(
                        onClick = { resetSimulator() },
                        border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = ImperialGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar Simulador para probar otra tirada", color = ImperialGoldLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialChapterCompassAndCodex() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TutorialHeroBannerCard(
                badge = "CONSECUENCIAS Y LEGADO",
                title = "Brújula Moral, Medallas, Crónicas y Audio",
                subtitle = "Cada decisión no solo cambia al héroe activo: inclina los 4 ejes morales de todo el grupo, desbloquea medallas al final y transforma la música del juego.",
                accentColor = AmethystWisdom
            )
        }

        item {
            TutorialSectionCard(
                title = "Los 4 Pilares de la Brújula Moral del Grupo",
                accentColor = SapphireOrder,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                TutorialBulletLine("⚖️ Justicia y Conciencia", "Rectitud moral, equidad ante la ley injusta y fidelidad a la verdad.", SapphireOrder)
                TutorialBulletLine("💚 Dignidad y Comunidad", "Compasión, solidaridad y respeto al valor intrínseco de cada persona.", EmeraldVirtue)
                TutorialBulletLine("🧭 Deber y Prudencia", "Responsabilidad colectiva, templanza y defensa del bien común.", AmethystWisdom)
                TutorialBulletLine("🕊️ Libertad Responsable", "Autonomía, coraje frente a la tiranía y peso de elegir libremente.", TorchAmber)
            }
        }

        item {
            TutorialSectionCard(
                title = "Epílogo, Medallas Éticas y Crónicas",
                accentColor = EmeraldVirtue,
                modifier = Modifier.widthIn(max = 640.dp)
            ) {
                TutorialInfoRow(
                    icon = Icons.Default.EmojiEvents,
                    iconColor = TorchAmber,
                    title = "Medallas de Virtud al Concluir",
                    description = "Cuando la historia llega a su clímax orgánico, el Epílogo evalúa la coherencia de vuestras elecciones y otorga insignias éticas."
                )
                HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                TutorialInfoRow(
                    icon = Icons.Default.AutoStories,
                    iconColor = EmeraldVirtue,
                    title = "Bitácora e Historial Permanente",
                    description = "Cada turno queda registrado en el Log de Eventos y al guardar la partida pasa a la pestaña Crónicas para releerla cuando quieras."
                )
                HorizontalDivider(color = ParchmentBorder.copy(alpha = 0.45f))
                TutorialInfoRow(
                    icon = Icons.Default.MusicNote,
                    iconColor = ImperialGold,
                    title = "Audio Adaptativo",
                    description = "La música cambia de género entre Menú, Forja, Exploración, Suspenso y Fallos Críticos, y cada pulsación o tirada tiene su efecto sonoro."
                )
            }
        }
    }
}

@Composable
private fun TutorialHeroBannerCard(
    badge: String,
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneCardElevated.copy(alpha = 0.92f)),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.65f))
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ImperialGoldLight
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentLight
            )
        }
    }
}

@Composable
private fun TutorialSectionCard(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ImperialGoldLight
            )
            content()
        }
    }
}

@Composable
private fun TutorialInfoRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = iconColor.copy(alpha = 0.16f),
            shape = CircleShape,
            border = BorderStroke(1.dp, iconColor.copy(alpha = 0.55f)),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = ParchmentLight
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentMuted
            )
        }
    }
}

@Composable
private fun TutorialBulletLine(
    title: String,
    description: String,
    color: Color
) {
    Surface(
        color = ObsidianBackground.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ParchmentLight
            )
        }
    }
}
