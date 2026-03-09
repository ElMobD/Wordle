<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'
import { authenticatedFetch } from '../utils/api'
import Header from '../components/Header.vue'
import Modal from '../components/Modal.vue'
import WordleGrid from '../components/WordleGrid.vue'
import WordleKeyboard from '../components/WordleKeyboard.vue'

const router = useRouter()
const isHelpModalOpen = ref(false)
const isResultModalOpen = ref(false)
const guesses = ref<any[]>([])
const currentGuess = ref('')
const gameId = ref<string>('')
const gameStatus = ref<'IN_PROGRESS' | 'WON' | 'LOST'>('IN_PROGRESS')
const answer = ref('')
const maxGuesses = 6
const wordLength = 5
const loading = ref(true)
const error = ref('')
let errorTimeout: number | null = null

// Créer ou récupérer une partie DAILY au chargement
onMounted(async () => {
  try {
    const response = await authenticatedFetch('http://localhost/api/games', {
      method: 'POST',
      body: JSON.stringify({ gameType: 'DAILY' })
    })
    
    if (response.ok) {
      const data = await response.json()
      gameId.value = data.id
      guesses.value = data.guesses || []
      gameStatus.value = data.status
      answer.value = data.answer || ''
      isResultModalOpen.value = data.status === 'WON' || data.status === 'LOST'
    } else {
      showError('Erreur lors du chargement de la partie')
    }
  } catch (err) {
    showError('Erreur réseau')
    console.error(err)
  } finally {
    loading.value = false
  }
  
  // Ajouter l'écouteur de clavier physique
  window.addEventListener('keydown', handlePhysicalKeyPress)
})

onUnmounted(() => {
  // Nettoyer l'écouteur de clavier
  window.removeEventListener('keydown', handlePhysicalKeyPress)
  // Annuler le timeout en cas de démontage
  if (errorTimeout) clearTimeout(errorTimeout)
})

const showError = (message: string) => {
  // Annuler le timeout précédent s'il existe
  if (errorTimeout) clearTimeout(errorTimeout)
  error.value = message
  // Auto-effacer après 2 secondes
  errorTimeout = window.setTimeout(() => {
    error.value = ''
    errorTimeout = null
  }, 2000)
}

// Soumettre un mot à l'API
const submitWord = async (word: string) => {
  if (!gameId.value || gameStatus.value !== 'IN_PROGRESS') return
  
  try {
    const response = await authenticatedFetch(
      `http://localhost/api/games/${gameId.value}/guess`,
      {
        method: 'POST',
        body: JSON.stringify({ word: word.toUpperCase() })
      }
    )
    
    if (response.ok) {
      const data = await response.json()
      guesses.value = data.guesses || []
      gameStatus.value = data.status
      answer.value = data.answer || ''
      
      if (gameStatus.value === 'WON' || gameStatus.value === 'LOST') {
        isResultModalOpen.value = true
      }
    } else {
      const errData = await response.json()
      showError(errData.error || 'Erreur lors de la soumission')
    }
  } catch (err) {
    showError('Erreur réseau')
    console.error(err)
  }
}

const goHome = () => {
  router.push('/homepage')
}

const closeResultModal = () => {
  isResultModalOpen.value = false
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
  if (key === 'ENTER') {
    if (currentGuess.value.length === wordLength && gameStatus.value === 'IN_PROGRESS') {
      submitWord(currentGuess.value)
      currentGuess.value = ''
    }
  } else if (key === 'BACKSPACE') {
    currentGuess.value = currentGuess.value.slice(0, -1)
  } else if (currentGuess.value.length < wordLength) {
    currentGuess.value += key.toUpperCase()
  }
}

// Gérer les touches du clavier physique
const handlePhysicalKeyPress = (event: KeyboardEvent) => {
  // Ignorer si un modal ou input est ouvert
  const activeElement = document.activeElement as HTMLElement
  if (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA') {
    return
  }
  
  // Ignorer si un modal est ouvert
  if (isHelpModalOpen.value || isResultModalOpen.value) {
    return
  }
  
  const key = event.key.toUpperCase()
  
  // Gérer ENTER
  if (event.key === 'Enter') {
    event.preventDefault()
    handleKeyPress('ENTER')
    return
  }
  
  // Gérer BACKSPACE
  if (event.key === 'Backspace') {
    event.preventDefault()
    handleKeyPress('BACKSPACE')
    return
  }
  
  // Gérer les lettres A-Z
  if (key.length === 1 && /^[A-Z]$/.test(key)) {
    event.preventDefault()
    handleKeyPress(key)
  }
}
</script>

<template>
  <!-- Couche d'atténuation du gradient -->
  <div class="relative flex flex-col min-h-[100dvh] w-full overflow-y-auto">
    <div class="fixed inset-0 bg-white/5 pointer-events-none z-0"></div>
    <Header
      title="Wordle du Jour"
      @home="goHome"
      @settings="goToSettings"
      @help="isHelpModalOpen = true"
      @contact="goToContact"
      @profile="goToProfile"
    />
    <!-- Message d'erreur en survol -->
    <Transition name="fade">
      <div v-if="error" class="fixed top-24 left-1/2 -translate-x-1/2 z-50 pointer-events-none">
        <div class="pointer-events-auto bg-red-500/90 border-2 border-red-400 text-white px-6 py-4 rounded-xl shadow-2xl backdrop-blur-sm">
          {{ error }}
        </div>
      </div>
    </Transition>
    <main class="relative z-5 flex flex-1 min-h-0 w-full items-start xl:items-center justify-center px-3 py-4 sm:px-4 lg:px-6 overflow-y-auto">
      <div class="flex flex-col flex-1 min-h-0 w-full max-w-xl h-auto xl:h-full gap-3 sm:gap-4 justify-start xl:justify-between items-center">
        <div v-if="loading" class="relative z-5 flex items-center justify-center flex-1 text-white w-full">Chargement...</div>
        <template v-else>
          <div class="flex-1 min-h-0 w-full flex items-center justify-center">
            <div class="w-full flex justify-center">
              <div class="max-w-[300px] w-full sm:max-w-[340px] xl:max-w-[360px]">
                <WordleGrid :guesses="guesses" :currentGuess="currentGuess" :maxGuesses="maxGuesses" :wordLength="wordLength" />
              </div>
            </div>
          </div>
          <div class="w-full flex justify-center">
            <div class="max-w-[320px] w-full sm:max-w-[360px] xl:max-w-[380px]">
              <WordleKeyboard :guesses="guesses" @keyPress="handleKeyPress" />
            </div>
          </div>
        </template>
      </div>
    </main>
    <Modal :isOpen="isHelpModalOpen" @close="isHelpModalOpen = false">
      <div class="flex flex-col gap-4">
        <h3 class="my-2 text-lg text-white font-semibold">Comment jouer ?</h3>
        <p class="my-2 opacity-95">Trouvez le mot du jour en un minimum d'essais. Chaque essai doit être un mot valide de 5 lettres.</p>
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
    <Modal :isOpen="isResultModalOpen" @close="closeResultModal">
      <div class="flex flex-col items-center gap-6 py-4">
        <div class="text-6xl leading-none">{{ gameStatus === 'WON' ? '✨' : '💡' }}</div>
        <p class="text-lg text-center text-white/95 m-0">
          {{ gameStatus === 'WON' 
            ? `Bravo ! Vous avez trouvé le mot en ${guesses.length} essai${guesses.length > 1 ? 's' : ''} !` 
            : 'Dommage ! Revenez demain pour un nouveau mot !' 
          }}
        </p>
        <div v-if="answer" class="text-center pt-2 border-t border-white/20 w-full">
          <div class="text-white/70 text-xs">La bonne reponse :</div>
          <div class="text-yellow-300 text-lg sm:text-xl font-bold tracking-widest">{{ answer.toUpperCase() }}</div>
        </div>
        <div class="flex flex-wrap gap-6 px-4 sm:px-8 py-4 bg-white/5 rounded-xl w-full justify-center">
          <div class="flex flex-col items-center gap-2">
            <span class="text-2xl font-bold text-white">{{ guesses.length }}</span>
            <span class="text-sm text-white/70 uppercase tracking-wide">Essais</span>
          </div>
          <div class="flex flex-col items-center gap-2">
            <span class="text-2xl font-bold text-white">{{ maxGuesses }}</span>
            <span class="text-sm text-white/70 uppercase tracking-wide">Maximum</span>
          </div>
        </div>
        <button @click="goHome" class="bg-indigo-600/80 border border-indigo-600 text-white px-8 py-3 rounded-xl text-base font-semibold cursor-pointer transition-all mt-4 hover:bg-indigo-600 hover:scale-105 hover:shadow-lg">Retour à l'accueil</button>
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
