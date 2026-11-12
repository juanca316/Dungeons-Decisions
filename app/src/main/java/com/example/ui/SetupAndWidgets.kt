package com.example.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.example.R
import com.example.data.EthicalArchetype
import com.example.data.GamePaceMode
import com.example.data.HeroGender
import com.example.data.HeroSpecies
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

@Composable
fun WelcomePlayerCountScreen(
    selectedCount: Int,
    selectedPaceMode: GamePaceMode,
    onSelectCount: (Int) -> Unit,
    onSelectPaceMode: (GamePaceMode) -> Unit,
    onContinueToPartySetup: (Int) -> Unit,
    onQuickStartAdventure: (Int) -> Unit,
    onOpenTutorial: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("welcome_player_count_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Banner Visual Minimalista con Estética Azul Noche Nórdico y Plata Lunar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("welcome_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.5.dp, ImperialGold.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_dungeon),
                        contentDescription = "Dungeons & Decisions",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ObsidianBackground.copy(alpha = 0.25f),
                                        ObsidianBackground.copy(alpha = 0.90f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚔️ DUNGEONS & DECISIONS",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ImperialGoldLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("banner_title_text")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Edición Noche Nórdica y Plata Lunar • CORE I",
                            style = MaterialTheme.typography.labelLarge,
                            color = ImperialGold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("welcome_subtitle_text")
                        )
                    }
                }
            }
        }

        // 2. Selector Visual de Votantes (1 a 6) + Ajuste de Complejidad de Dilemas
        item {
            val complexity = VoterDilemmaComplexity.forVoterCount(selectedCount)
            val complexityColor = when (complexity) {
                VoterDilemmaComplexity.SOLITARIO_INTROSPECTIVO -> EmeraldVirtue
                VoterDilemmaComplexity.DUO_DIALECTICO -> SapphireOrder
                VoterDilemmaComplexity.CONSEJO_DELIBERATIVO -> TorchAmber
                VoterDilemmaComplexity.ASAMBLEA_FACCIONES -> CrimsonChaos
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("player_count_selector_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurfaceVariant.copy(alpha = 0.9f)),
                border = BorderStroke(1.2.dp, complexityColor.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = ImperialGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Participantes y Votantes (1 a 6)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialGoldLight,
                                    modifier = Modifier.testTag("player_count_question_text")
                                )
                                Text(
                                    text = "Ajusta la complejidad según cuántos votan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ParchmentMuted
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                onClick = { if (selectedCount > 1) onSelectCount(selectedCount - 1) },
                                shape = CircleShape,
                                color = ObsidianBackground.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, if (selectedCount > 1) ImperialGold else ParchmentBorder),
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("voter_count_decrement_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "−",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedCount > 1) ImperialGoldLight else ParchmentMuted
                                    )
                                }
                            }

                            Surface(
                                color = ImperialGold.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "$selectedCount ${if (selectedCount == 1) "Votante" else "Votantes"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialGoldLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                onClick = { if (selectedCount < 6) onSelectCount(selectedCount + 1) },
                                shape = CircleShape,
                                color = ObsidianBackground.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, if (selectedCount < 6) ImperialGold else ParchmentBorder),
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("voter_count_increment_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedCount < 6) ImperialGoldLight else ParchmentMuted
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..6).forEach { number ->
                            val isSelected = selectedCount == number
                            PlayerNumberMedallion(
                                number = number,
                                isSelected = isSelected,
                                onClick = { onSelectCount(number) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("player_count_button_$number")
                            )
                        }
                    }

                    Surface(
                        color = ObsidianBackground.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, complexityColor.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dilemma_complexity_preview_badge")
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = complexity.badgeEmoji, fontSize = 16.sp)
                                    Text(
                                        text = complexity.titleEs,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = ImperialGoldLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    color = complexityColor.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, complexityColor.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "Nivel ${complexity.levelIndex}/4 • ${complexity.shortTierEs}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = complexityColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (1..4).forEach { seg ->
                                    val activeSeg = seg <= complexity.levelIndex
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (activeSeg) complexityColor else ParchmentBorder.copy(alpha = 0.45f)
                                            )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = ArcaneSurfaceVariant,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "🗳️ ${complexity.quorumLabelEs(selectedCount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = ParchmentLight,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = ArcaneSurfaceVariant,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "⚡ ${complexity.optionsPerTurn} Opciones",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = ImperialGoldLight,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = ArcaneSurfaceVariant,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "🔥 Costo x${complexity.stakeMultiplier}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = complexityColor,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Selector Compacto de Duración (3 Tarjetas Visuales en Fila)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ImperialGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Ritmo de Partida",
                            style = MaterialTheme.typography.titleMedium,
                            color = ImperialGoldLight
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GamePaceMode.entries.forEach { mode ->
                            val isSelected = mode == selectedPaceMode
                            val shortTitle = when (mode) {
                                GamePaceMode.MODO_EXPRES -> "Exprés"
                                GamePaceMode.MODO_ESTANDAR -> "Estándar"
                                GamePaceMode.MODO_CAMPANA -> "Campaña"
                            }
                            val shortTurns = when (mode) {
                                GamePaceMode.MODO_EXPRES -> "Rápida"
                                GamePaceMode.MODO_ESTANDAR -> "Media"
                                GamePaceMode.MODO_CAMPANA -> "Épica"
                            }
                            Surface(
                                onClick = { onSelectPaceMode(mode) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) ArcaneCardElevated else ObsidianBackground.copy(alpha = 0.6f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ImperialGold else ParchmentBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pace_mode_card_${mode.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = mode.badgeEmoji,
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = shortTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ImperialGoldLight else ParchmentLight,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = shortTurns,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TorchAmber else ParchmentMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Botones de Acción Directa
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onContinueToPartySetup(selectedCount) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("customize_party_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color(0xFF07111E)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Personalizar $selectedCount ${if (selectedCount == 1) "Héroe" else "Héroes"}",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = { onQuickStartAdventure(selectedCount) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quick_start_adventure_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ImperialGold.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImperialGoldLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Inicio Rápido",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Surface(
                    onClick = onOpenTutorial,
                    shape = RoundedCornerShape(16.dp),
                    color = ArcaneSurface,
                    border = BorderStroke(1.dp, SapphireOrder.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_tutorial_welcome_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = SapphireOrder,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tutorial Interactivo de Mecánicas",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight
                            )
                            Text(
                                text = "Guía completa para principiantes + simulador de turno",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SapphireOrder,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerNumberMedallion(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(),
        label = "medallion_scale"
    )

    val roleIcons = listOf("🛡️", "🔮", "⚖️", "🤝", "🦅", "👑")
    val complexity = VoterDilemmaComplexity.forVoterCount(number)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) ImperialGold.copy(alpha = 0.24f) else ObsidianBackground.copy(alpha = 0.7f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) ImperialGold else ParchmentBorder
        ),
        modifier = modifier
            .scale(scale)
            .minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = roleIcons.getOrElse(number - 1) { "⚔️" },
                fontSize = 15.sp
            )
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) ImperialGold else ParchmentLight
            )
            Text(
                text = "${complexity.optionsPerTurn} opc",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = if (isSelected) ImperialGoldLight else ParchmentMuted
            )
        }
    }
}

@Composable
fun HeroAvatarPortrait(
    hero: PlayerHero,
    sizeDp: Int = 64,
    modifier: Modifier = Modifier
) {
    val decodedBitmap = remember(hero.avatarBase64) {
        hero.avatarBase64?.let { b64 ->
            try {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    val emblemColor = Color(hero.emblemColorHex)
    val speciesColors = listOf(
        emblemColor.copy(alpha = 0.78f),
        ArcaneCardElevated,
        ObsidianBackground
    )

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(colors = speciesColors))
            .border(2.dp, emblemColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = "Avatar de ${hero.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f
                drawCircle(
                    color = ImperialGoldLight.copy(alpha = 0.25f),
                    radius = r * 0.78f,
                    style = Stroke(width = 3f)
                )
            }
            Text(
                text = "${hero.species.emoji}${hero.archetype.emoji}",
                fontSize = (sizeDp * 0.30f).sp
            )
        }
        if (hero.isGeneratingIdentity) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBackground.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ImperialGold,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size((sizeDp * 0.45f).dp)
                )
            }
        }
    }
}

/**
 * Nueva Pantalla de Edición de Personajes:
 * Selector por pestañas de héroe en la parte superior + Tarjeta de Vista Previa Grande con Stats en vivo
 * + Secciones visuales de edición ergonómicas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartyForgeScreen(
    players: List<PlayerHero>,
    selectedPaceMode: GamePaceMode,
    selectedHeroTabIndex: Int = 0,
    onSelectHeroTab: (Int) -> Unit = {},
    onUpdatePlayerName: (Int, String) -> Unit,
    onUpdatePlayerGender: (Int, HeroGender) -> Unit,
    onUpdatePlayerSpecies: (Int, HeroSpecies) -> Unit,
    onUpdatePlayerArchetype: (Int, EthicalArchetype) -> Unit,
    onUpdatePlayerEmblemColor: (Int, Long) -> Unit = { _, _ -> },
    onRandomizeHeroProfile: (Int) -> Unit = {},
    onGenerateHeroIdentityAi: (Int) -> Unit,
    onGenerateAllHeroesAi: () -> Unit,
    onBackToCount: () -> Unit,
    onLaunchCampaign: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localTabIndex by remember(players.size) {
        mutableIntStateOf(selectedHeroTabIndex.coerceIn(0, (players.size - 1).coerceAtLeast(0)))
    }
    LaunchedEffect(selectedHeroTabIndex, players.size) {
        localTabIndex = selectedHeroTabIndex.coerceIn(0, (players.size - 1).coerceAtLeast(0))
    }

    val activeIndex = localTabIndex.coerceIn(0, (players.size - 1).coerceAtLeast(0))
    val activeHero = players.getOrElse(activeIndex) { players.first() }
    val heroAccent = Color(activeHero.emblemColorHex)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("party_forge_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Cabecera Superior + Acción de Avatares IA
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBackToCount,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ArcaneSurfaceVariant)
                        .testTag("party_forge_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = ImperialGoldLight
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Forja de Héroes (${players.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = ImperialGoldLight
                    )
                    Text(
                        text = "Selector por pestañas • ${selectedPaceMode.badgeEmoji} ${selectedPaceMode.titleEs}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ImperialGold
                    )
                }
                OutlinedButton(
                    onClick = onGenerateAllHeroesAi,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ImperialGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImperialGoldLight),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("generate_all_heroes_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ImperialGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IA Grupo",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // 2. Barra de Pestañas de Héroe (Selector horizontal rápido)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("hero_editor_tabs_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players, key = { _, h -> h.index }) { idx, heroTab ->
                    val isSelected = idx == activeIndex
                    val tabColor = Color(heroTab.emblemColorHex)
                    Surface(
                        onClick = {
                            localTabIndex = idx
                            onSelectHeroTab(idx)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) ArcaneCardElevated else ArcaneSurface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) tabColor else ParchmentBorder
                        ),
                        modifier = Modifier.testTag("hero_tab_${heroTab.index}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HeroAvatarPortrait(
                                hero = heroTab,
                                sizeDp = 34
                            )
                            Column {
                                Text(
                                    text = "Héroe ${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isSelected) tabColor else ParchmentMuted
                                )
                                Text(
                                    text = heroTab.name.ifBlank { "Héroe ${idx + 1}" },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ImperialGoldLight else ParchmentLight,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Tarjeta de Vista Previa Grande del Héroe Seleccionado + Estadísticas Base en Vivo
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .testTag("hero_setup_card_${activeHero.index}"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.8.dp, heroAccent.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    heroAccent.copy(alpha = 0.16f),
                                    ArcaneSurface
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HeroAvatarPortrait(
                            hero = activeHero,
                            sizeDp = 82,
                            modifier = Modifier.testTag("hero_avatar_portrait_${activeHero.index}")
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = heroAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, heroAccent.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = "HÉROE ${activeIndex + 1} DE ${players.size} • VISTA PREVIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = ImperialGoldLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = activeHero.fullIdentityPhrase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight
                            )

                            Text(
                                text = activeHero.archetype.mottoEs,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = ParchmentMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Visor 3D Interactivo de Cuerpo Completo sobre Pedestal Rúnico (Refleja Raza, Vocación, Género y Emblema)
                    Hero3DPreviewStage(
                        hero = activeHero
                    )

                    // Cuadrícula de Estadísticas Iniciales del Personaje en Vivo
                    Surface(
                        color = ObsidianBackground.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ParchmentBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hero_preview_stats_bar_${activeHero.index}")
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
                                    text = "ESTADÍSTICAS BASE DEL PERSONAJE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialGold
                                )
                                Text(
                                    text = "${activeHero.species.statBonusLabelEs} | ${activeHero.archetype.statBonusLabelEs}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = TorchAmber,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HeroStatMiniPill(
                                    emoji = "⚖️",
                                    label = "Honor",
                                    value = activeHero.stats.honor,
                                    color = SapphireOrder,
                                    modifier = Modifier.weight(1f)
                                )
                                HeroStatMiniPill(
                                    emoji = "💚",
                                    label = "Empatía",
                                    value = activeHero.stats.empathy,
                                    color = EmeraldVirtue,
                                    modifier = Modifier.weight(1f)
                                )
                                HeroStatMiniPill(
                                    emoji = "🛡️",
                                    label = "Disciplina",
                                    value = activeHero.stats.discipline,
                                    color = AmethystWisdom,
                                    modifier = Modifier.weight(1f)
                                )
                                HeroStatMiniPill(
                                    emoji = "🦅",
                                    label = "Audacia",
                                    value = activeHero.stats.audacity,
                                    color = TorchAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (activeHero.visualDescription.isNotBlank()) {
                        Text(
                            text = activeHero.visualDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = ParchmentMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("hero_visual_description_${activeHero.index}")
                        )
                    }

                    // Acciones Rápidas sobre el Héroe Activo: Aleatorio + Retrato IA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRandomizeHeroProfile(activeHero.index) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ParchmentBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("randomize_hero_button_${activeHero.index}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = TorchAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Perfil Aleatorio", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { onGenerateHeroIdentityAi(activeHero.index) },
                            enabled = !activeHero.isGeneratingIdentity,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ImperialGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ImperialGoldLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("generate_hero_ai_button_${activeHero.index}")
                        ) {
                            if (activeHero.isGeneratingIdentity) {
                                CircularProgressIndicator(
                                    color = ImperialGold,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generando...", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ImperialGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeHero.visualDescription.isBlank()) "Retrato IA" else "Regenerar IA",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Panel de Edición Modular del Héroe Activo (Nombre, Género, Emblema, Especie, Arquetipo)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.dp, ParchmentBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Sección A: Nombre, Género y Color de Emblema Lunar
                    Text(
                        text = "1. Identidad y Emblema Lunar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGoldLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = activeHero.name,
                            onValueChange = { onUpdatePlayerName(activeHero.index, it) },
                            label = { Text("Nombre del Héroe") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImperialGold,
                                unfocusedBorderColor = ParchmentBorder,
                                focusedTextColor = ParchmentLight,
                                unfocusedTextColor = ParchmentLight,
                                focusedLabelColor = ImperialGoldLight,
                                unfocusedLabelColor = ParchmentMuted
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("hero_name_input_${activeHero.index}")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Género",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentMuted
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                HeroGender.entries.forEach { genderOption ->
                                    val selected = activeHero.gender == genderOption
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onUpdatePlayerGender(activeHero.index, genderOption) },
                                        label = {
                                            Text(
                                                text = genderOption.labelEs,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ImperialGold.copy(alpha = 0.28f),
                                            selectedLabelColor = ImperialGoldLight,
                                            containerColor = ObsidianBackground.copy(alpha = 0.6f),
                                            labelColor = ParchmentMuted
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Selector de Color de Emblema
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Color de Emblema:",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentMuted
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NORDIC_EMBLEM_SWATCHES.forEach { swatchHex ->
                                val swatchColor = Color(swatchHex)
                                val isSelected = activeHero.emblemColorHex == swatchHex
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Color.White else ObsidianBackground,
                                            shape = CircleShape
                                        )
                                        .clickable { onUpdatePlayerEmblemColor(activeHero.index, swatchHex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF07111E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sección B: Especie (Con bonificadores de estadísticas visibles)
                    Text(
                        text = "2. Especie y Bonificador Innato",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGoldLight
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HeroSpecies.entries.forEach { speciesOption ->
                            val selected = activeHero.species == speciesOption
                            Surface(
                                onClick = { onUpdatePlayerSpecies(activeHero.index, speciesOption) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) ArcaneCardElevated else ObsidianBackground.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    width = if (selected) 1.8.dp else 1.dp,
                                    color = if (selected) ImperialGold else ParchmentBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "${speciesOption.emoji} ${speciesOption.labelEs}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) ImperialGoldLight else ParchmentLight
                                    )
                                    Text(
                                        text = speciesOption.statBonusLabelEs,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (selected) TorchAmber else ParchmentMuted
                                    )
                                }
                            }
                        }
                    }

                    // Sección C: Arquetipo Ético (Tarjetas Visuales con Bonificador de Stats)
                    Text(
                        text = "3. Vocación Ética y Especialidad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGoldLight
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EthicalArchetype.entries.forEach { archetypeOption ->
                            val selected = activeHero.archetype == archetypeOption
                            Surface(
                                onClick = { onUpdatePlayerArchetype(activeHero.index, archetypeOption) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) ArcaneCardElevated else ObsidianBackground.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    width = if (selected) 1.8.dp else 1.dp,
                                    color = if (selected) ImperialGold else ParchmentBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = archetypeOption.emoji, fontSize = 22.sp)
                                        Column {
                                            Text(
                                                text = archetypeOption.genderedTitle(activeHero.gender),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) ImperialGoldLight else ParchmentLight
                                            )
                                            Text(
                                                text = archetypeOption.coreDimensionEs,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ParchmentMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Surface(
                                        color = (if (selected) EmeraldVirtue else ImperialGold).copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(
                                            1.dp,
                                            (if (selected) EmeraldVirtue else ImperialGold).copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Text(
                                            text = archetypeOption.statBonusLabelEs,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) EmeraldVirtue else ImperialGoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Navegación entre Héroes + Botón Principal de Comenzar Aventura
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (players.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val prev = (activeIndex - 1 + players.size) % players.size
                                localTabIndex = prev
                                onSelectHeroTab(prev)
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ParchmentBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Héroe Anterior", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                val next = (activeIndex + 1) % players.size
                                localTabIndex = next
                                onSelectHeroTab(next)
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ImperialGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ImperialGoldLight),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Siguiente Héroe", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Button(
                    onClick = onLaunchCampaign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("launch_campaign_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color(0xFF07111E)
                    )
                ) {
                    Text(
                        text = "Comenzar Aventura",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp
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
}

@Composable
fun HeroStatMiniPill(
    emoji: String,
    label: String,
    value: Int,
    color: Color,
    delta: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArcaneSurfaceVariant.copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = emoji, fontSize = 12.sp)
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (delta != 0) {
                    val sign = if (delta > 0) "+$delta" else "$delta"
                    Text(
                        text = sign,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        color = if (delta > 0) EmeraldVirtue else CrimsonChaos
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = ParchmentMuted,
                maxLines = 1
            )
        }
    }
}

const val SIGNIFICANT_STAT_DELTA_THRESHOLD = 5

data class StatFlashVisualState(
    val flashAlpha: Float,
    val scale: Float,
    val recentDelta: Int,
    val isSignificant: Boolean
)

/**
 * Recuerda y ejecuta una animación de 'flash' luminoso (doble destello + pulso de escala)
 * cuando una decisión tomada afecta significativamente (|delta| >= 5) a una estadística.
 */
@Composable
fun rememberStatFlashState(
    score: Int,
    historyDelta: Int = 0,
    turnTriggerKey: Int = 0
): StatFlashVisualState {
    var previousScore by remember { mutableIntStateOf(score) }
    var previousTurnKey by remember { mutableIntStateOf(turnTriggerKey) }
    var lastSignificantDelta by remember { mutableIntStateOf(historyDelta) }
    val flashAnim = remember { Animatable(0f) }

    LaunchedEffect(score, turnTriggerKey) {
        val liveDelta = score - previousScore
        val effectiveDelta = if (liveDelta != 0) {
            liveDelta
        } else if (turnTriggerKey != previousTurnKey) {
            historyDelta
        } else {
            0
        }

        previousScore = score
        previousTurnKey = turnTriggerKey

        if (abs(effectiveDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD) {
            lastSignificantDelta = effectiveDelta
            flashAnim.snapTo(0f)
            flashAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)
            )
            flashAnim.animateTo(
                targetValue = 0.25f,
                animationSpec = tween(durationMillis = 100)
            )
            flashAnim.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
            )
            flashAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 550)
            )
        } else if (turnTriggerKey == 0 && abs(historyDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD && flashAnim.value == 0f) {
            lastSignificantDelta = historyDelta
        }
    }

    val currentDelta = if (abs(historyDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD) {
        historyDelta
    } else {
        lastSignificantDelta
    }
    val alpha = flashAnim.value
    val scale = 1f + (0.32f * alpha)

    return StatFlashVisualState(
        flashAlpha = alpha,
        scale = scale,
        recentDelta = currentDelta,
        isSignificant = abs(currentDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD
    )
}

/**
 * Calcula cuánto cambió la estadística asociada al arquetipo ético de un personaje en el último turno.
 */
fun EthicalArchetype.statDeltaFromHistory(
    current: MoralScores,
    history: List<MoralScores>
): Int {
    if (history.size < 2) return 0
    val prev = history[history.lastIndex - 1]
    return when (this) {
        EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> current.compassion - prev.compassion
        EthicalArchetype.BUSCADOR_DE_VERDAD -> current.pragmatism - prev.pragmatism
        EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> current.liberty - prev.liberty
        EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> current.justice - prev.justice
        EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> {
            val compDelta = current.compassion - prev.compassion
            val justDelta = current.justice - prev.justice
            if (abs(compDelta) >= abs(justDelta)) compDelta else justDelta
        }
    }
}

/**
 * Barra compacta de iconos de estado (Justicia, Compasión, Deber, Libertad)
 * cuyos iconos emiten un destello ('flash') animado cuando una decisión afecta significativamente las estadísticas.
 */
@Composable
fun CharacterStatusIconsBar(
    scores: MoralScores,
    history: List<MoralScores>,
    modifier: Modifier = Modifier
) {
    val prevScores = if (history.size >= 2) history[history.lastIndex - 1] else scores
    val turnKey = history.size

    val justiceDelta = scores.justice - prevScores.justice
    val compassionDelta = scores.compassion - prevScores.compassion
    val dutyDelta = scores.pragmatism - prevScores.pragmatism
    val libertyDelta = scores.liberty - prevScores.liberty

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("character_status_icons_bar"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlashingStatusIconChip(
            emoji = "⚖️",
            shortName = "JUS",
            score = scores.justice,
            historyDelta = justiceDelta,
            turnKey = turnKey,
            accentColor = SapphireOrder,
            modifier = Modifier
                .weight(1f)
                .testTag("status_icon_justice")
        )
        FlashingStatusIconChip(
            emoji = "💚",
            shortName = "COM",
            score = scores.compassion,
            historyDelta = compassionDelta,
            turnKey = turnKey,
            accentColor = EmeraldVirtue,
            modifier = Modifier
                .weight(1f)
                .testTag("status_icon_compassion")
        )
        FlashingStatusIconChip(
            emoji = "🛡️",
            shortName = "DEB",
            score = scores.pragmatism,
            historyDelta = dutyDelta,
            turnKey = turnKey,
            accentColor = AmethystWisdom,
            modifier = Modifier
                .weight(1f)
                .testTag("status_icon_duty")
        )
        FlashingStatusIconChip(
            emoji = "🦅",
            shortName = "LIB",
            score = scores.liberty,
            historyDelta = libertyDelta,
            turnKey = turnKey,
            accentColor = TorchAmber,
            modifier = Modifier
                .weight(1f)
                .testTag("status_icon_liberty")
        )
    }
}

@Composable
fun FlashingStatusIconChip(
    emoji: String,
    shortName: String,
    score: Int,
    historyDelta: Int,
    turnKey: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val flashState = rememberStatFlashState(
        score = score,
        historyDelta = historyDelta,
        turnTriggerKey = turnKey
    )
    val isPositive = historyDelta >= 0
    val flashTint = if (isPositive) EmeraldVirtue else CrimsonChaos

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (flashState.flashAlpha > 0.02f) {
            accentColor.copy(alpha = (0.18f + 0.45f * flashState.flashAlpha).coerceAtMost(0.75f))
        } else {
            ObsidianBackground.copy(alpha = 0.72f)
        },
        border = BorderStroke(
            width = if (flashState.flashAlpha > 0.05f) (1.2f + 1.6f * flashState.flashAlpha).dp else 1.dp,
            color = if (flashState.flashAlpha > 0.05f) {
                ImperialGoldLight.copy(alpha = (0.6f + 0.4f * flashState.flashAlpha).coerceAtMost(1f))
            } else if (abs(historyDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD) {
                accentColor.copy(alpha = 0.75f)
            } else {
                ParchmentBorder
            }
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = flashState.scale
                        scaleY = flashState.scale
                    }
                    .clip(CircleShape)
                    .background(
                        if (flashState.flashAlpha > 0.05f) {
                            Brush.radialGradient(
                                colors = listOf(
                                    ImperialGoldLight.copy(alpha = 0.75f * flashState.flashAlpha),
                                    accentColor.copy(alpha = 0.45f * flashState.flashAlpha),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
            ) {
                Text(
                    text = emoji,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(3.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (flashState.flashAlpha > 0.1f) ImperialGoldLight else accentColor,
                        fontSize = 11.sp
                    )
                    if (abs(historyDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD) {
                        val sign = if (historyDelta > 0) "+$historyDelta" else "$historyDelta"
                        Text(
                            text = sign,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            color = flashTint
                        )
                    }
                }
                Text(
                    text = shortName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = ParchmentMuted
                )
            }
        }
    }
}

@Composable
fun MoralCompassCard(
    scores: MoralScores,
    history: List<MoralScores> = emptyList(),
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(0) } // 0: Radar Recharts, 1: Barras
    val prevScores = if (history.size >= 2) history[history.lastIndex - 1] else scores
    val turnKey = history.size

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VALORES ÉTICOS DEL GRUPO",
                style = MaterialTheme.typography.labelSmall,
                color = ImperialGoldLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    label = { Text("🕸️ Radar", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ImperialGold.copy(alpha = 0.25f),
                        selectedLabelColor = ImperialGoldLight,
                        containerColor = ObsidianBackground.copy(alpha = 0.6f),
                        labelColor = ParchmentMuted
                    )
                )
                FilterChip(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    label = { Text("📊 Barras", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ImperialGold.copy(alpha = 0.25f),
                        selectedLabelColor = ImperialGoldLight,
                        containerColor = ObsidianBackground.copy(alpha = 0.6f),
                        labelColor = ParchmentMuted
                    )
                )
            }
        }

        if (viewMode == 0) {
            EthicalRadarChart(
                currentScores = scores,
                scoresHistory = history,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("moral_compass_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneSurface),
                border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.4f))
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = null,
                                tint = ImperialGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.moral_compass_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = ImperialGoldLight
                            )
                        }
                        Surface(
                            color = ImperialGold.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, ImperialGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = scores.dominantAlignmentEs(),
                                style = MaterialTheme.typography.labelSmall,
                                color = ImperialGoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    MoralAxisBar(
                        emoji = "⚖️",
                        label = "Justicia",
                        score = scores.justice,
                        historyDelta = scores.justice - prevScores.justice,
                        turnKey = turnKey,
                        barColor = SapphireOrder
                    )
                    MoralAxisBar(
                        emoji = "💚",
                        label = "Compasión",
                        score = scores.compassion,
                        historyDelta = scores.compassion - prevScores.compassion,
                        turnKey = turnKey,
                        barColor = EmeraldVirtue
                    )
                    MoralAxisBar(
                        emoji = "🛡️",
                        label = "Deber",
                        score = scores.pragmatism,
                        historyDelta = scores.pragmatism - prevScores.pragmatism,
                        turnKey = turnKey,
                        barColor = AmethystWisdom
                    )
                    MoralAxisBar(
                        emoji = "🦅",
                        label = "Libertad",
                        score = scores.liberty,
                        historyDelta = scores.liberty - prevScores.liberty,
                        turnKey = turnKey,
                        barColor = TorchAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun MoralAxisBar(
    emoji: String,
    label: String,
    score: Int,
    historyDelta: Int,
    turnKey: Int,
    barColor: Color
) {
    val progress by animateFloatAsState(
        targetValue = (score / 100f).coerceIn(0.05f, 1f),
        animationSpec = spring(),
        label = "axis_progress"
    )
    val flashState = rememberStatFlashState(
        score = score,
        historyDelta = historyDelta,
        turnTriggerKey = turnKey
    )

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = flashState.scale
                            scaleY = flashState.scale
                        }
                        .clip(CircleShape)
                        .background(
                            if (flashState.flashAlpha > 0.04f) {
                                barColor.copy(alpha = 0.55f * flashState.flashAlpha)
                            } else {
                                Color.Transparent
                            }
                        )
                ) {
                    Text(text = emoji, fontSize = 13.sp)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (flashState.flashAlpha > 0.1f) ImperialGoldLight else ParchmentLight
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (abs(historyDelta) >= SIGNIFICANT_STAT_DELTA_THRESHOLD) {
                    val sign = if (historyDelta > 0) "+$historyDelta" else "$historyDelta"
                    Surface(
                        color = (if (historyDelta > 0) EmeraldVirtue else CrimsonChaos).copy(
                            alpha = 0.2f + 0.4f * flashState.flashAlpha
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = sign,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (historyDelta > 0) EmeraldVirtue else CrimsonChaos,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(ObsidianBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (flashState.flashAlpha > 0.1f) ImperialGoldLight else barColor
                    )
            )
        }
    }
}

/**
 * Widget Compacto de Dados D20 — Paso 3 del Flujo Ordenado (Bloqueado a 1 único tiro por turno).
 */
@Composable
fun HonestD20InputCard(
    currentRoll: Int?,
    manualInput: String,
    isRolling: Boolean,
    isStepUnlocked: Boolean = true,
    isDiceLocked: Boolean = false,
    selectedOptionLabel: String? = null,
    onRollVirtualClick: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onConfirmPhysicalRoll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tierColor = when {
        currentRoll == null -> if (isStepUnlocked) ImperialGold else ParchmentMuted
        currentRoll in 1..5 -> CrimsonChaos
        currentRoll in 6..14 -> TorchAmber
        else -> EmeraldVirtue
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("roll_d20_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStepUnlocked) ArcaneCardElevated else ArcaneSurface.copy(alpha = 0.75f)
        ),
        border = BorderStroke(
            width = if (isDiceLocked || isStepUnlocked) 1.8.dp else 1.dp,
            color = when {
                isDiceLocked -> tierColor
                isStepUnlocked -> ImperialGold
                else -> ParchmentBorder
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val hexPath = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            lineTo(w * 0.95f, h * 0.25f)
                            lineTo(w * 0.95f, h * 0.75f)
                            lineTo(w * 0.5f, h)
                            lineTo(w * 0.05f, h * 0.75f)
                            lineTo(w * 0.05f, h * 0.25f)
                            close()
                        }
                        drawPath(path = hexPath, color = ObsidianBackground)
                        drawPath(
                            path = hexPath,
                            color = tierColor,
                            style = Stroke(width = 3.5f)
                        )
                    }
                    Text(
                        text = currentRoll?.toString() ?: "D20",
                        style = MaterialTheme.typography.titleMedium,
                        color = tierColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when {
                                isRolling -> "🎲 Lanzando D20 único..."
                                isDiceLocked && currentRoll != null -> "🔒 Tiro Único Bloqueado: D20 = $currentRoll (${rollTierLabelEs(currentRoll)})"
                                !isStepUnlocked -> "🎲 Paso 3: Tira el Dado D20 (1 solo tiro)"
                                else -> "🎲 Paso 3: Lanza tu único D20 para ejecutar la acción"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isStepUnlocked || isDiceLocked) tierColor else ParchmentMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = when {
                            isDiceLocked -> "El dado ya fue lanzado en este turno y no puede repetirse."
                            selectedOptionLabel != null -> "Acción elegida: «$selectedOptionLabel» • 1 tiro único por turno"
                            else -> "Primero selecciona la solución al problema en el Paso 2 para desbloquear el dado."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedOptionLabel != null && !isDiceLocked) ImperialGoldLight else ParchmentMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!isDiceLocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRollVirtualClick,
                        enabled = isStepUnlocked && !isRolling,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImperialGold,
                            contentColor = Color(0xFF07111E),
                            disabledContainerColor = ParchmentBorder.copy(alpha = 0.5f),
                            disabledContentColor = ParchmentMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(46.dp)
                            .testTag("virtual_d20_roll_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tirar D20 Único",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = onManualInputChange,
                        enabled = isStepUnlocked && !isRolling,
                        placeholder = { Text("Físico 1-20", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImperialGold,
                            unfocusedBorderColor = ParchmentBorder,
                            focusedTextColor = ParchmentLight,
                            unfocusedTextColor = ParchmentLight
                        ),
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("physical_d20_input")
                    )

                    if (currentRoll != null && isStepUnlocked) {
                        Button(
                            onClick = onConfirmPhysicalRoll,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldVirtue,
                                contentColor = Color(0xFF061A12)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("confirm_physical_d20_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fijar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun rollTierLabelEs(roll: Int): String = when {
    roll == 20 -> "Crítico Legendario"
    roll in 15..19 -> "Éxito Sólido"
    roll in 10..14 -> "Éxito Templado"
    roll in 6..9 -> "Éxito con Costo"
    else -> "Revés Dramático"
}
