<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import Header from '../components/Header.vue'
import Modal from '../components/Modal.vue'
import WordleGrid from '../components/WordleGrid.vue'
import WordleKeyboard from '../components/WordleKeyboard.vue'

const router = useRouter()
const isHelpModalOpen = ref(false)
const guesses = ref<string[]>([])
const currentGuess = ref('')
const maxGuesses = 6
const wordLength = 5

const goToSettings = () => {
  router.push('/settings')
}

const showHelp = () => {
  isHelpModalOpen.value = true
}

const closeHelpModal = () => {
  isHelpModalOpen.value = false
}

const goHome = () => {
  router.push('/homepage')
}

const goToContact = () => {
  router.push('/contact')
}

const handleKeyPress = (key: string) => {
  if (key === 'ENTER') {
    if (currentGuess.value.length === wordLength) {
      guesses.value.push(currentGuess.value)
      currentGuess.value = ''
    }
  } else if (key === 'BACKSPACE') {
    currentGuess.value = currentGuess.value.slice(0, -1)
  } else if (currentGuess.value.length < wordLength) {
    currentGuess.value += key
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
    />

    <!-- Contenu principal -->
    <main class="main-content">
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
        />
      </div>
    </main>

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
