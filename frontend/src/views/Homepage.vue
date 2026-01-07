<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import Header from '../components/Header.vue'
import Modal from '../components/Modal.vue'

const router = useRouter()
const isHelpModalOpen = ref(false)

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
</script>

<template>
  <div class="homepage-wrapper">
    <!-- Couche d'atténuation du gradient -->
    <div class="gradient-overlay"></div>

    <!-- Header -->
    <Header 
      title="WORDLE+"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
    />

    <!-- Contenu principal -->
    <main class="main-content">
      <button 
        @click="router.push('/daily-word')"
        class="play-button"
      >
        Mot du jour
      </button>
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
.homepage-wrapper {
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
}

/* Header avec effet glass */
.glass-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border-bottom: 0.5px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* Titre du header */
.header-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: white;
  letter-spacing: 0.05em;
}

/* Actions du header */
.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

/* Boutons icônes */
.icon-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.625rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-button:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: scale(1.05);
}

.icon-button:active {
  transform: scale(0.95);
}

.icon {
  width: 1.375rem;
  height: 1.375rem;
  stroke-width: 2;
}

/* Contenu principal */
.main-content {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

/* Bouton principal "Mot du jour" */
.play-button {
  padding: 3rem 5rem;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border: 0.5px solid rgba(255, 255, 255, 0.25);
  border-radius: 28px;
  color: white;
  font-size: 3rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.play-button:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: scale(1.02);
  box-shadow: 
    0 24px 70px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.play-button:active {
  transform: scale(0.98);
  box-shadow: 
    0 16px 50px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  .glass-header {
    padding: 1.25rem 1.5rem;
  }

  .header-title {
    font-size: 1.5rem;
  }

  .icon {
    width: 1.25rem;
    height: 1.25rem;
  }

  .play-button {
    padding: 2.5rem 4rem;
    font-size: 2.25rem;
    border-radius: 24px;
  }
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

  .play-button {
    padding: 2rem 3rem;
    font-size: 1.75rem;
    border-radius: 20px;
  }
}
</style>