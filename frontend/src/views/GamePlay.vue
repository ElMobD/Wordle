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
const correctAnswer = ref('')
const isAdvancingRound = ref(false)
let timerInterval: number | null = null
let timerEndAtMs: number | null = null

type PlayerStatus = 'IN_PROGRESS' | 'WON' | 'LOST'
interface PlayerState {
  id: number
  name: string
  picture?: string
  status: PlayerStatus
}
const playersState = ref<PlayerState[]>([])

const statusLabel: Record<PlayerStatus, string> = {
  IN_PROGRESS: 'En cours',
  WON: 'Won',
  LOST: 'Lost'
}

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
    const isNewGameStart = msg.type === 'game_start' && String(msg.gameId) !== String(gameId.value)
    if (isNewGameStart) {
      // Nouveau round (nouvelle game): reset de l'UI de round
      guesses.value = []
      currentGuess.value = ''
      correctAnswer.value = ''
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

    if (Array.isArray(msg.players_status)) {
      playersState.value = msg.players_status.map((p: any) => ({
        id: Number(p.id),
        name: p.name,
        picture: p.picture,
        status: (p.status as PlayerStatus) || 'IN_PROGRESS'
      }))
    }

    hasNextRound.value = currentRound.value < rounds.value
    
    // Initialiser le timer depuis le serveur pour garder le même chrono pour tous
    if (msg.remainingTime !== undefined) {
      timeRemaining.value = Math.max(0, Number(msg.remainingTime))
    } else {
      timeRemaining.value = timeLimit.value
    }

    // Vérifier si le round est déjà terminé selon le serveur
    if (msg.roundFinished === true) {
      roundFinished.value = true
      roundFinishedReason.value = msg.roundFinishedReason || 'TIMER'
      correctAnswer.value = msg.answer || ''
      stopTimer()
    } else {
      // Le round n'est pas terminé : réinitialiser et démarrer le timer
      roundFinished.value = false
      roundFinishedReason.value = ''
      correctAnswer.value = ''
      
      // Démarrer/continuer le timer tant que le round n'est pas terminé
      // même si le joueur a déjà fini individuellement (WON/LOST)
      startTimer()
    }

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

    // Ne pas arrêter le timer ici - il continue jusqu'à la fin du round
  } else if (msg.type === 'round_finished') {
    if (msg.roundNumber === currentRound.value) {
      roundFinished.value = true
      hasNextRound.value = !!msg.hasNextRound
      roundFinishedReason.value = msg.reason || 'ALL_PLAYERS_FINISHED'
      correctAnswer.value = msg.answer || ''
      stopTimer()
    }
  } else if (msg.type === 'session_finished') {
    roundFinished.value = true
    hasNextRound.value = false
    roundFinishedReason.value = 'SESSION_FINISHED'
    stopTimer()
  } else if (msg.type === 'game_status_updated') {
    // Un autre joueur a terminé sa game (WON ou LOST)
    const player = playersState.value.find(p => String(p.id) === String(msg.userId))
    if (player && (msg.status === 'WON' || msg.status === 'LOST' || msg.status === 'IN_PROGRESS')) {
      player.status = msg.status
    }

    // Mettre à jour l'interface si nécessaire, ou afficher une notification
    console.log(`Joueur ${msg.userName} a ${msg.status === 'WON' ? 'gagné' : 'perdu'} en ${msg.attemptsUsed} tentatives`)
  } else if (msg.type === 'error') {
    isAdvancingRound.value = false
    error.value = msg.message || 'Erreur inconnue'
    // Auto-effacer après 1 seconde
    setTimeout(() => {
      error.value = ''
    }, 1000)
  } else if (msg.type === 'player_left') {
    // Si le joueur courant quitte, retourner à l'homepage
    if (String(msg.userId) === String(userIdCookie.value)) {
      router.push('/homepage')
    }
    playersState.value = playersState.value.filter(p => String(p.id) !== String(msg.userId))
  } else if (msg.type === 'lobbyInfo') {
    // Mettre à jour le statut d'hôte quand il y a un changement dans le lobby
    if (msg.hostId !== undefined) {
      isHost.value = String(msg.hostId) === String(userIdCookie.value)
    }
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
    // Si le round est déjà terminé (par le serveur), arrêter le timer
    if (roundFinished.value) {
      stopTimer()
      return
    }
    
    if (timerEndAtMs === null) {
      stopTimer()
      return
    }

    const remaining = Math.ceil((timerEndAtMs - Date.now()) / 1000)
    if (remaining > 0) {
      timeRemaining.value = remaining
    } else {
      // Ne déclencher la fin que si le round n'est pas déjà terminé
      if (!roundFinished.value) {
        timeRemaining.value = 0
        roundFinished.value = true
        roundFinishedReason.value = 'TIMER'
        hasNextRound.value = currentRound.value < rounds.value
        // Charger l'état du game depuis le serveur pour vérifier/mettre à jour le statut
        send({ type: 'LOAD_GAME', sessionCode })
      }
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

const showLeaderboard = () => {
  if (!isHost.value || !roundFinished.value || hasNextRound.value) return
  send({
    type: 'SHOW_LEADERBOARD',
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
  if (isHelpModalOpen.value) {
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
  <div class="relative flex flex-col min-h-[100dvh] w-full overflow-y-auto">
    <div class="fixed inset-0 bg-white/5 pointer-events-none z-0"></div>
    <Header
      title="Partie en cours"
      @home="goHome"
      @settings="goToSettings"
      @help="isHelpModalOpen = true"
      @contact="goToContact"
      @profile="goToProfile"
    />
    
    <!-- Message en survol pour la fin de round -->
    <div v-if="roundFinished" class="fixed top-1/3 -translate-y-1/2 left-1/2 -translate-x-1/2 sm:left-4 sm:translate-x-0 z-50 pointer-events-none w-full max-w-[90vw] sm:max-w-xs px-4 sm:px-0">
      <div class="pointer-events-auto bg-gradient-to-br from-slate-800 to-slate-900 border-2 border-white/20 rounded-xl p-3 sm:p-4 shadow-xl backdrop-blur-sm animate-[slideDown_0.3s_ease-out]">
        <div class="text-center space-y-2">
          <!-- Message de fin de round -->
          <div class="text-white text-sm sm:text-base font-bold">
            <div v-if="roundFinishedReason === 'TIMER' && hasNextRound">⏱️ Temps écoulé</div>
            <div v-else-if="roundFinishedReason === 'TIMER' && !hasNextRound">🎉 Partie terminée</div>
            <div v-else-if="roundFinishedReason === 'ALL_PLAYERS_FINISHED' && hasNextRound">✅ Tous les joueurs ont fini</div>
            <div v-else-if="roundFinishedReason === 'ALL_PLAYERS_FINISHED' && !hasNextRound">🎉 Partie terminée</div>
            <div v-else-if="roundFinishedReason === 'SESSION_FINISHED'">🎉 Partie terminée</div>
            <div v-else>Round terminé</div>
          </div>
          
          <!-- Affichage du mot correct -->
          <div v-if="correctAnswer" class="text-center pt-2 border-t border-white/20">
            <div class="text-white/70 text-xs">La bonne réponse:</div>
            <div class="text-yellow-300 text-lg sm:text-xl font-bold tracking-widest">{{ correctAnswer.toUpperCase() }}</div>
          </div>
          
          <!-- Bouton pour l'hôte ou message d'attente -->
          <div v-if="isHost && hasNextRound">
            <button
              @click="goNextRound"
              :disabled="isAdvancingRound"
              class="w-full bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold rounded-lg px-3 sm:px-4 py-2 shadow-lg transition-all text-xs sm:text-sm"
            >
              {{ isAdvancingRound ? 'Lancement...' : '▶️ Prochain round' }}
            </button>
          </div>
          <div v-if="isHost && !hasNextRound">
            <button
              @click="showLeaderboard"
              class="w-full bg-gradient-to-r from-yellow-500 to-yellow-600 hover:from-yellow-600 hover:to-yellow-700 text-white font-bold rounded-lg px-3 sm:px-4 py-2 shadow-lg transition-all text-xs sm:text-sm"
            >
              🏆 Voir le classement
            </button>
          </div>
          <div v-else-if="!isHost && hasNextRound" class="text-white/80 text-xs sm:text-sm">
            ⏳ En attente de l'hôte...
          </div>
          <div v-else-if="!isHost && !hasNextRound" class="text-white/80 text-xs sm:text-sm">
            ⏳ En attente du classement
          </div>
        </div>
      </div>
    </div>
    
    <!-- Message de victoire/défaite personnel (indépendant du round) -->
    <div v-if="gameStatus === 'WON' || gameStatus === 'LOST'" class="fixed top-1/2 translate-y-16 left-1/2 -translate-x-1/2 sm:left-4 sm:translate-x-0 z-50 pointer-events-none w-full max-w-[90vw] sm:max-w-xs px-4 sm:px-0">
      <div class="pointer-events-auto rounded-xl p-3 sm:p-4 shadow-xl backdrop-blur-sm border-2" 
           :class="gameStatus === 'WON' ? 'bg-gradient-to-br from-green-800 to-green-900 border-green-400/40' : 'bg-gradient-to-br from-orange-800 to-orange-900 border-orange-400/40'">
        <div v-if="gameStatus === 'WON'" class="text-green-300 text-sm sm:text-base font-semibold text-center">
          ✨ Bravo ! Vous avez trouvé le mot en {{ guesses.length }} essai{{ guesses.length > 1 ? 's' : '' }} !
        </div>
        <div v-else-if="gameStatus === 'LOST'" class="text-orange-300 text-sm sm:text-base font-semibold text-center">
          💡 Dommage ! Vous n'avez pas trouvé le mot.
        </div>
      </div>
    </div>
    
    <!-- Message d'erreur en survol -->
    <Transition name="fade">
      <div v-if="error" class="fixed top-24 left-1/2 -translate-x-1/2 z-50 pointer-events-none">
        <div class="pointer-events-auto bg-red-500/90 border-2 border-red-400 text-white px-6 py-4 rounded-xl shadow-2xl backdrop-blur-sm">
          {{ error }}
        </div>
      </div>
    </Transition>

    <div v-if="!loading" class="fixed top-24 right-3 z-30 w-64 hidden xl:block">
      <div class="rounded-xl border border-white/20 bg-slate-900/60 backdrop-blur-sm p-3">
        <div class="text-white font-semibold mb-2">Joueurs ({{ playersState.length }})</div>
        <div class="space-y-2 max-h-[45vh] overflow-y-auto pr-1">
          <div v-for="player in playersState" :key="player.id" class="flex items-center justify-between gap-2 rounded-lg bg-white/5 px-2 py-2">
            <div class="flex items-center gap-2 min-w-0">
              <img v-if="player.picture" :src="player.picture" :alt="player.name" class="w-7 h-7 rounded-full" />
              <div v-else class="w-7 h-7 rounded-full bg-white/20 text-white text-xs flex items-center justify-center">
                {{ player.name?.charAt(0)?.toUpperCase() }}
              </div>
              <span class="text-sm text-white truncate">{{ player.name }}</span>
            </div>
            <span
              class="text-xs font-semibold px-2 py-1 rounded"
              :class="{
                'bg-emerald-500/20 text-emerald-300': player.status === 'WON',
                'bg-rose-500/20 text-rose-300': player.status === 'LOST',
                'bg-sky-500/20 text-sky-300': player.status === 'IN_PROGRESS'
              }"
            >
              {{ statusLabel[player.status] }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <main class="relative z-5 flex flex-1 min-h-0 w-full items-start xl:items-center justify-center px-3 py-4 sm:px-4 lg:px-6 overflow-y-auto">
      <div class="flex flex-col flex-1 min-h-0 w-full max-w-xl h-auto xl:h-full gap-3 sm:gap-4 justify-start xl:justify-between items-center">
        <div v-if="loading" class="relative z-5 flex items-center justify-center flex-1 text-white w-full">Chargement...</div>
        <template v-else>
          <!-- Affichage des settings et du timer -->
          <div class="w-full flex flex-col items-center gap-2 mb-4">
            <div class="flex flex-wrap justify-center gap-2 sm:gap-4 text-white text-sm sm:text-base lg:text-lg font-semibold">
              <div class="bg-white/10 px-4 py-2 rounded-lg">
                Round: <span class="text-blue-400">{{ currentRound }}/{{ rounds }}</span>
              </div>
              <div v-if="!roundFinished" class="bg-white/10 px-4 py-2 rounded-lg" :class="{ 'text-red-400': timeRemaining <= 10 }">
                ⏱️ {{ timeRemaining }}s
              </div>
              <div class="bg-white/10 px-4 py-2 rounded-lg">
                Lettres: <span class="text-green-400">{{ wordLength }}</span>
              </div>
            </div>
          </div>

          <div class="flex-1 min-h-0 w-full flex items-center justify-center">
            <div class="w-full flex justify-center">
              <div class="max-w-[300px] w-full sm:max-w-[340px] xl:max-w-[360px]">
                <WordleGrid :guesses="guesses" :currentGuess="currentGuess" :maxGuesses="maxGuesses" :wordLength="wordLength" />
              </div>
            </div>
          </div>
          <div class="w-full flex justify-center">
            <div class="max-w-[320px] w-full sm:max-w-[360px] xl:max-w-[380px]">
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
  </div>
</template>

<style scoped>
.fade-enter-active {
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}

.fade-leave-active {
  transition: opacity 0.7s ease-in, transform 0.7s ease-in;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
</style>
