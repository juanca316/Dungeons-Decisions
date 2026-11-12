package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager as SystemAudioManager
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tipos de eventos narrativos específicos durante la transición de turnos y evaluación de acciones.
 */
enum class NarrativeEventType(
    val labelEs: String,
    val badgeEmoji: String,
    val descriptionEs: String
) {
    DILEMA(
        labelEs = "Nuevo Dilema Ético",
        badgeEmoji = "⚖️",
        descriptionEs = "Se presenta una nueva encrucijada filosófica o éxito parcial con costo ético (D20: 6-14)."
    ),
    EXITO(
        labelEs = "Éxito Pleno o Revelación",
        badgeEmoji = "🌟",
        descriptionEs = "La decisión del héroe alcanza un desenlace ejemplar o revelación moral (D20: 15-20)."
    ),
    FALLO_CRITICO(
        labelEs = "Fallo Crítico / Infortunio",
        badgeEmoji = "⚡",
        descriptionEs = "El destino o un fallo crítico complica gravemente la escena (D20: 1-5)."
    )
}

enum class SoundEffect(
    @RawRes val resId: Int,
    val emoji: String,
    val titleEs: String,
    val descriptionEs: String
) {
    PROBLEM_CARD_REVEALED(
        resId = R.raw.sfx_narrative_dilemma,
        emoji = "🃏",
        titleEs = "Tarjeta Revelada",
        descriptionEs = "Deslizamiento místico de pergamino y arpa celta al revelar una tarjeta de problema."
    ),
    DECISION_BUTTON_PRESS(
        resId = R.raw.sfx_dilemma_resolved,
        emoji = "⚔️",
        titleEs = "Botón de Decisión",
        descriptionEs = "Sello arcano y acorde de laúd al presionar un botón de decisión ética."
    ),
    EVENT_LOG_UPDATED(
        resId = R.raw.sfx_narrative_dilemma,
        emoji = "📜",
        titleEs = "Log de Eventos Actualizado",
        descriptionEs = "Trazo de pluma sobre pergamino y campana de cristal al registrar una consecuencia en el log."
    ),
    DICE_ROLL(
        resId = R.raw.sfx_dice_roll,
        emoji = "🎲",
        titleEs = "Tirada de Dados (D20)",
        descriptionEs = "Sonido de rodamiento de dado de 20 caras sobre la mesa."
    ),
    DILEMMA_RESOLVED(
        resId = R.raw.sfx_dilemma_resolved,
        emoji = "✨",
        titleEs = "Decisión Confirmada",
        descriptionEs = "Acorde armónico al sellar una opción ética."
    ),
    TUTORIAL_COMPLETED(
        resId = R.raw.sfx_narrative_success,
        emoji = "🎓",
        titleEs = "Tutorial Completado",
        descriptionEs = "Fanfarria dorada de laúd y campanas al completar el tutorial interactivo."
    ),
    DECISION_ERROR(
        resId = R.raw.sfx_decision_error,
        emoji = "⚠️",
        titleEs = "Error de Decisión",
        descriptionEs = "Alerta sonora cuando falta registrar la tirada del D20 antes de decidir."
    ),
    NARRATIVE_DILEMMA(
        resId = R.raw.sfx_narrative_dilemma,
        emoji = "⚖️",
        titleEs = "Evento Narrativo: Dilema",
        descriptionEs = "Motivo enigmático de arpa y campana al revelarse un dilema ético en el cambio de turno."
    ),
    NARRATIVE_SUCCESS(
        resId = R.raw.sfx_narrative_success,
        emoji = "🌟",
        titleEs = "Evento Narrativo: Éxito",
        descriptionEs = "Fanfarria luminosa al transicionar un turno con éxito pleno o revelación (D20 15-20)."
    ),
    NARRATIVE_CRITICAL_FAILURE(
        resId = R.raw.sfx_critical_failure,
        emoji = "⚡",
        titleEs = "Evento Narrativo: Fallo Crítico",
        descriptionEs = "Acorde sombrío descendente ante un infortunio o fallo crítico en el turno (D20 1-5)."
    )
}

/**
 * Gestor de efectos sonoros (SFX) de fantasía basado en el framework de audio de Android
 * ([SoundPool], [AudioAttributes] con [AudioAttributes.USAGE_GAME] / [AudioAttributes.CONTENT_TYPE_SONIFICATION]
 * y [android.media.AudioManager]) con precarga asíncrona y síntesis PCM WAV de 16 bits para momentos clave:
 * - Revelación de tarjetas de problema ([SoundEffect.PROBLEM_CARD_REVEALED])
 * - Toma de decisiones y confirmación ([SoundEffect.DECISION_BUTTON_PRESS], [SoundEffect.DILEMMA_RESOLVED])
 * - Finalización del tutorial ([SoundEffect.TUTORIAL_COMPLETED])
 * - Tiradas de dados D20, actualización del log de eventos y eventos narrativos.
 */
class SoundEffectManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val systemAudioManager: SystemAudioManager? =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? SystemAudioManager
    private val preloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var soundPool: SoundPool? = null
    private val soundIdMap = mutableMapOf<SoundEffect, Int>()
    private val loadedSoundIds = mutableSetOf<Int>()
    private val pendingPlayQueue = mutableMapOf<Int, Float>()

    /**
     * Mapa explícito de tipos de eventos narrativos ([NarrativeEventType]) a sus clips de audio ([SoundEffect]).
     */
    private val narrativeEventSoundMap: Map<NarrativeEventType, SoundEffect> = mapOf(
        NarrativeEventType.DILEMA to SoundEffect.NARRATIVE_DILEMMA,
        NarrativeEventType.EXITO to SoundEffect.NARRATIVE_SUCCESS,
        NarrativeEventType.FALLO_CRITICO to SoundEffect.NARRATIVE_CRITICAL_FAILURE
    )

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _sfxVolume = MutableStateFlow(DEFAULT_SFX_VOLUME)
    val sfxVolume: StateFlow<Float> = _sfxVolume.asStateFlow()

    private val _isPreloaded = MutableStateFlow(false)
    val isPreloaded: StateFlow<Boolean> = _isPreloaded.asStateFlow()

    private val _lastNarrativeEvent = MutableStateFlow<NarrativeEventType?>(null)
    val lastNarrativeEvent: StateFlow<NarrativeEventType?> = _lastNarrativeEvent.asStateFlow()

    private val _lastPlayedSoundEffect = MutableStateFlow<SoundEffect?>(null)
    val lastPlayedSoundEffect: StateFlow<SoundEffect?> = _lastPlayedSoundEffect.asStateFlow()

    init {
        preloadAll()
    }

    companion object {
        private const val MAX_STREAMS = 8
        private const val DEFAULT_SFX_VOLUME = 0.88f
        private const val SAMPLE_RATE = 22050

        /**
         * Clasifica una tirada del D20 o un inicio de capítulo en el [NarrativeEventType] correspondiente:
         * - 1..5 -> [NarrativeEventType.FALLO_CRITICO]
         * - 15..20 -> [NarrativeEventType.EXITO]
         * - 6..14 o sin tirada previa -> [NarrativeEventType.DILEMA]
         */
        fun resolveNarrativeEventForRoll(
            diceRoll: Int?,
            isStoryConcluded: Boolean = false
        ): NarrativeEventType = when {
            isStoryConcluded -> NarrativeEventType.EXITO
            diceRoll == null -> NarrativeEventType.DILEMA
            diceRoll in 1..5 -> NarrativeEventType.FALLO_CRITICO
            diceRoll in 15..20 -> NarrativeEventType.EXITO
            else -> NarrativeEventType.DILEMA
        }
    }

    /**
     * Precarga de forma asíncrona todos los efectos sonoros en [SoundPool] para garantizar
     * reproducción de baja latencia desde la primera interacción del usuario.
     */
    fun preloadAll() {
        preloadScope.launch {
            try {
                val pool = ensureSoundPool() ?: return@launch
                SoundEffect.entries.forEach { effect ->
                    loadEffectIntoPool(pool, effect)
                }
                _isPreloaded.value = true
            } catch (_: Exception) {
            }
        }
    }

    @Synchronized
    private fun loadEffectIntoPool(pool: SoundPool, effect: SoundEffect): Int {
        soundIdMap[effect]?.let { return it }
        val customWav = getOrSynthesizeCustomWav(effect)
        val sampleId = if (customWav != null && customWav.exists()) {
            pool.load(customWav.absolutePath, 1)
        } else {
            pool.load(appContext, effect.resId, 1)
        }
        if (sampleId != 0) {
            soundIdMap[effect] = sampleId
        }
        return sampleId
    }

    @Synchronized
    private fun ensureSoundPool(): SoundPool? {
        if (soundPool != null) return soundPool
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { sp, sampleId, status ->
                val playbackRate = pendingPlayQueue.remove(sampleId)
                if (status == 0) {
                    loadedSoundIds.add(sampleId)
                    if (playbackRate != null && canAudiblyPlay()) {
                        try {
                            val vol = _sfxVolume.value
                            sp.play(
                                sampleId,
                                vol,
                                vol,
                                1,
                                0,
                                playbackRate.coerceIn(0.5f, 2.0f)
                            )
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            soundPool = pool
            pool
        } catch (_: Exception) {
            null
        }
    }

    private fun canAudiblyPlay(): Boolean {
        if (_isMuted.value || _sfxVolume.value <= 0.01f) return false
        val streamVol = systemAudioManager?.getStreamVolume(SystemAudioManager.STREAM_MUSIC) ?: 1
        return streamVol >= 0
    }

    /**
     * Genera archivos WAV de fantasía medieval personalizados para los efectos clave de interfaz
     * (revelación de tarjetas, decisión, log de eventos y finalización del tutorial) sin requerir
     * recursos externos adicionales.
     */
    private fun getOrSynthesizeCustomWav(effect: SoundEffect): java.io.File? {
        return try {
            val fileName = when (effect) {
                SoundEffect.PROBLEM_CARD_REVEALED -> "fantasy_sfx_card_reveal.wav"
                SoundEffect.DECISION_BUTTON_PRESS -> "fantasy_sfx_decision_press.wav"
                SoundEffect.EVENT_LOG_UPDATED -> "fantasy_sfx_event_log_update.wav"
                SoundEffect.TUTORIAL_COMPLETED -> "fantasy_sfx_tutorial_complete.wav"
                else -> return null
            }
            val outFile = java.io.File(appContext.cacheDir, fileName)
            if (outFile.exists() && outFile.length() > 100L) {
                return outFile
            }
            val pcmSamples = when (effect) {
                SoundEffect.PROBLEM_CARD_REVEALED -> synthesizeProblemCardRevealSamples()
                SoundEffect.DECISION_BUTTON_PRESS -> synthesizeDecisionButtonSamples()
                SoundEffect.EVENT_LOG_UPDATED -> synthesizeEventLogUpdateSamples()
                SoundEffect.TUTORIAL_COMPLETED -> synthesizeTutorialCompletedSamples()
                else -> return null
            }
            writeWavFile(outFile, pcmSamples)
            outFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Síntesis de "Revelación de Tarjeta de Problema":
     * Deslizamiento suave de pergamino seguido de arpegio ascendente místico de arpa celta (G4 -> B4 -> D5 -> G5).
     */
    private fun synthesizeProblemCardRevealSamples(): ShortArray {
        val durationSec = 0.36
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(totalSamples)
        val harpNotes = doubleArrayOf(392.00, 493.88, 587.33, 783.99) // G4, B4, D5, G5

        var noiseSeed = 0x3F9B
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            // Deslizamiento breve de pergamino al voltear la tarjeta (primeros 70ms)
            noiseSeed = (noiseSeed * 1103515245 + 12345) and 0x7fffffff
            val whiteNoise = (noiseSeed.toDouble() / 0x7fffffff.toDouble()) * 2.0 - 1.0
            val slideEnv = if (t < 0.07) kotlin.math.sin((t / 0.07) * Math.PI) * 0.16 else 0.0
            val parchmentWhoosh = whiteNoise * slideEnv

            // Destello de arpa mística
            var chime = 0.0
            for (idx in harpNotes.indices) {
                val start = 0.02 + idx * 0.036
                if (t >= start) {
                    val localT = t - start
                    val attack = (localT / 0.007).coerceAtMost(1.0)
                    val decay = kotlin.math.exp(-localT * 8.5)
                    val f = harpNotes[idx]
                    val tone = kotlin.math.sin(2.0 * Math.PI * f * localT) +
                        0.30 * kotlin.math.sin(2.0 * Math.PI * (f * 2.0) * localT)
                    chime += tone * attack * decay * 0.21
                }
            }

            val combined = (parchmentWhoosh + chime).coerceIn(-0.95, 0.95)
            samples[i] = (combined * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Síntesis de "Sello de Decisión Arcana":
     * Toque percusivo cálido de sello sobre madera + acorde ascendente de laúd/arpa celta (Re mayor / Dórica).
     */
    private fun synthesizeDecisionButtonSamples(): ShortArray {
        val durationSec = 0.34
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(totalSamples)
        val chordFreqs = doubleArrayOf(293.66, 440.00, 587.33, 880.00) // D4, A4, D5, A5

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sealClick = if (t < 0.035) {
                val env = (1.0 - t / 0.035)
                kotlin.math.sin(2.0 * Math.PI * 135.0 * t) * env * 0.45
            } else 0.0

            var harpSignal = 0.0
            for (idx in chordFreqs.indices) {
                val noteStart = idx * 0.028
                if (t >= noteStart) {
                    val localT = t - noteStart
                    val attack = (localT / 0.008).coerceAtMost(1.0)
                    val decay = kotlin.math.exp(-localT * 9.5)
                    val f = chordFreqs[idx]
                    val fundamental = kotlin.math.sin(2.0 * Math.PI * f * localT)
                    val overtone = 0.35 * kotlin.math.sin(2.0 * Math.PI * (f * 2.0) * localT)
                    harpSignal += (fundamental + overtone) * attack * decay * 0.22
                }
            }

            val combined = (sealClick + harpSignal).coerceIn(-0.95, 0.95)
            samples[i] = (combined * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Síntesis de "Inscripción en el Log de Eventos":
     * Textura breve de pluma sobre pergamino seguida de doble campana cristalina de crónica (Do5 -> Mi5 -> Sol5 -> Do6).
     */
    private fun synthesizeEventLogUpdateSamples(): ShortArray {
        val durationSec = 0.42
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(totalSamples)
        val chimeNotes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6

        var pseudoNoiseSeed = 0x5A3C
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            pseudoNoiseSeed = (pseudoNoiseSeed * 1103515245 + 12345) and 0x7fffffff
            val normalizedNoise = (pseudoNoiseSeed.toDouble() / 0x7fffffff.toDouble()) * 2.0 - 1.0
            val quillEnv = if (t < 0.065) {
                kotlin.math.sin((t / 0.065) * Math.PI) * 0.14
            } else 0.0
            val quillScribe = normalizedNoise * quillEnv

            var chimeSignal = 0.0
            for (idx in chimeNotes.indices) {
                val start = 0.03 + idx * 0.045
                if (t >= start) {
                    val localT = t - start
                    val attack = (localT / 0.006).coerceAtMost(1.0)
                    val decay = kotlin.math.exp(-localT * 7.8)
                    val freq = chimeNotes[idx]
                    val bell = kotlin.math.sin(2.0 * Math.PI * freq * localT) +
                        0.25 * kotlin.math.sin(2.0 * Math.PI * (freq * 2.5) * localT)
                    chimeSignal += bell * attack * decay * 0.20
                }
            }

            val combined = (quillScribe + chimeSignal).coerceIn(-0.95, 0.95)
            samples[i] = (combined * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Síntesis de "Tutorial Completado":
     * Cadencia triunfal luminosa de laúd y campana dorada (D4 -> F#4 -> A4 -> D5 -> F#5) al graduarse del tutorial.
     */
    private fun synthesizeTutorialCompletedSamples(): ShortArray {
        val durationSec = 0.72
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(totalSamples)
        val fanfareNotes = doubleArrayOf(293.66, 369.99, 440.00, 587.33, 739.99) // D4, F#4, A4, D5, F#5
        val noteStarts = doubleArrayOf(0.0, 0.08, 0.16, 0.25, 0.35)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var signal = 0.0

            for (idx in fanfareNotes.indices) {
                val start = noteStarts[idx]
                if (t >= start) {
                    val localT = t - start
                    val isFinalHold = idx >= 3
                    val attack = (localT / 0.01).coerceAtMost(1.0)
                    val decay = kotlin.math.exp(-localT * (if (isFinalHold) 4.2 else 7.5))
                    val f = fanfareNotes[idx]
                    val fundamental = kotlin.math.sin(2.0 * Math.PI * f * localT)
                    val fifthHarmonic = 0.28 * kotlin.math.sin(2.0 * Math.PI * (f * 2.0) * localT)
                    val shimmer = 0.12 * kotlin.math.sin(2.0 * Math.PI * (f * 3.0) * localT)
                    signal += (fundamental + fifthHarmonic + shimmer) * attack * decay * 0.22
                }
            }

            val clamped = signal.coerceIn(-0.95, 0.95)
            samples[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun writeWavFile(outFile: java.io.File, samples: ShortArray) {
        val byteDataSize = samples.size * 2
        val totalDataLen = byteDataSize + 36
        val byteRate = SAMPLE_RATE * 2
        val header = java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(totalDataLen)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16) // Subchunk1Size (PCM)
        header.putShort(1) // AudioFormat = 1 (PCM)
        header.putShort(1) // NumChannels = 1 (Mono)
        header.putInt(SAMPLE_RATE)
        header.putInt(byteRate)
        header.putShort(2) // BlockAlign
        header.putShort(16) // BitsPerSample
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(byteDataSize)

        java.io.FileOutputStream(outFile).use { fos ->
            fos.write(header.array())
            val pcmBuffer = java.nio.ByteBuffer.allocate(byteDataSize).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            for (s in samples) {
                pcmBuffer.putShort(s)
            }
            fos.write(pcmBuffer.array())
        }
    }

    /**
     * Dispara el clip de audio mapeado a un [NarrativeEventType] (Dilema, Éxito, Fallo Crítico)
     * y actualiza el estado del último evento narrativo para retroalimentación en la UI.
     */
    fun playNarrativeEvent(eventType: NarrativeEventType) {
        _lastNarrativeEvent.value = eventType
        val mappedEffect = narrativeEventSoundMap[eventType] ?: SoundEffect.NARRATIVE_DILEMMA
        playSound(mappedEffect)
    }

    /**
     * Evalúa automáticamente el resultado del D20 y el estado del turno para reproducir
     * el clip de evento narrativo correspondiente (Dilema, Éxito o Fallo Crítico).
     */
    fun playForTurnTransition(diceRoll: Int?, isStoryConcluded: Boolean = false): NarrativeEventType {
        val eventType = resolveNarrativeEventForRoll(diceRoll, isStoryConcluded)
        playNarrativeEvent(eventType)
        return eventType
    }

    /**
     * Dispara un efecto de sonido corto utilizando [SoundPool], cargando el archivo sintetizado
     * de fantasía o el recurso raw bajo demanda si aún no había finalizado la precarga.
     */
    fun playSound(effect: SoundEffect, rate: Float = 1.0f) {
        _lastPlayedSoundEffect.value = effect
        if (!canAudiblyPlay()) return
        try {
            val pool = ensureSoundPool() ?: return
            val existingId = soundIdMap[effect]
            val clampedRate = rate.coerceIn(0.5f, 2.0f)
            val vol = _sfxVolume.value
            if (existingId != null && loadedSoundIds.contains(existingId)) {
                pool.play(
                    existingId,
                    vol,
                    vol,
                    1,
                    0,
                    clampedRate
                )
            } else if (existingId == null) {
                val newSampleId = loadEffectIntoPool(pool, effect)
                if (newSampleId != 0) {
                    pendingPlayQueue[newSampleId] = clampedRate
                }
            } else {
                pendingPlayQueue[existingId] = clampedRate
            }
        } catch (_: Exception) {
        }
    }

    fun getSoundEffectForNarrativeEvent(eventType: NarrativeEventType): SoundEffect =
        narrativeEventSoundMap[eventType] ?: SoundEffect.NARRATIVE_DILEMMA

    fun playProblemCardRevealed(stepNumber: Int = 2) {
        val rate = when (stepNumber) {
            1 -> 0.96f
            2 -> 1.04f
            else -> 1.14f
        }
        playSound(SoundEffect.PROBLEM_CARD_REVEALED, rate = rate)
    }

    fun playTutorialCompleted() = playSound(SoundEffect.TUTORIAL_COMPLETED)

    fun playDecisionButtonPress() = playSound(SoundEffect.DECISION_BUTTON_PRESS)

    fun playEventLogUpdated() = playSound(SoundEffect.EVENT_LOG_UPDATED)

    fun playDiceRoll() = playSound(SoundEffect.DICE_ROLL)

    fun playDilemmaResolved() = playSound(SoundEffect.DILEMMA_RESOLVED)

    fun playDecisionError() = playSound(SoundEffect.DECISION_ERROR)

    fun setVolume(volume: Float) {
        _sfxVolume.value = volume.coerceIn(0f, 1f)
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
    }

    fun toggleMute(): Boolean {
        val next = !_isMuted.value
        _isMuted.value = next
        return next
    }

    /**
     * Libera los recursos nativos del [SoundPool] y cancela las tareas de precarga.
     */
    @Synchronized
    fun release() {
        try {
            preloadScope.cancel()
            soundPool?.release()
        } catch (_: Exception) {
        } finally {
            soundPool = null
            soundIdMap.clear()
            loadedSoundIds.clear()
            pendingPlayQueue.clear()
        }
    }
}
