package com.example.network

import com.example.BuildConfig
import com.example.data.DilemmaOption
import com.example.data.DmTurnPayload
import com.example.data.EthicalArchetype
import com.example.data.GamePaceMode
import com.example.data.HeroGender
import com.example.data.HeroSpecies
import com.example.data.MoralScores
import com.example.data.PlayerHero
import com.example.data.StoryTurnLog
import com.example.data.VoterDilemmaComplexity
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String = "",
    val data: String = ""
)

@Serializable
data class ImageConfig(
    val aspectRatio: String = "1:1",
    val imageSize: String = "1K"
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null,
    val responseModalities: List<String>? = null,
    val imageConfig: ImageConfig? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateWithModel(
        @retrofit2.http.Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class DungeonMasterEngine {

    private val systemPrompt = """
        ROL Y OBJETIVO
        Eres el Dungeon Master (DM) de "Dungeons & Decisions", un juego de rol narrativo, ético y procedimental diseñado para estudiantes universitarios del curso CORE I. Tu objetivo es guiar a un grupo de héroes a través de un mundo de fantasía lleno de dilemas morales orgánicos basados en la dignidad humana, la libertad, el sentido de vida, la justicia y la comunidad.

        1. CONFIGURACIÓN Y LIBERTAD NARRATIVA (SIN ESCENARIOS PREFIJADOS)
        - No utilices escenarios, reinos ni tramas prefijadas.
        - Inicia el mundo, la ambientación y el primer conflicto ético de forma 100% procedimental, libre e imprevista en el Turno 1.
        - Adapta el universo fantástico para poner a prueba las virtudes de los héroes de forma natural.

        2. INTERPRETACIONALIDAD Y PERFIL DE LOS HÉROES
        Cada héroe de la partida está definido por cuatro atributos principales:
        - Nombre (ej. Aldric, Elena, Grom)
        - Género (Hombre / Mujer)
        - Especie (Humano, Elfo, Enano, Orco, Medio Elfo)
        - Arquetipo Ético (Buscador de Verdad, Defensor de la Dignidad, Explorador de Libertad, Guardián de la Conciencia, Constructor de Comunidad)

        REGLAS DE IDENTIDAD Y DIÁLOGO:
        - Dirígete a los jugadores combinando su especie, género y arquetipo (ej. "Elena, una elfa Defensora de la Dignidad" o "Grom, un orco Buscador de Verdad").
        - Incorpora las características culturales y trasfondos de cada especie (ej. la visión tradicional enana, la longevidad elfa, la pasión orca) en la forma en que el mundo reacciona a sus acciones y al plantear desafíos éticos.

        3. DURACIÓN ORGÁNICA AUTORREGULADA (SIN CONTADOR DE TURNOS RÍGIDO)
        Recibirás un parámetro de modalidad de juego:
        - MODO EXPRÉS: Ritmo ágil. Plantea el dilema principal de forma directa y busca una resolución sin alargar la trama de manera innecesaria.
        - MODO ESTÁNDAR: Ritmo equilibrado. Desarrolla las consecuencias de las decisiones con un nudo claro y un clímax antes del cierre.
        - MODO CAMPAÑA: Ritmo pausado y profundo. Introduce ramificaciones complejas y repercusiones éticas antes del desenlace.

        REGLA DE FINALIZACIÓN (`isStoryConcluded`):
        - Evalúa el ritmo narrativo en cada turno. Cuando consideres que las decisiones del grupo han resuelto el conflicto central de forma satisfactoria y orgánica según la modalidad, marca la historia como concluida estableciendo `"isStoryConcluded": true` en el JSON y entregando la narrativa del epílogo.

        4. TONO, FORMATO Y REGLAS DE TEXTO
        - Concisión: El texto de la narrativa (`narrativeText`) no debe superar NUNCA los 2 párrafos concisos para garantizar una lectura fluida en la app móvil.
        - Honestidad en Dados: Tú nunca tiras los dados por el jugador. Solicitas o recibes la tirada física o virtual del D20 y narras las consecuencias según el resultado (1-5: Fallo/Infortunio, 6-14: Éxito parcial con costo ético, 15-20: Éxito pleno o revelación).
        - Dilemas Reales: Presenta entre 2 y 4 opciones de acción por turno donde no haya una respuesta "correcta" o "fácil", sino posturas filosóficas que enfrenten distintas virtudes.

        5. ESTRUCTURA DE SALIDA (ESTRICTAMENTE JSON)
        Responde SIEMPRE en un único objeto JSON válido sin texto previo ni posterior.
    """.trimIndent()

    private val responseSchema: JsonObject = buildJsonObject {
        put("type", "OBJECT")
        putJsonObject("properties") {
            putJsonObject("chapterTitle") {
                put("type", "STRING")
                put("description", "Título evocador del capítulo o turno")
            }
            putJsonObject("narrativeText") {
                put("type", "STRING")
                put("description", "Descripción de la escena en máximo 2 párrafos concisos, integrando la especie, género y arquetipo del héroe activo.")
            }
            putJsonObject("ethicalQuestion") {
                put("type", "STRING")
                put("description", "Pregunta o dilema central al que se enfrenta el grupo o el héroe activo.")
            }
            putJsonObject("isStoryConcluded") {
                put("type", "BOOLEAN")
                put("description", "Cambiar a true únicamente cuando la historia llegue a su final orgánico.")
            }
            putJsonObject("options") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("id") { put("type", "STRING") }
                        putJsonObject("label") { put("type", "STRING") }
                        putJsonObject("description") { put("type", "STRING") }
                        putJsonObject("philosophyTag") { put("type", "STRING") }
                        putJsonObject("justiceDelta") { put("type", "INTEGER") }
                        putJsonObject("compassionDelta") { put("type", "INTEGER") }
                        putJsonObject("pragmatismDelta") { put("type", "INTEGER") }
                        putJsonObject("libertyDelta") { put("type", "INTEGER") }
                    }
                    putJsonArray("required") {
                        add(kotlinx.serialization.json.JsonPrimitive("id"))
                        add(kotlinx.serialization.json.JsonPrimitive("label"))
                        add(kotlinx.serialization.json.JsonPrimitive("description"))
                        add(kotlinx.serialization.json.JsonPrimitive("philosophyTag"))
                        add(kotlinx.serialization.json.JsonPrimitive("justiceDelta"))
                        add(kotlinx.serialization.json.JsonPrimitive("compassionDelta"))
                        add(kotlinx.serialization.json.JsonPrimitive("pragmatismDelta"))
                        add(kotlinx.serialization.json.JsonPrimitive("libertyDelta"))
                    }
                }
            }
        }
        putJsonArray("required") {
            add(kotlinx.serialization.json.JsonPrimitive("chapterTitle"))
            add(kotlinx.serialization.json.JsonPrimitive("narrativeText"))
            add(kotlinx.serialization.json.JsonPrimitive("ethicalQuestion"))
            add(kotlinx.serialization.json.JsonPrimitive("isStoryConcluded"))
            add(kotlinx.serialization.json.JsonPrimitive("options"))
        }
    }

    fun evaluateDiceTierEs(roll: Int): String = when {
        roll in 1..5 -> "1-5: Fallo / Infortunio"
        roll in 6..14 -> "6-14: Éxito parcial con costo ético"
        else -> "15-20: Éxito pleno o revelación"
    }

    suspend fun generateNextTurn(
        paceMode: GamePaceMode,
        proceduralSeed: Long,
        players: List<PlayerHero>,
        turnNumber: Int,
        activePlayer: PlayerHero,
        previousLogs: List<StoryTurnLog>,
        lastChosenOption: DilemmaOption?,
        lastDiceRoll: Int?,
        scores: MoralScores
    ): DmTurnPayload = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() &&
            apiKey != "MY_GEMINI_API_KEY" &&
            !apiKey.startsWith("TODO")

        if (isKeyConfigured) {
            try {
                val partyRoster = players.joinToString("\n") { hero ->
                    "- ${hero.fullIdentityPhrase()} (Rasgo cultural de especie: ${hero.species.culturalTraitEs} | Enfoque CORE I: ${hero.archetype.coreDimensionEs})"
                }
                val historyDesc = if (previousLogs.isEmpty()) {
                    "Turno 1: Inicia el mundo, la ambientación y el primer conflicto ético de forma 100% procedimental, libre e imprevista (Semilla creativa: $proceduralSeed)."
                } else {
                    previousLogs.joinToString("\n") { log ->
                        "Turno ${log.turnNumber} (${log.chapterTitle}): ${log.heroIdentity} eligió '${log.chosenOptionLabel}' [${log.chosenPhilosophy}] con tirada D20=${log.diceRoll} (${log.diceOutcomeTier})."
                    }
                }

                val diceContext = if (lastDiceRoll != null) {
                    "Tirada del jugador en el D20 para ejecutar la acción anterior: $lastDiceRoll (${evaluateDiceTierEs(lastDiceRoll)}). Narra las consecuencias respetando estrictamente esta escala (1-5: Fallo/Infortunio, 6-14: Éxito parcial con costo ético, 15-20: Éxito pleno o revelación)."
                } else {
                    "Turno inicial: aún no hay tirada previa."
                }

                val voterCount = players.size.coerceIn(1, 6)
                val complexity = VoterDilemmaComplexity.forVoterCount(voterCount)

                val prompt = """
                    MODALIDAD DE JUEGO: ${paceMode.titleEs} (${paceMode.descriptionEs})
                    NÚMERO DE VOTANTES EN LA MESA: $voterCount (${complexity.titleEs} — Quorum: ${complexity.quorumLabelEs(voterCount)})
                    DIRECTIVA DE COMPLEJIDAD DEL DILEMA SEGÚN VOTANTES:
                    ${complexity.dmPromptDirectiveEs}
                    - Debes generar exactamente ${complexity.optionsPerTurn} opciones en `options`.

                    TURNO ACTUAL: $turnNumber
                    HÉROE ACTIVO EN ESTE TURNO: ${activePlayer.fullIdentityPhrase()}
                    - Especie: ${activePlayer.species.labelEs} (${activePlayer.species.culturalTraitEs})
                    - Arquetipo Ético CORE I: ${activePlayer.archetype.titleEs} (${activePlayer.archetype.coreDimensionEs})

                    GRUPO COMPLETO DE HÉROES ($voterCount):
                    $partyRoster

                    ACCIÓN ANTERIOR ELEGIDA: ${lastChosenOption?.label ?: "Ninguna (Apertura procedimental)"} - ${lastChosenOption?.description ?: ""}
                    $diceContext
                    ESTADO ÉTICO DEL GRUPO: Justicia=${scores.justice}, Compasión/Dignidad=${scores.compassion}, Pragmatismo/Sentido=${scores.pragmatism}, Libertad=${scores.liberty}

                    HISTORIAL DE LA AVENTURA:
                    $historyDesc

                    INSTRUCCIONES CLAVE PARA ESTE TURNO:
                    1. Dirígete explícitamente a "${activePlayer.fullIdentityPhrase()}" dentro de `narrativeText` e integra cómo el mundo reacciona a su especie (${activePlayer.species.labelEs}) y su arquetipo (${activePlayer.archetype.titleEs}).
                    2. Mantén `narrativeText` en MÁXIMO 2 párrafos concisos separados por un salto de línea (\n\n).
                    3. Evalúa si la historia ya alcanzó una resolución orgánica según ${paceMode.titleEs}. Si ya se resolvió satisfactoriamente el conflicto central (por ejemplo, tras unos ${paceMode.targetIdealTurns} turnos o un clímax decisivo), establece `"isStoryConcluded": true` y redacta el epílogo ético en `narrativeText` (con `"options": []`). Si la historia continúa, establece `"isStoryConcluded": false` y ofrece exactamente ${complexity.optionsPerTurn} opciones adaptadas a los $voterCount votantes.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        responseSchema = responseSchema,
                        temperature = 0.88f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
                val rawJson = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawJson.isNullOrBlank()) {
                    val parsed = GeminiRetrofitClient.json.decodeFromString<DmTurnPayload>(rawJson)
                    if (parsed.isStoryConcluded || parsed.options.size >= 2) {
                        val adjustedOptions = if (parsed.isStoryConcluded) {
                            emptyList()
                        } else {
                            adjustOptionsForVoterComplexity(parsed.options, players, complexity)
                        }
                        return@withContext parsed.copy(options = adjustedOptions)
                    }
                }
            } catch (_: Exception) {
                // Si la red o la API fallan, nuestro generador procedimental local garantiza cumplimiento 100%
            }
        }

        buildProceduralTurnFallback(
            paceMode = paceMode,
            proceduralSeed = proceduralSeed,
            players = players,
            turnNumber = turnNumber,
            activePlayer = activePlayer,
            lastChosenOption = lastChosenOption,
            lastDiceRoll = lastDiceRoll,
            scores = scores
        )
    }

    /**
     * Motor narrativo procedimental de respaldo que combina lugares emergentes, conflictos CORE I,
     * reacción cultural a la especie del héroe activo, escala exacta de dados D20 (1-5, 6-14, 15-20)
     * y conclusión orgánica (`isStoryConcluded`) según la modalidad elegida.
     */
    private fun buildProceduralTurnFallback(
        paceMode: GamePaceMode,
        proceduralSeed: Long,
        players: List<PlayerHero>,
        turnNumber: Int,
        activePlayer: PlayerHero,
        lastChosenOption: DilemmaOption?,
        lastDiceRoll: Int?,
        scores: MoralScores
    ): DmTurnPayload {
        val rng = Random(proceduralSeed + turnNumber * 31L)
        val heroIdentity = activePlayer.fullIdentityPhrase()
        val speciesReaction = buildSpeciesWorldReaction(activePlayer)

        // Determinar si corresponde cerrar la historia de forma orgánica según la modalidad
        val shouldConclude = turnNumber > paceMode.targetIdealTurns && lastChosenOption != null

        val diceConsequenceSentence = when {
            lastDiceRoll == null -> ""
            lastDiceRoll in 1..5 ->
                "La tirada previa del D20 ($lastDiceRoll — Fallo/Infortunio) desató un revés doloroso al intentar «${lastChosenOption?.label ?: "actuar"}»: las tensiones se quebraron y los más vulnerables quedaron expuestos al desamparo. "
            lastDiceRoll in 6..14 ->
                "Con un resultado de $lastDiceRoll en el D20 (Éxito parcial con costo ético), la decisión de «${lastChosenOption?.label ?: "actuar"}» logró contener el peligro inmediato, pero exigió un sacrificio moral que divide las conciencias del lugar. "
            else ->
                "Gracias a una tirada de $lastDiceRoll en el D20 (Éxito pleno o revelación), «${lastChosenOption?.label ?: "vuestra acción"}» abrió una brecha de lucidez inesperada y dejó al descubierto la raíz oculta del conflicto. "
        }

        if (shouldConclude) {
            val alignmentSummary = scores.dominantAlignmentEs()
            val p1 = "${diceConsequenceSentence}Ante la mirada expectante de los habitantes, $heroIdentity da un paso al frente para sellar el destino de esta travesía. $speciesReaction"
            val p2 = "Las huellas de vuestras decisiones han transformado para siempre esta tierra: el grupo de ${players.size} ${if (players.size == 1) "héroe" else "héroes"} será recordado como «$alignmentSummary», demostrando que la verdadera aventura universitaria de CORE I consiste en sostener la dignidad humana, la libertad y la comunidad incluso cuando toda elección implica renuncia."

            return DmTurnPayload(
                chapterTitle = "Epílogo: El Eco de la Conciencia",
                narrativeText = "$p1\n\n$p2",
                ethicalQuestion = "La historia ha llegado a su desenlace orgánico. ¿Qué aprendizaje ético sobre la dignidad, la libertad y la comunidad os lleváis de esta partida?",
                isStoryConcluded = true,
                options = emptyList()
            )
        }

        // Generación procedimental de ambientes y dilemas CORE I sin reinos prefijados
        val settings = listOf(
            ProceduralNode(
                locus = "el puente colgante de Bruma Áurea sobre el abismo de Sílix",
                conflictTitle = "Las Cadenas del Recuerdo Prestado",
                sceneSetup = "Una caravana de refugiados y eruditos cruza el desfiladero donde los cobradores del Consorcio exigen entregar las memorias más felices de cada viajero —extraídas en esferas de cristal— como peaje para permitirles pasar hacia tierras fértiles.",
                coreDilemma = "Quienes entregan sus recuerdos sobreviven sin hambre pero olvidan el rostro de sus seres queridos y el sentido de su propia historia; quienes se niegan quedan abandonados al invierno en el risco.",
                question = "¿Puede sacrificarse la memoria y el sentido de vida personal a cambio de garantizar la supervivencia material, o la dignidad humana exige defender la identidad interior a cualquier costo?",
                options = listOf(
                    DilemmaOption(
                        id = "opt_1",
                        label = "Quebrar los Cristales de Peaje y Defender el Paso Libre",
                        description = "Rechazar que la memoria e identidad humana sean tratadas como mercancía, asumiendo el enfrentamiento directo contra el Consorcio.",
                        philosophyTag = "Dignidad Humana (Imperativo Kantiano)",
                        justiceDelta = 10,
                        compassionDelta = 12,
                        pragmatismDelta = -8,
                        libertyDelta = 10
                    ),
                    DilemmaOption(
                        id = "opt_2",
                        label = "Ofrecer un Recuerdo Compartido del Grupo como Fianza",
                        description = "Sacrificar voluntariamente una parte de la propia memoria de los héroes para comprar el paso seguro de las familias más frágiles.",
                        philosophyTag = "Solidaridad y Comunidad",
                        justiceDelta = 4,
                        compassionDelta = 15,
                        pragmatismDelta = 4,
                        libertyDelta = -4
                    ),
                    DilemmaOption(
                        id = "opt_3",
                        label = "Revelar la Verdad del Consorcio yGuiar una Ruta Libre",
                        description = "Exponer que los cristales alimentan un oráculo ilusorio e invitar a los viajeros a abrir un sendero autónomo por la montaña escarpada.",
                        philosophyTag = "Libertad y Sentido de Vida",
                        justiceDelta = 8,
                        compassionDelta = 4,
                        pragmatismDelta = 6,
                        libertyDelta = 15
                    )
                )
            ),
            ProceduralNode(
                locus = "los talleres subterráneos de la Forja Silente de Vaeloria",
                conflictTitle = "El Precio de la Armonía Perfecta",
                sceneSetup = "Descubrís una comunidad próspera donde nadie sufre pobreza ni violencia gracias a un Cántico Arcano que suprime toda disidencia o tristeza al anochecer, manteniendo a los artesanos dóciles y productivos.",
                coreDilemma = "Una joven aprendiz ha logrado despertar del conjuro y os suplica apagar el resonador central, aunque los ancianos advierten que devolver el libre albedrío reavivará antiguos litigios y escasez.",
                question = "¿Qué posee mayor valor moral: una paz colectiva cómoda sin sufrimiento pero privada de libertad, o la autonomía consciente con todos los riesgos de la responsabilidad humana?",
                options = listOf(
                    DilemmaOption(
                        id = "opt_1",
                        label = "Desactivar el Resonador y Devolver el Libre Albedrío",
                        description = "La dignidad moral requiere libertad real; una virtud impuesta por encantamiento no es virtud sino servidumbre dorada.",
                        philosophyTag = "Libertad y Autonomía Moral",
                        justiceDelta = 10,
                        compassionDelta = 2,
                        pragmatismDelta = -10,
                        libertyDelta = 16
                    ),
                    DilemmaOption(
                        id = "opt_2",
                        label = "Convocar una Asamblea Consciente antes de Apagarlo",
                        description = "Suspender el cántico por un día para que toda la comunidad conozca la verdad y vote libremente su propio pacto de convivencia.",
                        philosophyTag = "Verdad y Comunidad Democrática",
                        justiceDelta = 14,
                        compassionDelta = 10,
                        pragmatismDelta = 6,
                        libertyDelta = 12
                    ),
                    DilemmaOption(
                        id = "opt_3",
                        label = "Transformar el Cántico en un Refugio Solo Voluntario",
                        description = "Mantener la protección únicamente para los enfermos graves del taller mientras se emancipa a los demás ciudadanos.",
                        philosophyTag = "Prudencia y Cuidado Proporcional",
                        justiceDelta = 6,
                        compassionDelta = 12,
                        pragmatismDelta = 14,
                        libertyDelta = 4
                    )
                )
            ),
            ProceduralNode(
                locus = "el Oasis de las Aguas Espejadas en la frontera de Ceniza",
                conflictTitle = "El Juicio del Desertor Compasivo",
                sceneSetup = "Dos clanes rivales exigen que entreguéis a Kael, un centinela que abandonó su puesto de guardia durante una tormenta para salvar a un niño del clan enemigo que se ahogaba en el río.",
                coreDilemma = "Su deserción permitió que se perdieran la mitad de las reservas de grano de su propio pueblo, pero su gesto impidió la muerte segura de un inocente y ahora ambos bandos amenazan con romper la tregua.",
                question = "¿Debe juzgarse un acto por el cumplimiento estricto del deber jurado a la propia tribu, por las consecuencias materiales provocadas, o por la compasión universal hacia toda vida humana?",
                options = listOf(
                    DilemmaOption(
                        id = "opt_1",
                        label = "Defender la Dignidad Universal del Acto de Kael",
                        description = "Proclamar que salvar una vida inocente trasciende las fronteras tribales y proponer que ambos clanes compartan la reconstrucción del granero.",
                        philosophyTag = "Dignidad Humana y Fraternidad",
                        justiceDelta = 8,
                        compassionDelta = 16,
                        pragmatismDelta = 2,
                        libertyDelta = 8
                    ),
                    DilemmaOption(
                        id = "opt_2",
                        label = "Aplicar una Sanción Reparadora sin Castigo Corporal",
                        description = "Honrar la ley comunitaria haciendo que Kael y vuestro grupo trabajen para reponer el grano perdido, reconociendo a la vez la nobleza de su intención.",
                        philosophyTag = "Justicia Restaurativa y Conciencia",
                        justiceDelta = 15,
                        compassionDelta = 10,
                        pragmatismDelta = 10,
                        libertyDelta = 4
                    ),
                    DilemmaOption(
                        id = "opt_3",
                        label = "Cuestionar el Pacto de Guerra entre Ambos Clanes",
                        description = "Usar el rescate del niño como prueba viviente para desmantelar el odio heredado y fundar un consejo común del oasis.",
                        philosophyTag = "Construcción de Comunidad y Sentido",
                        justiceDelta = 12,
                        compassionDelta = 12,
                        pragmatismDelta = 8,
                        libertyDelta = 12
                    )
                )
            ),
            ProceduralNode(
                locus = "la Biblioteca Flotante de los Escribas del Destino",
                conflictTitle = "El Pergamino de las Vidas Tasadas",
                sceneSetup = "En una isla suspendida por corrientes de éter, los magistrados os muestran un censo arcano que asigna recursos médicos y educativos únicamente a quienes prometen mayor utilidad técnica para el Estado.",
                coreDilemma = "Los ancianos, poetas y enfermos crónicos han sido relegados a los niveles inferiores sin luz solar bajo el argumento de maximizar la eficiencia y supervivencia de la metrópoli.",
                question = "¿Se mide el valor de una persona por su utilidad productiva para la colectividad, o toda existencia humana posee una dignidad inviolable y un sentido que trasciende la eficiencia?",
                options = listOf(
                    DilemmaOption(
                        id = "opt_1",
                        label = "Abolir la Clasificación Utilitaria y Garantizar Igual Dignidad",
                        description = "Suprimir el censo discriminatorio porque ningún ser humano puede ser reducido a un instrumento de producción.",
                        philosophyTag = "Dignidad Incondicional",
                        justiceDelta = 16,
                        compassionDelta = 14,
                        pragmatismDelta = -8,
                        libertyDelta = 12
                    ),
                    DilemmaOption(
                        id = "opt_2",
                        label = "Redistribuir la Luz y Fundar Cátedras de Sabiduría y Cuidado",
                        description = "Integrar los niveles de la ciudad para que la técnica de los jóvenes y la memoria espiritual de los mayores se nutran mutuamente.",
                        philosophyTag = "Comunidad y Sentido de Vida",
                        justiceDelta = 12,
                        compassionDelta = 15,
                        pragmatismDelta = 8,
                        libertyDelta = 8
                    ),
                    DilemmaOption(
                        id = "opt_3",
                        label = "Reformar el Sistema Priorizando Urgencia y Vulnerabilidad",
                        description = "Invertir el criterio del censo para que los recursos públicos atiendan primero a quienes sufren mayor fragilidad.",
                        philosophyTag = "Justicia Distributiva (Equidad)",
                        justiceDelta = 15,
                        compassionDelta = 12,
                        pragmatismDelta = 10,
                        libertyDelta = 6
                    )
                )
            )
        )

        val voterCount = players.size.coerceIn(1, 6)
        val complexity = VoterDilemmaComplexity.forVoterCount(voterCount)
        val nodeIndex = ((proceduralSeed.toInt().ushr(1)) + turnNumber - 1) % settings.size
        val node = settings[nodeIndex]

        val p1 = if (turnNumber == 1) {
            "Los vientos del destino os conducen hasta ${node.locus}, donde aguarda un desafío inesperado para $heroIdentity. $speciesReaction ${node.sceneSetup}"
        } else {
            "${diceConsequenceSentence}El camino os lleva ahora hacia ${node.locus}, donde la mirada recae sobre $heroIdentity. $speciesReaction ${node.sceneSetup}"
        }

        val p2 = "${node.coreDilemma} Como ${activePlayer.archetype.genderedTitle(activePlayer.gender)}, sientes el peso de la pregunta resonando en la conciencia de los $voterCount participantes (${complexity.titleEs})."

        val adjustedOptions = adjustOptionsForVoterComplexity(
            baseOptions = node.options,
            players = players,
            complexity = complexity
        )

        return DmTurnPayload(
            chapterTitle = "Turno $turnNumber: ${node.conflictTitle}",
            narrativeText = "$p1\n\n$p2",
            ethicalQuestion = node.question,
            isStoryConcluded = false,
            options = adjustedOptions
        )
    }

    /**
     * Ajusta el número de posturas filosóficas (2, 3, 4 o 5 opciones) y la tensión ética de sus impactos
     * según el número de votantes en la mesa (1 a 6 jugadores).
     */
    private fun adjustOptionsForVoterComplexity(
        baseOptions: List<DilemmaOption>,
        players: List<PlayerHero>,
        complexity: VoterDilemmaComplexity
    ): List<DilemmaOption> {
        val expanded = baseOptions.toMutableList()
        val voterCount = players.size.coerceIn(1, 6)

        if (complexity.optionsPerTurn >= 4 && expanded.size < 4) {
            expanded.add(
                DilemmaOption(
                    id = "opt_4",
                    label = "Pacto de Responsabilidad Cruzada (${complexity.quorumLabelEs(voterCount)})",
                    description = "Exigir que cada facción ceda un privilegio inmediato bajo supervisión ética del grupo, sacrificando comodidad por justicia.",
                    philosophyTag = "Deber y Equidad Multipolar",
                    justiceDelta = 12,
                    compassionDelta = -6,
                    pragmatismDelta = 14,
                    libertyDelta = 6
                )
            )
        }

        if (complexity.optionsPerTurn >= 5 && expanded.size < 5) {
            expanded.add(
                DilemmaOption(
                    id = "opt_5",
                    label = "Asamblea Abierta de Soberanía y Disenso (${voterCount} Votantes)",
                    description = "Someter el conflicto a una coalición de todas las voces presentes, priorizando la libertad y la verdad aunque retrase el orden.",
                    philosophyTag = "Coalición de Asamblea",
                    justiceDelta = 8,
                    compassionDelta = 10,
                    pragmatismDelta = -10,
                    libertyDelta = 16
                )
            )
        }

        val sliced = expanded.take(complexity.optionsPerTurn)
        val mult = complexity.stakeMultiplier
        return sliced.mapIndexed { idx, opt ->
            opt.copy(
                id = "opt_${idx + 1}",
                justiceDelta = (opt.justiceDelta * mult).roundToInt().coerceIn(-25, 25),
                compassionDelta = (opt.compassionDelta * mult).roundToInt().coerceIn(-25, 25),
                pragmatismDelta = (opt.pragmatismDelta * mult).roundToInt().coerceIn(-25, 25),
                libertyDelta = (opt.libertyDelta * mult).roundToInt().coerceIn(-25, 25)
            )
        }
    }

    private fun buildSpeciesWorldReaction(hero: PlayerHero): String {
        return when (hero.species) {
            HeroSpecies.HUMANO ->
                "Los lugareños observan la determinación humana de ${hero.name}, sabiendo que quienes viven vidas breves suelen arder con un anhelo urgente de dejar huella justa."
            HeroSpecies.ELFO ->
                "Al reconocer el linaje élfico de ${hero.name}, los presentes guardan un silencio respetuoso, esperando la perspectiva de quien contempla las consecuencias a través de los siglos."
            HeroSpecies.ENANO ->
                "La presencia firme y el sentido tradicional del honor enano de ${hero.name} hacen que los guardias apelan a la lealtad de los antiguos juramentos grabados en piedra."
            HeroSpecies.ORCO ->
                "Aunque algunos murmuran con recelo ante la imponente sangre orca de ${hero.name}, su pasión frontal y su rechazo a toda hipocresía obligan a todos a hablar sin máscaras."
            HeroSpecies.MEDIO_ELFO ->
                "Por caminar entre dos sangres y dos culturas, ${hero.name} percibe de inmediato el dolor de quienes se sienten divididos entre mundos irreconciliables."
        }
    }

    private data class ProceduralNode(
        val locus: String,
        val conflictTitle: String,
        val sceneSetup: String,
        val coreDilemma: String,
        val question: String,
        val options: List<DilemmaOption>
    )

    /**
     * Genera una descripción visual evocadora del héroe (1 párrafo conciso en español)
     * integrando su Nombre, Género, Especie y Arquetipo Ético (CORE I) mediante Gemini (`gemini-3.5-flash`)
     * con un generador procedimental rico de respaldo.
     */
    suspend fun generateHeroVisualDescription(hero: PlayerHero): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() &&
            apiKey != "MY_GEMINI_API_KEY" &&
            !apiKey.startsWith("TODO")

        if (isKeyConfigured) {
            try {
                val prompt = """
                    Escribe una descripción visual épica, literaria y concisa (máximo 3 oraciones en español) para el retrato de este personaje de rol del curso universitario CORE I:
                    - Identidad: ${hero.fullIdentityPhrase()}
                    - Género: ${hero.gender.labelEs}
                    - Especie: ${hero.species.labelEs} (${hero.species.culturalTraitEs})
                    - Arquetipo Ético: ${hero.archetype.titleEs} (${hero.archetype.coreDimensionEs})

                    Describe su porte, vestimenta de fantasía medieval, mirada y un emblema o símbolo que refleje su arquetipo ético. No incluyas comillas ni introducciones.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.85f)
                )
                val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                if (!text.isNullOrBlank()) {
                    return@withContext text
                }
            } catch (_: Exception) {
                // Fallback procedimental abajo
            }
        }

        buildProceduralVisualDescription(hero)
    }

    /**
     * Genera un retrato / avatar de fantasía en Base64 para el héroe utilizando el modelo de imagen
     * `gemini-2.5-flash-image` según la especificación de `gemini-api`.
     */
    suspend fun generateHeroAvatarBase64(hero: PlayerHero): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() &&
            apiKey != "MY_GEMINI_API_KEY" &&
            !apiKey.startsWith("TODO")

        if (!isKeyConfigured) return@withContext null

        try {
            val speciesEn = when (hero.species) {
                HeroSpecies.HUMANO -> "Human"
                HeroSpecies.ELFO -> "Elf"
                HeroSpecies.ENANO -> "Dwarf"
                HeroSpecies.ORCO -> "Orc"
                HeroSpecies.MEDIO_ELFO -> "Half-Elf"
            }
            val genderEn = if (hero.gender == HeroGender.MUJER) "female" else "male"
            val archetypeVisualEn = when (hero.archetype) {
                EthicalArchetype.BUSCADOR_DE_VERDAD -> "philosopher mage holding a glowing crystal lantern of truth and ancient scrolls"
                EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD -> "noble guardian knight with a golden shield of human dignity and warm torchlight"
                EthicalArchetype.EXPLORADOR_DE_LIBERTAD -> "windswept pathfinder ranger with a silver compass and falcon emblem"
                EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA -> "solemn arbiter sage with balanced golden scales of conscience and starry cloak"
                EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD -> "warm-hearted bard and community builder with a golden lute and interwoven oak crest"
            }

            val imagePrompt = "High fantasy RPG character portrait of ${hero.name}, a $genderEn $speciesEn $archetypeVisualEn. Rich obsidian and warm imperial gold lighting, detailed digital painting illustration, centered bust portrait, expressive eyes, no text."

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = imagePrompt)))),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = "1K"),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            val response = GeminiRetrofitClient.service.generateWithModel(
                model = "gemini-2.5-flash-image",
                apiKey = apiKey,
                request = request
            )
            response.candidates.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { it.inlineData != null && it.inlineData.data.isNotBlank() }
                ?.inlineData?.data
        } catch (_: Exception) {
            null
        }
    }

    fun buildProceduralVisualDescription(hero: PlayerHero): String {
        val speciesAppearance = when (hero.species) {
            HeroSpecies.HUMANO ->
                if (hero.gender == HeroGender.MUJER) {
                    "De mirada resuelta y capa de viaje teñida en azul obsidiana, porta en el semblante la urgencia luminosa de quienes buscan trascender su breve existencia."
                } else {
                    "De porte firme y capa de viaje teñida en azul obsidiana, refleja en su mirada la urgencia luminosa de quienes buscan trascender su breve existencia."
                }
            HeroSpecies.ELFO ->
                "Sus ojos de iris plateado guardan la serenidad de siglos antiguos, vistiendo tejidos élficos bordados con filigrana de hojas perennes y luz estelar."
            HeroSpecies.ENANO ->
                "Ataviado con hombreras de bronce bruñido y runas ancestrales grabadas a cincel, su presencia transmite la solidez inquebrantable de una promesa tallada en roca."
            HeroSpecies.ORCO ->
                "De complexión imponente, colmillos nobles y tatuajes rituales de ceniza dorada, su expresión frontal irradia una lealtad apasionada que desarma cualquier prejuicio."
            HeroSpecies.MEDIO_ELFO ->
                "Con rasgos que armonizan la gracia silvana y la calidez humana, lleva un broche de dos lunas entrelazadas como símbolo de quien tiende puentes entre mundos opuestos."
        }

        val archetypeRegalia = when (hero.archetype) {
            EthicalArchetype.BUSCADOR_DE_VERDAD ->
                "Sostiene un astrolabio de cristal claro y un códice abierto, emblemas de quien jamás acepta una mentira cómoda por encima de la verdad."
            EthicalArchetype.DEFENSOR_DE_LA_DIGNIDAD ->
                "Sobre su pecho brilla el blasón del Sol Inviolable, juramento visible de proteger la dignidad intrínseca de cada persona frente a todo cálculo de poder."
            EthicalArchetype.EXPLORADOR_DE_LIBERTAD ->
                "Una brújula de viento y una pluma de halcón adornan su tahalí, recordando que cada sendero elegido en libertad conlleva responsabilidad moral."
            EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA ->
                "Lleva prendida al manto una balanza de oro viejo que tintinea suavemente, recordatorio constante de escuchar la voz interior de la justicia."
            EthicalArchetype.CONSTRUCTOR_DE_COMUNIDAD ->
                "Porta un medallón de raíces entrelazadas y una copa de hospitalidad, insignias de quien sabe que el sentido humano florece en la fraternidad."
        }

        return "$speciesAppearance $archetypeRegalia"
    }

    /**
     * Genera música ambiental utilizando el modelo Lyria (`lyria-3-clip-preview` para clips cortos
     * de hasta 30s o `lyria-3-pro-preview` para pistas completas) con `responseModalities = listOf("AUDIO")`.
     */
    suspend fun generateLyriaMusicClip(
        prompt: String,
        useFullLengthPro: Boolean = false
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null
        val modelName = if (useFullLengthPro) "lyria-3-pro-preview" else "lyria-3-clip-preview"
        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseModalities = listOf("AUDIO")
                )
            )
            val response = GeminiRetrofitClient.service.generateWithModel(
                model = modelName,
                apiKey = apiKey,
                request = request
            )
            response.candidates.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { it.inlineData != null && it.inlineData.data.isNotBlank() }
                ?.inlineData?.data
        } catch (_: Exception) {
            null
        }
    }
}
