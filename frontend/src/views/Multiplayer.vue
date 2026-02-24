<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import Header from '../components/Header.vue'

const router = useRouter()
const mode = ref<'menu' | 'create' | 'join'>('menu')
const joinCode = ref('')

const showCreateForm = () => {
  mode.value = 'create'
}

const showJoinForm = () => {
  mode.value = 'join'
}

const backToMenu = () => {
  mode.value = 'menu'
  joinCode.value = ''
}

const goHome = () => {
  router.push('/homepage')
}

const goToSettings = () => {
  router.push('/settings')
}

const goToContact = () => {
  router.push('/contact')
}

const goToProfile = () => {
  router.push('/settings?tab=profil')
}

const showHelp = () => {
  // TODO: implémenter modal aide
}

const createSession = () => {
  // TODO: implémenter création de session
  console.log('Créer une session')
}

const joinSession = () => {
  if (joinCode.value.trim()) {
    // TODO: implémenter rejoindre session
    console.log('Rejoindre session:', joinCode.value)
  }
}
</script>

<template>
  <div class="multiplayer-wrapper">
    <div class="gradient-overlay"></div>

    <Header 
      title="MULTIJOUEUR"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />

    <main class="main-content">
      <!-- Menu principal -->
      <div v-if="mode === 'menu'" class="menu-container">
        <button @click="showCreateForm" class="action-button primary">
          Créer une partie
        </button>
        <button @click="showJoinForm" class="action-button secondary">
          Rejoindre une partie
        </button>
      </div>

      <!-- Formulaire créer -->
      <div v-else-if="mode === 'create'" class="form-container">
        <h2 class="form-title">Créer une partie</h2>
        <p class="form-description">Configuration de la session multijoueur</p>
        
        <div class="form-content">
          <p class="placeholder-text">Formulaire de configuration à venir...</p>
        </div>

        <div class="form-actions">
          <button @click="backToMenu" class="btn-secondary">Retour</button>
          <button @click="createSession" class="btn-primary">Créer</button>
        </div>
      </div>

      <!-- Formulaire rejoindre -->
      <div v-else-if="mode === 'join'" class="form-container">
        <h2 class="form-title">Rejoindre une partie</h2>
        <p class="form-description">Entrez le code de la session</p>
        
        <div class="form-content">
          <input 
            v-model="joinCode"
            type="text"
            placeholder="Code de la partie (ex: ABC123)"
            class="code-input"
            maxlength="10"
            @keyup.enter="joinSession"
          />
        </div>

        <div class="form-actions">
          <button @click="backToMenu" class="btn-secondary">Retour</button>
          <button @click="joinSession" class="btn-primary" :disabled="!joinCode.trim()">Rejoindre</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.multiplayer-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

.gradient-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.03);
  pointer-events: none;
}

.main-content {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

/* Menu principal */
.menu-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  align-items: center;
}

.action-button {
  padding: 2.5rem 4rem;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border: 0.5px solid rgba(255, 255, 255, 0.25);
  border-radius: 28px;
  color: white;
  font-size: 2.5rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  min-width: 400px;
}

.action-button.secondary {
  padding: 2rem 3.5rem;
  font-size: 2rem;
  background: rgba(255, 255, 255, 0.08);
}

.action-button:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: scale(1.02);
  box-shadow: 
    0 24px 70px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.action-button:active {
  transform: scale(0.98);
}

/* Formulaires */
.form-container {
  max-width: 600px;
  width: 100%;
  padding: 3rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border: 0.5px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.form-title {
  font-size: 2rem;
  font-weight: 700;
  color: white;
  margin-bottom: 0.5rem;
  text-align: center;
}

.form-description {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
  margin-bottom: 2rem;
}

.form-content {
  margin-bottom: 2rem;
}

.placeholder-text {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
}

.code-input {
  width: 100%;
  padding: 1.25rem;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  color: white;
  font-size: 1.5rem;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  transition: all 0.2s ease;
}

.code-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
  text-transform: none;
  letter-spacing: normal;
}

.code-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.4);
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-primary,
.btn-secondary {
  padding: 1rem 2.5rem;
  border-radius: 16px;
  font-size: 1.125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 0.5px solid rgba(255, 255, 255, 0.2);
}

.btn-primary {
  background: rgba(255, 255, 255, 0.18);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.02);
}

.btn-primary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn-primary:active:not(:disabled),
.btn-secondary:active {
  transform: scale(0.98);
}
</style>