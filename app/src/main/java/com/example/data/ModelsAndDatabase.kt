package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import java.io.File

@Serializable
enum class HeroGender(
    val labelEs: String,
    val articleIndef: String
) {
    HOMBRE(labelEs = "Hombre", articleIndef = "un"),
    MUJER(labelEs = "Mujer", articleIndef = "una")
}

@Serializable
enum class HeroSpecies(
    val labelEs: String,
    val maleForm: String,
    val femaleForm: String,
    val culturalTraitEs: String,
    val statBonusLabelEs: String,
    val emoji: String
) {
    HUMANO(
        labelEs = "Humano",
        maleForm = "humano",
        femaleForm = "humana",
        culturalTraitEs = "Versatilidad, urgencia vital ante su breve existencia y anhelo de trascendencia histórica.",
        statBonusLabelEs = "+5 Honor • +5 Audacia",
        emoji = "🧑"
    ),
    ELFO(
        labelEs = "Elfo",
        maleForm = "elfo",
        femaleForm = "elfa",
        culturalTraitEs = "Longevidad milenaria, memoria profunda de las eras y contemplación serena ante decisiones apresuradas.",
        statBonusLabelEs = "+8 Disciplina • +4 Empatía",
        emoji = "🧝"
    ),
    ENANO(
        labelEs = "Enano",
        maleForm = "enano",
        femaleForm = "enana",
        culturalTraitEs = "Visión tradicional de honor inquebrantable, lealtad al linaje y respeto sagrado por la palabra empeñada.",
        statBonusLabelEs = "+8 Honor • +6 Disciplina",
        emoji = "🧔"
    ),
    ORCO(
        labelEs = "Orco",
        maleForm = "orco",
        femaleForm = "orca",
        culturalTraitEs = "Pasión intensa, franqueza frontal sin hipocresías y defensa visceral de los suyos frente al prejuicio.",
        statBonusLabelEs = "+10 Audacia • +4 Empatía",
        emoji = "👹"
    ),
    MEDIO_ELFO(
        labelEs = "Medio Elfo",
        maleForm = "medio elfo",
        femaleForm = "medio elfa",
        culturalTraitEs = "Puente entre dos mundos, empatía hacia los marginados y búsqueda constante de pertenencia e identidad.",
        statBonusLabelEs = "+8 Empatía • +6 Audacia",
        emoji = "✨"
    );

    fun genderedNoun(gender: HeroGender): String =
        if (gender == HeroGender.MUJER) femaleForm else maleForm
}

@Serializable
enum class EthicalArchetype(
    val titleEs: String,
    val maleForm: String,
    val femaleForm: String,
    val coreDimensionEs: String,
    val philosophicalDescriptionEs: String,
    val mottoEs: String,
    val statBonusLabelEs: String,
    val emoji: String
) {
    BUSCADOR_DE_VERDAD(
        titleEs = "Buscador de Verdad",
        maleForm = "Buscador de Verdad",
        femaleForm = "Buscadora de Verdad",
        coreDimensionEs = "Sentido de Vida y Honestidad Intelectual",
        philosophicalDescriptionEs = "Desafía las apariencias, los autoengaños colectivos y las mentiras piadosas en busca de la realidad última y el sentido auténtico de la existencia.",
        mottoEs = "«Ninguna paz duradera puede edificarse sobre el velo de una mentira.»",
        statBonusLabelEs = "+10 Disciplina • +4 Honor",
        emoji = "🔮"
    ),
    DEFENSOR_DE_LA_DIGNIDAD(
        titleEs = "Defensor de la Dignidad",
        maleForm = "Defensor de la Dignidad",
        femaleForm = "Defensora de la Dignidad",
        coreDimensionEs = "Dignidad Humana Intrínseca (Imperativo Kantiano)",
        philosophicalDescriptionEs = "Reconoce en cada ser consciente un fin en sí mismo y jamás un simple medio o instrumento sacrificable por conveniencia o cálculo utilitario.",
        mottoEs = "«Cada vida posee un valor infinito que ningún cálculo de poder puede comprar.»",
        statBonusLabelEs = "+10 Empatía • +4 Honor",
        emoji = "🛡️"
    ),
    EXPLORADOR_DE_LIBERTAD(
        titleEs = "Explorador de Libertad",
        maleForm = "Explorador de Libertad",
        femaleForm = "Exploradora de Libertad",
        coreDimensionEs = "Libertad, Autonomía y Responsabilidad Moral",
        philosophicalDescriptionEs = "Defiende la autodeterminación y el libre albedrío frente al determinismo y la tiranía, asumiendo el peso moral de elegir.",
        mottoEs = "«Ser libre no es carecer de ataduras, sino responder con honor de cada paso elegido.»",
        statBonusLabelEs = "+10 Audacia • +4 Empatía",
        emoji = "🦅"
    ),
    GUARDIAN_DE_LA_CONCIENCIA(
        titleEs = "Guardián de la Conciencia",
        maleForm = "Guardián de la Conciencia",
        femaleForm = "Guardiana de la Conciencia",
        coreDimensionEs = "Justicia, Virtud Interior y Rectitud Moral",
        philosophicalDescriptionEs = "Escucha la voz interior del deber ético y la prudencia aristotélica incluso cuando la multitud o las leyes injustas exigen callar.",
        mottoEs = "«Aunque el mundo entero absuelva una injusticia, nuestra conciencia jamás debe hacerlo.»",
        statBonusLabelEs = "+10 Honor • +4 Disciplina",
        emoji = "⚖️"
    ),
    CONSTRUCTOR_DE_COMUNIDAD(
        titleEs = "Constructor de Comunidad",
        maleForm = "Constructor de Comunidad",
        femaleForm = "Constructora de Comunidad",
        coreDimensionEs = "Comunidad, Solidaridad y Bien Común",
        philosophicalDescriptionEs = "Teje vínculos de fraternidad, cuidado mutuo y reconciliación, recordando que la persona florece en el encuentro con los demás.",
        mottoEs = "«Nadie se salva en soledad; nuestro destino habita en el vínculo que cuidamos juntos.»",
        statBonusLabelEs = "+8 Empatía • +6 Honor",
        emoji = "🤝"
    );

    fun genderedTitle(gender: HeroGender): String =
        if (gender == HeroGender.MUJER) femaleForm else maleForm
}

/**
 * Estadísticas individuales de cada personaje que evolucionan cuando lidera una acción y tira el D20.
 */
@Serializable
data class HeroStats(
    val honor: Int = 55,       // Rectitud, palabra y justicia personal
    val empathy: Int = 55,     // Compasión, vínculo y sensibilidad hacia otros
    val discipline: Int = 55,  // Temple, deber y sabiduría táctica
    val audacity: Int = 55     // Iniciativa, coraje y autonomía
) {
    companion object {
        fun baseFor(species: HeroSpecies, archetype: EthicalArchetype): HeroStats {
            var honor = 50
            var empathy = 50
            var discipline = 50
            var audacity = 50

            when (species) {
                HeroSpecies.HUMANO -> {
                    honor += 5
                    audacity += 5
                }
                HeroSpecies.ELFO -> {
                    discipline += 8
                    empathy += 4
                }
                HeroSpecies.ENANO -> {
                    honor += 8
                    discipline += 6
                }
                HeroSpecies.ORCO -> {
                    audacity += 10
                    empathy += 4
                }
                HeroSpecies.MEDIO_ELFO -> {
                    empathy += 8
                    audacity += 6
                }
            }

            when (archetype) {
                EthicalArchetype.BUSCADOR_DE_VERDAD -> {
                    discipline += 10
                    honor += 4
                }
                EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> {
                    empathy += 10
                    honor += 4
                }
                EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> {
                    audacity += 10
                    empathy += 4
                }
                EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> {
                    honor += 10
                    discipline += 4
                }
                EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> {
                    empathy += 8
                    honor += 6
                }
            }

            return HeroStats(
                honor = honor.coerceIn(10, 100),
                empathy = empathy.coerceIn(10, 100),
                discipline = discipline.coerceIn(10, 100),
                audacity = audacity.coerceIn(10, 100)
            )
        }
    }
}

@Serializable
data class HeroStatDeltas(
    val honorDelta: Int = 0,
    val empathyDelta: Int = 0,
    val disciplineDelta: Int = 0,
    val audacityDelta: Int = 0
)

@Serializable
enum class GamePaceMode(
    val titleEs: String,
    val subtitleEs: String,
    val descriptionEs: String,
    val targetIdealTurns: Int,
    val badgeEmoji: String
) {
    MODO_EXPRES(
        titleEs = "MODO EXPRÉS",
        subtitleEs = "Ritmo ágil y directo",
        descriptionEs = "Plantea el dilema principal de forma directa y busca una resolución orgánica sin alargar la trama de manera innecesaria.",
        targetIdealTurns = 3,
        badgeEmoji = "⚡"
    ),
    MODO_ESTANDAR(
        titleEs = "MODO ESTÁNDAR",
        subtitleEs = "Ritmo equilibrado (Nudo y Clímax)",
        descriptionEs = "Desarrolla las consecuencias de las decisiones con un nudo claro y un clímax ético antes del cierre.",
        targetIdealTurns = 5,
        badgeEmoji = "⚔️"
    ),
    MODO_CAMPANA(
        titleEs = "MODO CAMPAÑA",
        subtitleEs = "Ritmo pausado y profundo",
        descriptionEs = "Introduce ramificaciones complejas, tensiones entre virtudes y repercusiones éticas profundas antes del desenlace.",
        targetIdealTurns = 8,
        badgeEmoji = "📜"
    )
}

/**
 * Escala de complejidad de los dilemas éticos ajustada dinámicamente según el número de votantes (1 a 6).
 */
@Serializable
enum class VoterDilemmaComplexity(
    val levelIndex: Int, // 1 a 4
    val badgeEmoji: String,
    val shortTierEs: String,
    val titleEs: String,
    val optionsPerTurn: Int,
    val stakeMultiplier: Float,
    val shortSummaryEs: String,
    val dmPromptDirectiveEs: String
) {
    SOLITARIO_INTROSPECTIVO(
        levelIndex = 1,
        badgeEmoji = "🌱",
        shortTierEs = "Introspectiva",
        titleEs = "Dilema Binario de Conciencia",
        optionsPerTurn = 2,
        stakeMultiplier = 1.0f,
        shortSummaryEs = "2 posturas polares opuestas • Decisión autónoma directa",
        dmPromptDirectiveEs = "1 VOTANTE (Complejidad Introspectiva Focalizada): Presenta exactamente 2 opciones polares en tensión directa entre dos virtudes fundamentales para un examen de conciencia personal."
    ),
    DUO_DIALECTICO(
        levelIndex = 2,
        badgeEmoji = "⚖️",
        shortTierEs = "Dialéctica",
        titleEs = "Tríada Dialéctica de Par",
        optionsPerTurn = 3,
        stakeMultiplier = 1.15f,
        shortSummaryEs = "3 posturas (Tesis, Antítesis y Síntesis) • Consenso o D20",
        dmPromptDirectiveEs = "2 VOTANTES (Complejidad Dialéctica): Presenta exactamente 3 opciones (dos posturas éticas contrapuestas y una tercera vía con costo compartido) que exijan diálogo o desempate entre los 2 participantes."
    ),
    CONSEJO_DELIBERATIVO(
        levelIndex = 3,
        badgeEmoji = "🏛️",
        shortTierEs = "Multipolar",
        titleEs = "Consejo Multipolar Cruzado",
        optionsPerTurn = 4,
        stakeMultiplier = 1.25f,
        shortSummaryEs = "4 posturas con costos éticos cruzados • Votación por mayoría",
        dmPromptDirectiveEs = "3 A 4 VOTANTES (Complejidad Multipolar de Consejo): Presenta exactamente 4 opciones con costos cruzados donde cada opción favorezca ciertos valores pero sacrifique otro, obligando a los votantes a debatir y formar mayoría."
    ),
    ASAMBLEA_FACCIONES(
        levelIndex = 4,
        badgeEmoji = "👑",
        shortTierEs = "Asamblea",
        titleEs = "Gran Asamblea de Coaliciones",
        optionsPerTurn = 5,
        stakeMultiplier = 1.40f,
        shortSummaryEs = "5 posturas de alta tensión • Coalición entre arquetipos",
        dmPromptDirectiveEs = "5 A 6 VOTANTES (Complejidad Suprema de Asamblea): Presenta 5 opciones de alta complejidad ética con tensiones entre múltiples facciones y arquetipos del grupo, donde alcanzar mayoría exige negociar sacrificios morales."
    );

    fun requiredQuorumVotes(voterCount: Int): Int {
        val clamped = voterCount.coerceIn(1, 6)
        return if (clamped == 2) 2 else (clamped / 2) + 1
    }

    fun quorumLabelEs(voterCount: Int): String {
        val clamped = voterCount.coerceIn(1, 6)
        val quorum = requiredQuorumVotes(clamped)
        return when (clamped) {
            1 -> "1/1 Autónomo"
            2 -> "2/2 Consenso"
            else -> "$quorum/$clamped Mayoría"
        }
    }

    companion object {
        fun forVoterCount(count: Int): VoterDilemmaComplexity = when (count.coerceIn(1, 6)) {
            1 -> SOLITARIO_INTROSPECTIVO
            2 -> DUO_DIALECTICO
            3, 4 -> CONSEJO_DELIBERATIVO
            else -> ASAMBLEA_FACCIONES
        }
    }
}

@Serializable
data class PlayerHero(
    val index: Int,
    val name: String,
    val gender: HeroGender,
    val species: HeroSpecies,
    val archetype: EthicalArchetype,
    val visualDescription: String = "",
    val avatarBase64: String? = null,
    val isGeneratingIdentity: Boolean = false,
    val emblemColorHex: Long = 0xFF7CC6FE,
    val stats: HeroStats = HeroStats.baseFor(species, archetype)
) {
    /**
     * Construye la descripción identitaria exacta requerida por el DM:
     * ej. "Elena, una elfa Defensora de la Dignidad" o "Grom, un orco Buscador de Verdad"
     */
    fun fullIdentityPhrase(): String {
        val cleanName = name.trim().ifEmpty { "Héroe ${index + 1}" }
        return "$cleanName, ${gender.articleIndef} ${species.genderedNoun(gender)} ${archetype.genderedTitle(gender)}"
    }

    fun shortBadgeText(): String {
        val cleanName = name.trim().ifEmpty { "Héroe ${index + 1}" }
        return "${species.emoji}${archetype.emoji} $cleanName (${species.genderedNoun(gender).replaceFirstChar { it.uppercase() }})"
    }

    /**
     * Aplica la modificación de stats del personaje basada en la solución elegida y el número del dado D20.
     */
    fun applyActionAndDiceRoll(option: DilemmaOption, diceRoll: Int): Pair<PlayerHero, HeroStatDeltas> {
        val rollFactor = when {
            diceRoll == 20 -> 1.45f
            diceRoll >= 15 -> 1.25f
            diceRoll >= 10 -> 1.0f
            diceRoll >= 6 -> 0.75f
            else -> 0.45f
        }
        val rollFlatBonus = when {
            diceRoll == 20 -> 4
            diceRoll >= 15 -> 2
            diceRoll in 6..9 -> -1
            diceRoll <= 5 -> -3
            else -> 0
        }

        fun computeStatDelta(rawOptionDelta: Int): Int {
            val scaled = if (rawOptionDelta >= 0) {
                (rawOptionDelta * rollFactor).roundToInt() + rollFlatBonus
            } else {
                val penaltyFactor = if (diceRoll >= 15) 0.7f else if (diceRoll <= 5) 1.3f else 1.0f
                (rawOptionDelta * penaltyFactor).roundToInt() + (if (diceRoll <= 5) -2 else 0)
            }
            return scaled.coerceIn(-18, 22)
        }

        val hDelta = computeStatDelta(option.justiceDelta)
        val eDelta = computeStatDelta(option.compassionDelta)
        val dDelta = computeStatDelta(option.pragmatismDelta)
        val aDelta = computeStatDelta(option.libertyDelta)

        val newStats = HeroStats(
            honor = (stats.honor + hDelta).coerceIn(5, 100),
            empathy = (stats.empathy + eDelta).coerceIn(5, 100),
            discipline = (stats.discipline + dDelta).coerceIn(5, 100),
            audacity = (stats.audacity + aDelta).coerceIn(5, 100)
        )

        val effectiveDeltas = HeroStatDeltas(
            honorDelta = newStats.honor - stats.honor,
            empathyDelta = newStats.empathy - stats.empathy,
            disciplineDelta = newStats.discipline - stats.discipline,
            audacityDelta = newStats.audacity - stats.audacity
        )

        return copy(stats = newStats) to effectiveDeltas
    }
}

@Serializable
data class DilemmaOption(
    val id: String = "opt_1",
    val label: String = "",
    val description: String = "",
    val philosophyTag: String = "Ética",
    val justiceDelta: Int = 0,
    val compassionDelta: Int = 0,
    val pragmatismDelta: Int = 0,
    val libertyDelta: Int = 0
)

@Serializable
data class DmTurnPayload(
    val chapterTitle: String = "",
    val narrativeText: String = "",
    val ethicalQuestion: String = "",
    val isStoryConcluded: Boolean = false,
    val options: List<DilemmaOption> = emptyList()
)

@Serializable
data class MoralScores(
    val justice: Int = 50,     // Justicia y Conciencia
    val compassion: Int = 50,  // Dignidad y Comunidad
    val pragmatism: Int = 50,  // Deber Moral y Sentido (Pragmatismo)
    val liberty: Int = 50      // Libertad y Autonomía
) {
    val justicia: Int get() = justice
    val compasion: Int get() = compassion
    val deber: Int get() = pragmatism
    val libertad: Int get() = liberty

    fun applyChoice(option: DilemmaOption, diceRoll: Int = 12): MoralScores {
        val rollBonus = when {
            diceRoll == 20 -> 4
            diceRoll >= 15 -> 2
            diceRoll in 6..9 -> -1
            diceRoll <= 5 -> -3
            else -> 0
        }
        fun adjust(baseDelta: Int): Int {
            if (baseDelta == 0) return 0
            return if (baseDelta > 0) {
                (baseDelta + rollBonus).coerceAtLeast(1)
            } else {
                baseDelta + (if (diceRoll <= 5) -2 else if (diceRoll >= 15) 2 else 0)
            }
        }
        return copy(
            justice = (justice + adjust(option.justiceDelta)).coerceIn(5, 100),
            compassion = (compassion + adjust(option.compassionDelta)).coerceIn(5, 100),
            pragmatism = (pragmatism + adjust(option.pragmatismDelta)).coerceIn(5, 100),
            liberty = (liberty + adjust(option.libertyDelta)).coerceIn(5, 100)
        )
    }

    fun dominantAlignmentEs(): String {
        val pairs = listOf(
            "Guardianes de la Justicia y Conciencia" to justice,
            "Baluartes de la Dignidad y Comunidad" to compassion,
            "Custodios del Deber y la Rectitud" to pragmatism,
            "Heraldos de la Libertad Responsable" to liberty
        )
        return pairs.maxByOrNull { it.second }?.first ?: "Caminantes del Equilibrio CORE"
    }
}

/**
 * Representa el resultado detallado de los Pasos 4 y 5 (Desenlace de la acción según el D20
 * y modificación de las estadísticas del personaje activo y del grupo) antes de pasar al siguiente turno.
 */
@Serializable
data class TurnOutcomeResolution(
    val turnNumber: Int,
    val heroIndex: Int,
    val heroName: String,
    val heroIdentity: String,
    val chosenOption: DilemmaOption,
    val diceRoll: Int,
    val diceTierTitleEs: String,
    val actionOutcomeNarrativeEs: String,
    val updatedHeroStats: HeroStats,
    val heroStatDeltas: HeroStatDeltas,
    val groupJusticeDelta: Int,
    val groupCompassionDelta: Int,
    val groupDutyDelta: Int,
    val groupLibertyDelta: Int
)

@Serializable
data class StoryTurnLog(
    val turnNumber: Int,
    val chapterTitle: String,
    val dmNarrative: String,
    val ethicalQuestion: String,
    val heroIdentity: String,
    val chosenOptionLabel: String,
    val chosenPhilosophy: String,
    val diceRoll: Int,
    val diceOutcomeTier: String,
    val scoresSnapshot: MoralScores? = null,
    val justiceDelta: Int = 0,
    val compassionDelta: Int = 0,
    val pragmatismDelta: Int = 0,
    val libertyDelta: Int = 0,
    val directConsequenceSummary: String = ""
)

@Serializable
data class ChronicleEntity(
    val id: Long = 0,
    val realmTitle: String, // Guarda el título del capítulo final o modalidad procedimental
    val playerCount: Int,
    val partySummary: String,
    val dominantAlignment: String,
    val turnsCompleted: Int,
    val justiceScore: Int,
    val compassionScore: Int,
    val pragmatismScore: Int,
    val libertyScore: Int,
    val lastNarrativeSnippet: String,
    val fullTranscriptJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

interface ChronicleDao {
    fun getAllChronicles(): Flow<List<ChronicleEntity>>
    suspend fun insertChronicle(chronicle: ChronicleEntity): Long
    suspend fun deleteChronicleById(id: Long)
}

class DungeonsDatabase private constructor(context: Context) : ChronicleDao {

    private val storageFile = File(context.applicationContext.filesDir, "dungeons_chronicles_v2.json")
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _chroniclesFlow = MutableStateFlow<List<ChronicleEntity>>(loadInitialList())

    private fun loadInitialList(): List<ChronicleEntity> {
        if (!storageFile.exists()) return emptyList()
        return try {
            val raw = storageFile.readText()
            if (raw.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<ChronicleEntity>>(raw).sortedByDescending { it.timestamp }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistList(items: List<ChronicleEntity>) {
        try {
            val sorted = items.sortedByDescending { it.timestamp }
            storageFile.writeText(json.encodeToString(sorted))
            _chroniclesFlow.value = sorted
        } catch (_: Exception) {
        }
    }

    fun chronicleDao(): ChronicleDao = this

    override fun getAllChronicles(): Flow<List<ChronicleEntity>> = _chroniclesFlow.asStateFlow()

    override suspend fun insertChronicle(chronicle: ChronicleEntity): Long = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _chroniclesFlow.value.toMutableList()
            val assignedId = if (chronicle.id != 0L) {
                chronicle.id
            } else {
                (current.maxOfOrNull { it.id } ?: 0L) + 1L
            }
            val updatedItem = chronicle.copy(id = assignedId)
            current.removeAll { it.id == assignedId }
            current.add(0, updatedItem)
            persistList(current)
            assignedId
        }
    }

    override suspend fun deleteChronicleById(id: Long) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _chroniclesFlow.value.filterNot { it.id == id }
            persistList(current)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DungeonsDatabase? = null

        fun getDatabase(context: Context): DungeonsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = DungeonsDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

class ChronicleRepository(private val dao: ChronicleDao) {
    val allChronicles: Flow<List<ChronicleEntity>> = dao.getAllChronicles()

    suspend fun saveChronicle(chronicle: ChronicleEntity): Long = dao.insertChronicle(chronicle)

    suspend fun deleteChronicle(id: Long) = dao.deleteChronicleById(id)
}
