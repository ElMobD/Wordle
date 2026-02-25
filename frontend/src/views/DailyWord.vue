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
const maxGuesses = 6
const wordLength = 5
const loading = ref(true)
const error = ref('')

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
    } else {
      error.value = 'Erreur lors du chargement de la partie'
    }
  } catch (err) {
    error.value = 'Erreur réseau'
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
})

// Soumettre un mot à l'API
const submitWord = async (word: string) => {
  if (!gameId.value || gameStatus.value !== 'IN_PROGRESS') return
  
  try {
    const response = await authenticatedFetch(
      `http://localhost:8080/api/games/${gameId.value}/guess`,
      {
        method: 'POST',
        body: JSON.stringify({ word: word.toUpperCase() })
      }
    )
    
    if (response.ok) {
      const data = await response.json()
      guesses.value = data.guesses || []
      gameStatus.value = data.status
      
      if (gameStatus.value === 'WON' || gameStatus.value === 'LOST') {
        isResultModalOpen.value = true
      }
    } else {
      const errData = await response.json()
      error.value = errData.error || 'Erreur lors de la soumission'
    }
  } catch (err) {
    error.value = 'Erreur réseau'
    console.error(err)
  }
}

const goToSettings = () => {
  router.push('/settings')
}

const showHelp = () => {
  isHelpModalOpen.value = true
}

const closeHelpModal = () => {
  isHelpModalOpen.value = false
}

const closeResultModal = () => {
  isResultModalOpen.value = false
}

const goHome = () => {
  router.push('/homepage')
}

const goToContact = () => {
  router.push('/contact')
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
  <div class="dailyword-wrapper">
    <!-- Couche d'atténuation du gradient -->
    <div class="gradient-overlay"></div>

    <!-- Header -->
    <Header 
      title="MOT DU JOUR"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />

    <!-- Message d'erreur -->
    <div v-if="error" class="error-banner">
      {{ error }}
    </div>

    <!-- Contenu principal -->
    <main class="main-content" v-if="!loading">
      <div class="game-container">
        <!-- Grille Wordle -->
        <WordleGrid 
          :guesses="guesses"
          :current-guess="currentGuess"
          :max-guesses="maxGuesses"
          :word-length="wordLength"
        />
        
        <!-- Clavier -->
        <WordleKeyboard 
          @key-press="handleKeyPress"
          :disabled="gameStatus !== 'IN_PROGRESS'"
          :guesses="guesses"
        />
      </div>
    </main>

    <!-- Loading -->
    <div v-else class="loading-container">
      <p>Chargement de la partie...</p>
    </div>

    <!-- Modal d'aide -->
    <Modal 
      :is-open="isHelpModalOpen"
      title="Comment jouer"
      @close="closeHelpModal"
    >
      <div class="help-content">
        <h3>Règles du jeu</h3>
        <p>Devinez le mot en 6 essais maximum !</p>
        
        <h3>Comment jouer</h3>
        <ul>
          <li>Tapez un mot de 5 lettres</li>
          <li>Appuyez sur ENTRÉE pour valider</li>
          <li>Les couleurs changent pour vous indiquer si les lettres sont correctes :</li>
        </ul>

        <div class="hint-box">
          <div class="hint-item green">🟩 La lettre est au bon endroit</div>
          <div class="hint-item yellow">🟨 La lettre existe mais au mauvais endroit</div>
          <div class="hint-item gray">⬜ La lettre n'existe pas dans le mot</div>
        </div>
      </div>
    </Modal>

    <!-- Modal de résultat -->
    <Modal 
      :is-open="isResultModalOpen"
      :title="gameStatus === 'WON' ? '🎉 Victoire !' : '😔 Défaite'"
      @close="closeResultModal"
    >
      <div class="result-content">
        <div class="result-icon">
          {{ gameStatus === 'WON' ? '✨' : '💡' }}
        </div>
        <p class="result-message">
          {{ gameStatus === 'WON' 
            ? `Bravo ! Vous avez trouvé le mot en ${guesses.length} essai${guesses.length > 1 ? 's' : ''} !` 
            : 'Dommage ! Revenez demain pour un nouveau mot !' 
          }}
        </p>
        <div class="result-stats">
          <div class="stat">
            <span class="stat-value">{{ guesses.length }}</span>
            <span class="stat-label">Essais</span>
          </div>
          <div class="stat">
            <span class="stat-value">{{ maxGuesses }}</span>
            <span class="stat-label">Maximum</span>
          </div>
        </div>
        <button @click="goHome" class="result-button">
          Retour à l'accueil
        </button>
      </div>
    </Modal>
  </div>
</template>

<style scoped>
/* Wrapper principal */
.dailyword-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

/* Couche d'atténuation pour adoucir le gradient */
.gradient-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.03);
  pointer-events: none;
  z-index: 0;
}

/* Header avec effet glass */
.glass-header {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-title {
  color: white;
  font-size: 1.875rem;
  font-weight: 700;
  margin: 0;
  text-align: center;
  flex: 1;
  letter-spacing: 0.05em;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.icon-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 12px;
  cursor: pointer;
  padding: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.back-button {
  margin-right: 1rem;
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
}

/* Contenu principal */
.main-content {
  position: relative;
  z-index: 5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
}

.game-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 600px;
  height: 100%;
  gap: 1rem;
}

/* Styles du contenu d'aide */
.help-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.help-content h3 {
  margin: 0.5rem 0;
  font-size: 1.125rem;
  color: white;
  font-weight: 600;
}

.help-content p {
  margin: 0.5rem 0;
  opacity: 0.95;
}

.help-content ul {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
  opacity: 0.95;
}

.help-content li {
  margin: 0.25rem 0;
}

.hint-box {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
  padding: 1rem;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.hint-item {
  padding: 0.75rem;
  border-radius: 6px;
  text-align: center;
  font-weight: 500;
}

.hint-item.green {
  background-color: rgba(34, 197, 94, 0.2);
  color: #86efac;
}

.hint-item.yellow {
  background-color: rgba(202, 138, 4, 0.2);
  color: #fde047;
}

.hint-item.gray {
  background-color: rgba(107, 114, 128, 0.2);
  color: #d1d5db;
}

/* Error banner */
.error-banner {
  position: relative;
  z-index: 10;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.6);
  color: #fca5a5;
  padding: 1rem;
  text-align: center;
  border-radius: 8px;
}

/* Loading container */
.loading-container {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: white;
}

/* Result content */
.result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem 0;
}

.result-icon {
  font-size: 4rem;
  line-height: 1;
}

.result-message {
  font-size: 1.125rem;
  text-align: center;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
}

.result-stats {
  display: flex;
  gap: 2rem;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  width: 100%;
  justify-content: center;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: white;
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.result-button {
  background: rgba(99, 102, 241, 0.8);
  border: 1px solid rgba(99, 102, 241, 1);
  color: white;
  padding: 0.75rem 2rem;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 1rem;
}

.result-button:hover {
  background: rgba(99, 102, 241, 1);
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.4);
}

@media (max-width: 480px) {
  .glass-header {
    padding: 1rem 1.25rem;
  }

  .header-title {
    font-size: 1.25rem;
  }

  .header-actions {
    gap: 0.5rem;
  }

  .icon-button {
    padding: 0.5rem;
  }

  .coming-soon h2 {
    font-size: 1.5rem;
  }

  .coming-soon p {
    font-size: 1rem;
  }
}
</style>
