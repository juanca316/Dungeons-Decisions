package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NarrativeEventType
import com.example.audio.SoundEffect
import com.example.audio.SoundEffectManager
import com.example.data.DilemmaOption
import com.example.data.DmTurnPayload
import com.example.data.EthicalArchetype
import com.example.data.HeroGender
import com.example.data.HeroSpecies
import com.example.data.MoralScores
import com.example.data.PlayerHero
import com.example.data.VoterDilemmaComplexity
import com.example.ui.computeMoralStakes
import com.example.ui.parseProblemPresentation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dungeons & Decisions", appName)
  }

  @Test
  fun `sound effect manager handles key moments and volume`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sfxManager = SoundEffectManager(context)

    sfxManager.playProblemCardRevealed(2)
    assertEquals(SoundEffect.PROBLEM_CARD_REVEALED, sfxManager.lastPlayedSoundEffect.value)

    sfxManager.playDecisionButtonPress()
    assertEquals(SoundEffect.DECISION_BUTTON_PRESS, sfxManager.lastPlayedSoundEffect.value)

    sfxManager.playTutorialCompleted()
    assertEquals(SoundEffect.TUTORIAL_COMPLETED, sfxManager.lastPlayedSoundEffect.value)

    sfxManager.setVolume(0.5f)
    assertEquals(0.5f, sfxManager.sfxVolume.value, 0.001f)

    assertFalse(sfxManager.isMuted.value)
    assertTrue(sfxManager.toggleMute())
    assertTrue(sfxManager.isMuted.value)

    val eventType = sfxManager.playForTurnTransition(diceRoll = 19, isStoryConcluded = false)
    assertEquals(NarrativeEventType.EXITO, eventType)

    sfxManager.release()
  }

  @Test
  fun `problem presentation splits situation conflict question and stakes`() {
    val payload = DmTurnPayload(
      chapterTitle = "El Faro Congelado",
      narrativeText = "La ventisca azota el puerto del norte.\n\nLos guardianes dudan si abrir las compuertas a los refugiados.",
      ethicalQuestion = "¿Abres las puertas arriesgando las provisiones?",
      options = listOf(
        DilemmaOption(
          id = "o1",
          label = "Abrir compuertas",
          justiceDelta = 4,
          compassionDelta = 10,
          pragmatismDelta = -4,
          libertyDelta = 2
        ),
        DilemmaOption(
          id = "o2",
          label = "Mantener cierre",
          justiceDelta = -2,
          compassionDelta = -6,
          pragmatismDelta = 9,
          libertyDelta = -3
        )
      )
    )

    val parsed = parseProblemPresentation(payload)
    assertEquals("La ventisca azota el puerto del norte.", parsed.situationText)
    assertEquals("Los guardianes dudan si abrir las compuertas a los refugiados.", parsed.conflictText)
    assertEquals("¿Abres las puertas arriesgando las provisiones?", parsed.keyQuestionText)

    val stakes = computeMoralStakes(payload.options, MoralScores(), voterCount = 4)
    assertTrue(stakes.valuesAtStake.isNotEmpty())
  }

  @Test
  fun `hero stats and voter complexity scale as expected`() {
    assertEquals(VoterDilemmaComplexity.SOLITARIO_INTROSPECTIVO, VoterDilemmaComplexity.forVoterCount(1))
    assertEquals(VoterDilemmaComplexity.ASAMBLEA_FACCIONES, VoterDilemmaComplexity.forVoterCount(6))

    val hero = PlayerHero(
      index = 0,
      name = "Alden",
      gender = HeroGender.HOMBRE,
      species = HeroSpecies.HUMANO,
      archetype = EthicalArchetype.GUARDIAN_DE_LA_CONCIENCIA
    )
    val option = DilemmaOption(
      id = "opt",
      label = "Defender a los inocentes",
      justiceDelta = 8,
      compassionDelta = 4,
      pragmatismDelta = 2,
      libertyDelta = 2
    )
    val (updatedHero, deltas) = hero.applyActionAndDiceRoll(option, diceRoll = 18)
    assertTrue(deltas.honorDelta > 0)
    assertTrue(updatedHero.stats.honor > hero.stats.honor)
  }
}
