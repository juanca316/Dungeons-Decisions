package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import androidx.annotation.RawRes
import com.example.R
import com.example.ui.AdventurePhase
import com.example.ui.MainTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

enum class BgmTrack(
    @RawRes val resId: Int,
    val genreTagEs: String,
    val titleEs: String,
    val descriptionEs: String,
    val defaultLyriaPrompt: String
) {
    BGM_MENU(
        resId = R.raw.bgm_menu,
        genreTagEs = "🎻 Barroco Clásico",
        titleEs = "Preludio Barroco de Cámara (Menú)",
        descriptionEs = "Contrapunto de clavecín y violonchelo en Re menor para el recibimiento solemne.",
        defaultLyriaPrompt = "Baroque chamber music prelude with harpsichord and warm cello counterpoint in D minor"
    ),
    BGM_MEDIEVAL_TAVERN(
        resId = R.raw.bgm_medieval_tavern,
        genreTagEs = "🪕 Medieval Trovadoresco",
        titleEs = "Balada Medieval de la Forja (Creación de Héroes)",
        descriptionEs = "Laúd medieval, arpa celta y flauta dórica mientras se forja la identidad del grupo.",
        defaultLyriaPrompt = "Medieval tavern ballad with acoustic lute, celtic harp and gentle wooden flute in Dorian mode"
    ),
    BGM_ADVENTURE(
        resId = R.raw.bgm_adventure,
        genreTagEs = "✨ Fantasía Épica",
        titleEs = "Sinfonía de Fantasía Mística (Exploración)",
        descriptionEs = "Cuerdas etéreas y arpa celestial durante la exploración procedimental y tiradas exitosas.",
        defaultLyriaPrompt = "Mystical high fantasy orchestral exploration theme with celestial harp and warm strings"
    ),
    BGM_SUSPENSE_DILEMMA(
        resId = R.raw.bgm_suspense_dilemma,
        genreTagEs = "🕯️ Suspenso Filosófico",
        titleEs = "Atmósfera de Suspenso y Deliberación",
        descriptionEs = "Cuerdas en trémolo y misterio armónico cuando el grupo debate una decisión ética con costo.",
        defaultLyriaPrompt = "Dark atmospheric suspense soundtrack with tremolo strings and mysterious philosophical tension"
    ),
    BGM_TENSION(
        resId = R.raw.bgm_tension,
        genreTagEs = "⚡ Tensión Dramática",
        titleEs = "Crisis e Infortunio Ético (Tensión Alta)",
        descriptionEs = "Pulso dramático urgente cuando ocurre un fallo en el D20 (1-5) o un conflicto moral extremo.",
        defaultLyriaPrompt = "Dramatic dark fantasy tension music with urgent minor ostinato strings and deep timpani"
    ),
    BGM_BAROQUE_CODEX(
        resId = R.raw.bgm_baroque_codex,
        genreTagEs = "📜 Fuga Barroca Académica",
        titleEs = "Fuga de la Biblioteca Real (Códice y Crónicas)",
        descriptionEs = "Polifonía barroca serena para la lectura filosófica del Códice CORE I y los Archivos.",
        defaultLyriaPrompt = "Contemplative baroque fugue on harpsichord and chamber strings for an ancient library"
    ),
    BGM_EPILOGUE(
        resId = R.raw.bgm_epilogue,
        genreTagEs = "👑 Fantasía Heroica",
        titleEs = "Fanfarria Sinfónica del Epílogo",
        descriptionEs = "Cierre triunfal e inspirador al concluir orgánicamente la historia del grupo.",
        defaultLyriaPrompt = "Inspiring heroic fantasy orchestral epilogue with French horns, lush strings and choir"
    );

    companion object {
        /**
         * Selecciona dinámicamente la pista musical según la pestaña, la fase de la aventura,
         * el estado del dilema y el resultado del dado D20 para ofrecer variaciones Barroca,
         * Medieval, Fantasía, Suspenso y Tensión Dramática.
         */
        fun resolveDynamicTrack(
            selectedTab: MainTab,
            phase: AdventurePhase,
            turnNumber: Int,
            lastDiceRoll: Int?,
            isStoryConcluded: Boolean
        ): BgmTrack {
            if (selectedTab == MainTab.CODICE || selectedTab == MainTab.CRONICAS) {
                return BGM_BAROQUE_CODEX
            }

            return when (phase) {
                AdventurePhase.WELCOME_PLAYER_COUNT -> BGM_MENU
                AdventurePhase.PARTY_FORGE -> BGM_MEDIEVAL_TAVERN
                AdventurePhase.CAMPAIGN_EPILOGUE -> BGM_EPILOGUE
                AdventurePhase.ACTIVE_TABLETOP -> {
                    when {
                        isStoryConcluded -> BGM_EPILOGUE
                        lastDiceRoll != null && lastDiceRoll in 1..5 -> BGM_TENSION
                        lastDiceRoll != null && lastDiceRoll in 6..14 -> BGM_SUSPENSE_DILEMMA
                        turnNumber % 2 == 0 -> BGM_SUSPENSE_DILEMMA
                        else -> BGM_ADVENTURE
                    }
                }
            }
        }

        fun forPhase(phase: AdventurePhase): BgmTrack = when (phase) {
            AdventurePhase.WELCOME_PLAYER_COUNT -> BGM_MENU
            AdventurePhase.PARTY_FORGE -> BGM_MEDIEVAL_TAVERN
            AdventurePhase.ACTIVE_TABLETOP -> BGM_ADVENTURE
            AdventurePhase.CAMPAIGN_EPILOGUE -> BGM_EPILOGUE
        }
    }
}

class AudioManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared: Boolean = false
    private var shouldPlayWhenPrepared: Boolean = true

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _currentTrack = MutableStateFlow<BgmTrack?>(null)
    val currentTrack: StateFlow<BgmTrack?> = _currentTrack.asStateFlow()

    private val customTrackFiles = mutableMapOf<BgmTrack, File>()

    companion object {
        private const val DEFAULT_VOLUME = 0.3f
    }

    /**
     * Reproduce la pista indicada en bucle con un volumen inicial moderado del 30% (0.3f).
     * Controla estrictamente el estado [isPrepared] para evitar llamadas a start() en estado PREPARING (state 4, error -38).
     */
    @Synchronized
    fun playTrack(track: BgmTrack) {
        if (_currentTrack.value == track && mediaPlayer != null) {
            shouldPlayWhenPrepared = true
            if (isPrepared) {
                applyVolume()
                try {
                    if (mediaPlayer?.isPlaying == false) {
                        mediaPlayer?.start()
                    }
                } catch (_: Exception) {
                }
            }
            return
        }

        stopAndReleasePlayerOnly()
        _currentTrack.value = track
        isPrepared = false
        shouldPlayWhenPrepared = true

        try {
            val customFile = customTrackFiles[track]
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val player = MediaPlayer()
            player.setAudioAttributes(audioAttributes)

            player.setOnPreparedListener { mp ->
                synchronized(this@AudioManager) {
                    if (mediaPlayer !== mp) return@synchronized
                    isPrepared = true
                    try {
                        mp.isLooping = true
                        val activeVol = if (_isMuted.value) 0f else DEFAULT_VOLUME
                        mp.setVolume(activeVol, activeVol)
                        if (shouldPlayWhenPrepared) {
                            mp.start()
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            player.setOnErrorListener { mp, _, _ ->
                synchronized(this@AudioManager) {
                    if (mediaPlayer === mp) {
                        isPrepared = false
                    }
                }
                true
            }

            if (customFile != null && customFile.exists()) {
                player.setDataSource(customFile.absolutePath)
                mediaPlayer = player
                player.prepareAsync()
            } else {
                val afd = appContext.resources.openRawResourceFd(track.resId)
                if (afd != null) {
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    mediaPlayer = player
                    player.prepareAsync()
                } else {
                    player.release()
                }
            }
        } catch (_: Exception) {
            isPrepared = false
        }
    }

    /**
     * Reproduce automáticamente la pista asociada a la fase actual de la aventura.
     */
    fun playForPhase(phase: AdventurePhase) {
        playTrack(BgmTrack.forPhase(phase))
    }

    /**
     * Alterna entre silencio (0f) y el volumen ambiental del 30% (0.3f).
     */
    @Synchronized
    fun toggleMute(): Boolean {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        applyVolume()
        return newMuted
    }

    /**
     * Permite registrar una pista generada dinámicamente con Lyria (Base64 audio)
     * para reemplazar una pista ambiental y reproducirla inmediatamente.
     */
    fun updateTrackWithGeneratedAudio(track: BgmTrack, base64AudioData: String): Boolean {
        return try {
            val bytes = Base64.decode(base64AudioData, Base64.DEFAULT)
            if (bytes.isEmpty()) return false
            val outFile = File(appContext.cacheDir, "lyria_${track.name.lowercase()}.wav")
            FileOutputStream(outFile).use { it.write(bytes) }
            customTrackFiles[track] = outFile
            _currentTrack.value = null
            playTrack(track)
            true
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun pause() {
        shouldPlayWhenPrepared = false
        if (!isPrepared) return
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun resume() {
        shouldPlayWhenPrepared = true
        if (!isPrepared) {
            if (mediaPlayer == null && _currentTrack.value != null) {
                val track = _currentTrack.value
                _currentTrack.value = null
                track?.let { playTrack(it) }
            }
            return
        }
        try {
            if (mediaPlayer?.isPlaying == false) {
                applyVolume()
                mediaPlayer?.start()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Libera todos los recursos del MediaPlayer para evitar fugas de memoria
     * o que el audio continúe sonando al minimizar o cerrar la aplicación.
     */
    @Synchronized
    fun release() {
        stopAndReleasePlayerOnly()
        _currentTrack.value = null
    }

    private fun applyVolume() {
        if (!isPrepared) return
        try {
            val vol = if (_isMuted.value) 0f else DEFAULT_VOLUME
            mediaPlayer?.setVolume(vol, vol)
        } catch (_: Exception) {
        }
    }

    private fun stopAndReleasePlayerOnly() {
        val player = mediaPlayer
        val wasPrepared = isPrepared
        mediaPlayer = null
        isPrepared = false
        shouldPlayWhenPrepared = false

        if (player != null) {
            try {
                player.setOnPreparedListener(null)
                player.setOnErrorListener(null)
                if (wasPrepared && player.isPlaying) {
                    player.stop()
                }
            } catch (_: Exception) {
            }
            try {
                player.release()
            } catch (_: Exception) {
            }
        }
    }
}
