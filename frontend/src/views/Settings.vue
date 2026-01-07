<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { useTheme } from '../composables/useTheme'
import { authenticatedFetch } from '../utils/api'
import Modal from '../components/Modal.vue'
import Header from '../components/Header.vue'

const router = useRouter()
const { logout } = useAuth()
const { theme, toggleTheme, isDark } = useTheme()
const activeTab = ref<'settings' | 'user'>('settings')
const userProfile = ref<any>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const isHelpModalOpen = ref(false)

const goHome = () => {
  router.push('/homepage')
}

const showHelp = () => {
  isHelpModalOpen.value = true
}

const closeHelpModal = () => {
  isHelpModalOpen.value = false
}

const goToContact = () => {
  router.push('/contact')
}

const goToSettings = () => {
  router.push('/settings')
}

const goToProfile = () => {
  activeTab.value = 'user'
  // Mettre à jour l'URL sans recharger la page
  router.replace({ query: { tab: 'profil' } })
}

const handleLogout = () => {
  logout()
  router.push('/login')
}

onMounted(async () => {
  console.log('Settings mounted')
  
  // Vérifier si on doit ouvrir l'onglet profil
  const route = router.currentRoute.value
  if (route.query.tab === 'profil') {
    activeTab.value = 'user'
  }
  
  loading.value = true
  error.value = null
  try {
    console.log('Fetching user profile...')
    const response = await authenticatedFetch('http://localhost:8080/api/user/profile')
    console.log('Response status:', response.status)
    const data = await response.json()
    console.log('User profile data:', data)
    userProfile.value = data
  } catch (err) {
    console.error('Error fetching profile:', err)
    error.value = err instanceof Error ? err.message : 'Erreur lors du chargement du profil'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="settings-wrapper">
    <!-- Couche d'atténuation du gradient -->
    <div class="gradient-overlay"></div>

    <!-- Header -->
    <Header 
      title="PARAMÈTRES"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />

    <!-- Contenu principal -->
    <div class="content-wrapper">
      <div class="content-container">
        <!-- Tabs -->
        <div class="tabs-container">
          <button 
            @click="activeTab = 'settings'"
            :class="['tab-button', { 'active': activeTab === 'settings' }]"
          >
            Paramètres
          </button>
          <button 
            @click="activeTab = 'user'"
            :class="['tab-button', { 'active': activeTab === 'user' }]"
          >
            Profil
          </button>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="tab-content">
          <div class="glass-card">
            <h2 class="card-title">Apparence</h2>
            
            <div class="settings-list">
              <div class="setting-item theme-setting">
                <div class="setting-info">
                  <label class="setting-label">Thème</label>
                  <p class="setting-description">{{ isDark() ? 'Mode sombre' : 'Mode clair' }}</p>
                </div>
                <button 
                  @click="toggleTheme"
                  class="theme-toggle-button"
                >
                  <svg v-if="isDark()" xmlns="http://www.w3.org/2000/svg" class="theme-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="theme-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <div class="glass-card">
            <h2 class="card-title">Paramètres de jeu</h2>
            
            <div class="settings-list">
              <div class="setting-item">
                <label class="setting-label">Son</label>
                <input type="checkbox" class="setting-checkbox" checked />
              </div>
              
              <div class="setting-item">
                <label class="setting-label">Notifications</label>
                <input type="checkbox" class="setting-checkbox" />
              </div>
            </div>
          </div>

          <div class="glass-card">
            <h2 class="card-title">À propos</h2>
            <p class="card-text">Wordle+ v1.0.0</p>
            <p class="card-subtext">Un jeu de mots amusant et addictif</p>
          </div>
        </div>

        <!-- User Tab -->
        <div v-if="activeTab === 'user'" class="tab-content">
          <div class="glass-card">
            
            <div v-if="loading" class="loading-state">
              Chargement...
            </div>

            <div v-if="error" class="error-state">
              {{ error }}
            </div>

            <div v-if="userProfile && !loading" class="profile-content">
              <!-- Photo de profil -->
              <div class="profile-picture-wrapper">
                <img 
                  v-if="userProfile.picture"
                  :src="userProfile.picture" 
                  :alt="userProfile.name || 'Photo de profil'"
                  class="profile-picture"
                  referrerpolicy="no-referrer"
                  @error="(e) => { console.error('Image load error:', e); (e.target as HTMLImageElement).style.display = 'none' }"
                />
                <div 
                  v-if="!userProfile.picture"
                  class="profile-picture-placeholder"
                >
                  <span class="profile-initial">
                    {{ userProfile.name ? userProfile.name.charAt(0).toUpperCase() : '?' }}
                  </span>
                </div>
              </div>
              
              <div class="profile-field">
                <label class="field-label">Email</label>
                <p class="field-value">{{ userProfile.email || 'Non disponible' }}</p>
              </div>
              
              <div class="profile-field">
                <label class="field-label">Nom</label>
                <p class="field-value">{{ userProfile.name || 'Non disponible' }}</p>
              </div>
              
              <!--<div class="profile-field">
                <label class="field-label">ID Utilisateur</label>
                <p class="field-value field-value-mono">{{ userProfile.sub || 'Non disponible' }}</p>
              </div>-->
              
              <div class="profile-field">
                <label class="field-label">Statistiques</label>
                <div class="stats-grid">
                  <div class="stat-card">
                    <p class="stat-label">Parties jouées</p>
                    <p class="stat-value">0</p>
                  </div>
                  <div class="stat-card">
                    <p class="stat-label">Victoires</p>
                    <p class="stat-value">0</p>
                  </div>
                  <div class="stat-card">
                    <p class="stat-label">Taux</p>
                    <p class="stat-value">0%</p>
                  </div>
                </div>
              </div>
              
              <!-- Bouton déconnexion -->
              <div class="logout-wrapper">
                <button 
                  @click="handleLogout"
                  class="logout-button"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="logout-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Déconnexion
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
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
  </div>
</template>

<style scoped>
/* Wrapper principal */
.settings-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  overflow: hidden;
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

/* Titre du header */
.header-title {
  color: white;
  font-size: 1.875rem;
  font-weight: 700;
  margin: 0;
  text-align: center;
  flex: 1;
  letter-spacing: 0.05em;
}

/* Actions du header */
.header-actions {
  display: flex;
  gap: 1rem;
}

/* Bouton icône */
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
  width: 1.375rem;
  height: 1.375rem;
  stroke-width: 2;
}

/* Contenu wrapper */
.content-wrapper {
  position: relative;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.content-wrapper::-webkit-scrollbar {
  display: none;
}

/* Container du contenu */
.content-container {
  max-width: 56rem;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

/* Tabs */
.tabs-container {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 0.5rem;
  border-bottom: 0.5px solid rgba(255, 255, 255, 0.15);
  position: sticky;
  top: 0;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  z-index: 10;
}

.tab-button {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: rgba(255, 255, 255, 0.5);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-button:hover {
  color: rgba(255, 255, 255, 0.8);
}

.tab-button.active {
  color: white;
  border-bottom-color: white;
}

/* Contenu des tabs */
.tab-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Cartes avec effet glass */
.glass-card {
  padding: 2rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border: 0.5px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 1.5rem;
}

.card-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1rem;
}

.card-subtext {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

/* Liste des paramètres */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setting-item.theme-setting {
  padding: 0.5rem 0;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.setting-label {
  color: white;
  font-size: 1rem;
  font-weight: 500;
}

.setting-description {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.875rem;
  margin: 0;
}

.theme-toggle-button {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 12px;
  cursor: pointer;
  padding: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.theme-toggle-button:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
}

.theme-toggle-button:active {
  transform: scale(0.95);
}

.theme-icon {
  width: 1.5rem;
  height: 1.5rem;
}

.setting-checkbox {
  width: 1.5rem;
  height: 1.5rem;
  cursor: pointer;
  accent-color: white;
}

/* États de chargement et d'erreur */
.loading-state {
  color: white;
  text-align: center;
  padding: 2rem;
}

.error-state {
  padding: 1rem 1.25rem;
  background: rgba(239, 68, 68, 0.15);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(239, 68, 68, 0.3);
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.95);
  font-size: 0.875rem;
}

/* Contenu du profil */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Photo de profil */
.profile-picture-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.profile-picture {
  width: 8rem;
  height: 8rem;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
}

.profile-picture-placeholder {
  width: 8rem;
  height: 8rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: 3px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-initial {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
}

/* Champs du profil */
.profile-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.875rem;
  font-weight: 500;
}

.field-value {
  color: white;
  font-size: 1rem;
}

.field-value-mono {
  font-family: monospace;
  font-size: 0.875rem;
}

/* Grille des statistiques */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-top: 1rem;
}

.stat-card {
  padding: 1.25rem;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  text-align: center;
}

.stat-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.75rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  color: white;
  font-size: 1.75rem;
  font-weight: 700;
}

/* Bouton de déconnexion */
.logout-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

.logout-button {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.875rem 1.75rem;
  background: rgba(239, 68, 68, 0.2);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(239, 68, 68, 0.4);
  border-radius: 14px;
  color: white;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-button:hover {
  background: rgba(239, 68, 68, 0.3);
  transform: scale(1.02);
}

.logout-button:active {
  transform: scale(0.98);
}

.logout-icon {
  width: 1.25rem;
  height: 1.25rem;
  stroke-width: 2;
}

/* Responsive */
@media (max-width: 768px) {
  .glass-header {
    padding: 1.25rem 1.5rem;
  }

  .header-title {
    font-size: 1.5rem;
  }

  .content-container {
    padding: 1.5rem 1rem;
  }

  .glass-card {
    padding: 1.5rem;
    border-radius: 20px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
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

  .tabs-container {
    gap: 0.5rem;
  }

  .tab-button {
    padding: 0.625rem 1rem;
    font-size: 0.9rem;
  }

  .glass-card {
    padding: 1.25rem;
    border-radius: 18px;
  }

  .card-title {
    font-size: 1.25rem;
  }
}
</style>
