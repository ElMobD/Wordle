<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted, onUnmounted, watch } from 'vue'
import Header from '../components/Header.vue'
import Modal from '../components/Modal.vue'
import WordleGrid from '../components/WordleGrid.vue'
import WordleKeyboard from '../components/WordleKeyboard.vue'
import { useLobbySocket } from '../composables/useLobbySocket'

const router = useRouter()
const route = useRoute()
const sessionCode = route.params.sessionCode as string
const gameId = ref(route.params.gameId as string)
const isHelpModalOpen = ref(false)
const isResultModalOpen = ref(false)
const guesses = ref<any[]>([])
const currentGuess = ref('')
const gameStatus = ref<'IN_PROGRESS' | 'WON' | 'LOST'>('IN_PROGRESS')
const maxGuesses = ref(6)
const wordLength = ref(5)
const loading = ref(true)
const error = ref('')
const userIdCookie = ref<string | null>(null)

// Settings de la session
const rounds = ref(1)
const timeLimit = ref(60)
const currentRound = ref(1)
const timeRemaining = ref(60)
const isHost = ref(false)
const roundFinished = ref(false)
const hasNextRound = ref(true)
const roundFinishedReason = ref('')
const isAdvancingRound = ref(false)
let timerInterval: number | null = null
let timerEndAtMs: number | null = null

const { connect, send, isConnected, lastMessage} = useLobbySocket()

onMounted(async () => {
  userIdCookie.value = document.cookie.split('; ').find(row => row.startsWith('userId='))?.split('=')[1] || null
  connect()
  
  const checkConnection = setInterval(() => {
    if (isConnected.value) {
      clearInterval(checkConnection)
      send({ type: 'LOAD_GAME', sessionCode })
    }
  }, 100)
  
  window.addEventListener('keydown', handlePhysicalKeyPress)
})
watch(lastMessage, (msg) => {
  if (!msg) return
  console.table(msg)

  if (msg.type === 'load_game' || msg.type === 'game_start') {
    if (msg.type === 'game_start') {
      // Nouveau round: reset de l'UI de round
      guesses.value = []
      currentGuess.value = ''
      isResultModalOpen.value = false
      isAdvancingRound.value = false
      router.replace(`/lobby/${sessionCode}/${msg.gameId}`)
    }

    gameId.value = msg.gameId
    maxGuesses.value = msg.maxAttempts
    wordLength.value = msg.answerLength
    gameStatus.value = msg.status

    // Extraire les settings de la session
    if (msg.rounds !== undefined) rounds.value = msg.rounds
    if (msg.timeLimit !== undefined) timeLimit.value = msg.timeLimit
    if (msg.currentRound !== undefined) currentRound.value = msg.currentRound
    if (msg.wordLength !== undefined) wordLength.value = msg.wordLength
    if (msg.hostId !== undefined) {
      isHost.value = String(msg.hostId) === String(userIdCookie.value)
    }

    hasNextRound.value = currentRound.value < rounds.value
    roundFinished.value = false
    roundFinishedReason.value = ''

    // Initialiser le timer depuis le serveur pour garder le même chrono pour tous
    if (msg.remainingTime !== undefined) {
      timeRemaining.value = Math.max(0, Number(msg.remainingTime))
    } else {
      timeRemaining.value = timeLimit.value
    }
    startTimer()

    if (msg.guesses && Array.isArray(msg.guesses)) {
      guesses.value = msg.guesses.map((g: any) => ({
        word: g.guess,
        mask: g.resultMask
      }))
    } else if (msg.type === 'load_game') {
      guesses.value = []
    }

    loading.value = false
    if (msg.status === 'CANCELED') {
      router.push('/homepage')
    }
  } else if (msg.type === 'guess_submitted') {
    // Ajouter la nouvelle tentative à la liste
    guesses.value.push({
      word: msg.word,
      mask: msg.resultMask
    })

    // Mettre à jour l'état du jeu
    gameStatus.value = msg.status

    if (msg.status === 'WON' || msg.status === 'LOST') {
      isResultModalOpen.value = true
    }
  } else if (msg.type === 'round_finished') {
    if (msg.roundNumber === currentRound.value) {
      roundFinished.value = true
      hasNextRound.value = !!msg.hasNextRound
      roundFinishedReason.value = msg.reason || 'ALL_PLAYERS_FINISHED'
      stopTimer()
    }
  } else if (msg.type === 'session_finished') {
    roundFinished.value = true
    hasNextRound.value = false
    roundFinishedReason.value = 'SESSION_FINISHED'
    stopTimer()
  } else if (msg.type === 'error') {
    isAdvancingRound.value = false
    error.value = msg.message || 'Erreur inconnue'
  } else if (msg.type === 'player_left') {
    // Si le joueur courant quitte, retourner à l'homepage
    if (String(msg.userId) === String(userIdCookie.value)) {
      router.push('/homepage')
    }
    // Sinon, le joueur reste dans la partie (un autre joueur a quitté)
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', handlePhysicalKeyPress)
  stopTimer()
})

// Fonctions du timer
const startTimer = () => {
  stopTimer() // Arrêter le timer existant s'il y en a un
  if (timeRemaining.value <= 0) {
    roundFinished.value = true
    roundFinishedReason.value = 'TIMER'
    hasNextRound.value = currentRound.value < rounds.value
    return
  }

  timerEndAtMs = Date.now() + (timeRemaining.value * 1000)

  timerInterval = setInterval(() => {
    if (timerEndAtMs === null) {
      stopTimer()
      return
    }

    const remaining = Math.ceil((timerEndAtMs - Date.now()) / 1000)
    if (remaining > 0) {
      timeRemaining.value = remaining
    } else {
      timeRemaining.value = 0
      roundFinished.value = true
      roundFinishedReason.value = 'TIMER'
      hasNextRound.value = currentRound.value < rounds.value
      stopTimer()
    }
  }, 1000)
}

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
  timerEndAtMs = null
}

const quitLobby = () => {
  send({ type: 'LEAVE_LOBBY', sessionCode })
}
const submitWord = async (word: string) => {
  if (!gameId.value || gameStatus.value !== 'IN_PROGRESS' || roundFinished.value || timeRemaining.value <= 0) return

  send({
    type: 'SUBMIT_GUESS',
    gameId: gameId.value,
    guess: word,
    sessionCode
  })
}

const goNextRound = () => {
  if (!isHost.value || !roundFinished.value || !hasNextRound.value || isAdvancingRound.value) return
  isAdvancingRound.value = true
  send({
    type: 'NEXT_ROUND',
    sessionCode
  })
}

const goHome = () => {
  router.push('/homepage')
}
const goToContact = () => {
  router.push('/contact')
}
const goToSettings = () => {
  router.push('/settings')
}
const goToProfile = () => {
  router.push('/settings?tab=profil')
}

const handleKeyPress = (key: string) => {
  // Bloquer le clavier si le round est terminé (timer ou état de game)
  if (gameStatus.value !== 'IN_PROGRESS' || roundFinished.value || timeRemaining.value <= 0) return

  if (key === 'ENTER') {
    if (currentGuess.value.length === wordLength.value) {
      submitWord(currentGuess.value)
      currentGuess.value = ''
    }
  } else if (key === 'BACKSPACE') {
    currentGuess.value = currentGuess.value.slice(0, -1)
  } else if (currentGuess.value.length < wordLength.value) {
    currentGuess.value += key.toUpperCase()
  }
}

const handlePhysicalKeyPress = (event: KeyboardEvent) => {
  const activeElement = document.activeElement as HTMLElement
  if (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA') {
    return
  }
  if (isHelpModalOpen.value || isResultModalOpen.value) {
    return
  }
  const key = event.key.toUpperCase()
  if (event.key === 'Enter') {
    event.preventDefault()
    handleKeyPress('ENTER')
    return
  }
  if (event.key === 'Backspace') {
    event.preventDefault()
    handleKeyPress('BACKSPACE')
    return
  }
  if (key.length === 1 && /^[A-Z]$/.test(key)) {
    event.preventDefault()
    handleKeyPress(key)
  }
}
</script>

<template>
  <div class="relative flex flex-col min-h-screen w-full">
    <div class="fixed inset-0 bg-white/5 pointer-events-none z-0"></div>
    <Header
      title="Partie en cours"
      @home="goHome"
      @settings="goToSettings"
      @help="isHelpModalOpen = true"
      @contact="goToContact"
      @profile="goToProfile"
    />
    <main class="relative z-5 flex flex-1 min-h-0 w-full items-center justify-center p-4">
      <div class="flex flex-col flex-1 min-h-0 w-full max-w-xl h-full gap-4 justify-between items-center">
        <div v-if="error" class="relative z-10 bg-red-500/20 border border-red-500/60 text-red-300 p-4 text-center rounded-lg mb-4 w-full">{{ error }}</div>
        <div v-if="loading" class="relative z-5 flex items-center justify-center flex-1 text-white w-full">Chargement...</div>
        <template v-else>
          <!-- Affichage des settings et du timer -->
          <div class="w-full flex flex-col items-center gap-2 mb-4">
            <div class="flex gap-6 text-white text-lg font-semibold">
              <div class="bg-white/10 px-4 py-2 rounded-lg">
                Round: <span class="text-blue-400">{{ currentRound }}/{{ rounds }}</span>
              </div>
              <div class="bg-white/10 px-4 py-2 rounded-lg" :class="{ 'text-red-400': timeRemaining <= 10 }">
                ⏱️ {{ timeRemaining }}s
              </div>
              <div class="bg-white/10 px-4 py-2 rounded-lg">
                Lettres: <span class="text-green-400">{{ wordLength }}</span>
              </div>
            </div>
            <div v-if="roundFinished" class="text-center text-white/90 bg-white/10 px-4 py-2 rounded-lg">
              <div v-if="roundFinishedReason === 'TIMER'">Round terminé: temps écoulé.</div>
              <div v-else-if="roundFinishedReason === 'ALL_PLAYERS_FINISHED'">Round terminé: tous les joueurs ont fini.</div>
              <div v-else-if="roundFinishedReason === 'SESSION_FINISHED'">Partie terminée.</div>
              <div v-else>Round terminé.</div>
            </div>
            <button
              v-if="isHost && roundFinished && hasNextRound"
              @click="goNextRound"
              :disabled="isAdvancingRound"
              class="bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold rounded-full px-8 py-3 shadow-lg transition-all"
            >
              {{ isAdvancingRound ? 'Lancement...' : 'Prochain round' }}
            </button>
            <div v-else-if="!isHost && roundFinished && hasNextRound" class="text-white/80 text-sm">
              En attente de l'hôte pour lancer le prochain round...
            </div>
          </div>

          <div class="flex-1 min-h-0 w-full flex items-center justify-center">
            <div class="w-full flex justify-center">
              <div class="max-w-[320px] w-full sm:max-w-[360px]">
                <WordleGrid :guesses="guesses" :currentGuess="currentGuess" :maxGuesses="maxGuesses" :wordLength="wordLength" />
              </div>
            </div>
          </div>
          <div class="w-full flex justify-center">
            <div class="max-w-[340px] w-full sm:max-w-[380px]">
              <WordleKeyboard :guesses="guesses" :disabled="roundFinished || timeRemaining <= 0 || gameStatus !== 'IN_PROGRESS'" @keyPress="handleKeyPress" />
            </div>
          </div>
          <button @click="quitLobby" class="bg-red-600 hover:bg-red-700 text-white font-bold rounded-full px-8 py-3 shadow-lg transition-all">
            Quitter le lobby
          </button>
        </template>
      </div>
    </main>
    <Modal :isOpen="isHelpModalOpen" @close="isHelpModalOpen = false">
      <div class="flex flex-col gap-4">
        <h3 class="my-2 text-lg text-white font-semibold">Comment jouer ?</h3>
        <p class="my-2 opacity-95">Trouvez le mot du round en un minimum d'essais. Chaque essai doit être un mot valide.</p>
        <ul class="my-2 pl-6 opacity-95 list-disc">
          <li>Les lettres vertes sont bien placées.</li>
          <li>Les lettres jaunes sont dans le mot mais mal placées.</li>
          <li>Les lettres grises ne sont pas dans le mot.</li>
        </ul>
        <div class="flex flex-col gap-3 mt-4 p-4 bg-white/5 rounded-lg">
          <div class="p-3 rounded-md text-center font-medium bg-green-500/20 text-green-300">Vert : lettre bien placée</div>
          <div class="p-3 rounded-md text-center font-medium bg-yellow-600/20 text-yellow-300">Jaune : lettre mal placée</div>
          <div class="p-3 rounded-md text-center font-medium bg-gray-500/20 text-gray-300">Gris : lettre absente</div>
        </div>
      </div>
    </Modal>
    <Modal :isOpen="isResultModalOpen" @close="isResultModalOpen = false">
      <div class="flex flex-col items-center gap-6 py-4">
        <div class="text-6xl leading-none">{{ gameStatus === 'WON' ? '✨' : '💡' }}</div>
        <p class="text-lg text-center text-white/95 m-0">
          {{ gameStatus === 'WON' 
            ? `Bravo ! Vous avez trouvé le mot en ${guesses.length} essai${guesses.length > 1 ? 's' : ''} !` 
            : 'Dommage ! Prochain round ou fin de partie.' 
          }}
        </p>
        <div class="flex gap-8 px-8 py-4 bg-white/5 rounded-xl w-full justify-center">
          <div class="flex flex-col items-center gap-2">
            <span class="text-2xl font-bold text-white">{{ guesses.length }}</span>
            <span class="text-sm text-white/70 uppercase tracking-wide">Essais</span>
          </div>
          <div class="flex flex-col items-center gap-2">
            <span class="text-2xl font-bold text-white">{{ maxGuesses }}</span>
            <span class="text-sm text-white/70 uppercase tracking-wide">Maximum</span>
          </div>
        </div>
        <button @click="isResultModalOpen = false" class="bg-indigo-600/80 border border-indigo-600 text-white px-8 py-3 rounded-xl text-base font-semibold cursor-pointer transition-all mt-4 hover:bg-indigo-600 hover:scale-105 hover:shadow-lg">Continuer</button>
      </div>
    </Modal>
  </div>
</template>
