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
const dailyStats = ref<any>(null)
const loading = ref(false)
const loadingStats = ref(false)
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

const fetchDailyStats = async () => {
  loadingStats.value = true
  try {
    const response = await authenticatedFetch('http://localhost:8080/api/stats/daily')
    const data = await response.json()
    dailyStats.value = data
  } catch (err) {
    console.error('Error fetching daily stats:', err)
  } finally {
    loadingStats.value = false
  }
}

const calculateWinRate = () => {
  if (!dailyStats.value || dailyStats.value.totalPlayed === 0) return '0%'
  const rate = (dailyStats.value.totalWon / dailyStats.value.totalPlayed) * 100
  return `${Math.round(rate)}%`
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
    
    // Charger les stats daily
    await fetchDailyStats()
  } catch (err) {
    console.error('Error fetching profile:', err)
    error.value = err instanceof Error ? err.message : 'Erreur lors du chargement du profil'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="relative flex flex-col h-full w-full overflow-hidden">
    <!-- Couche d'atténuation du gradient -->
    <div class="fixed inset-0 bg-white/5 pointer-events-none"></div>

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
    <div class="flex-1 flex flex-col">
      <div class="flex-1 overflow-y-auto max-h-[calc(100vh-5rem)] min-h-0">
        <div class="max-w-4xl mx-auto p-8 sm:p-6">
        <!-- Tabs -->
        <div class="flex gap-4 mb-8 pb-2 border-b border-white/15 sticky top-0 backdrop-blur-xl z-10">
          <button 
            @click="activeTab = 'settings'"
            :class="['px-6 py-3 text-base font-semibold border-b-2 transition-all duration-200', activeTab === 'settings' ? 'text-white border-white' : 'text-white/50 border-transparent hover:text-white/80']"
          >
            Paramètres
          </button>
          <button 
            @click="activeTab = 'user'"
            :class="['px-6 py-3 text-base font-semibold border-b-2 transition-all duration-200', activeTab === 'user' ? 'text-white border-white' : 'text-white/50 border-transparent hover:text-white/80']"
          >
            Profil
          </button>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="flex flex-col gap-6">
          <div class="p-8 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-2xl shadow-[0_12px_40px_rgba(0,0,0,0.1),_inset_0_1px_0_rgba(255,255,255,0.2)]">
            <h2 class="text-2xl font-bold text-white mb-6">Apparence</h2>
            <div class="flex flex-col gap-6">
              <div class="flex items-center justify-between py-2">
                <div class="flex flex-col gap-1">
                  <label class="text-white text-base font-medium">Thème</label>
                  <p class="text-white/60 text-sm m-0">{{ isDark() ? 'Mode sombre' : 'Mode clair' }}</p>
                </div>
                <button 
                  @click="toggleTheme"
                  class="bg-white/15 border border-white/30 text-white rounded-xl cursor-pointer p-3 flex items-center justify-center transition-all duration-200 hover:bg-white/25 hover:border-white/50 hover:scale-105 active:scale-95"
                >
                  <svg v-if="isDark()" xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <div class="p-8 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-2xl shadow-[0_12px_40px_rgba(0,0,0,0.1),_inset_0_1px_0_rgba(255,255,255,0.2)]">
            <h2 class="text-2xl font-bold text-white mb-6">Paramètres de jeu</h2>
            <div class="flex flex-col gap-6">
              <div class="flex items-center justify-between">
                <label class="text-white text-base font-medium">Son</label>
                <input type="checkbox" class="w-6 h-6 accent-white cursor-pointer" checked />
              </div>
              <div class="flex items-center justify-between">
                <label class="text-white text-base font-medium">Notifications</label>
                <input type="checkbox" class="w-6 h-6 accent-white cursor-pointer" />
              </div>
            </div>
          </div>

          <div class="p-8 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-2xl shadow-[0_12px_40px_rgba(0,0,0,0.1),_inset_0_1px_0_rgba(255,255,255,0.2)]">
            <h2 class="text-2xl font-bold text-white mb-4">À propos</h2>
            <p class="text-white/90 text-base">Wordle+ v1.0.0</p>
            <p class="text-white/60 text-sm mt-2">Un jeu de mots amusant et addictif</p>
          </div>
        </div>

        <!-- User Tab -->
        <div v-if="activeTab === 'user'" class="flex flex-col gap-6">
          <div class="p-8 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-2xl shadow-[0_12px_40px_rgba(0,0,0,0.1),_inset_0_1px_0_rgba(255,255,255,0.2)]">
            <div v-if="loading" class="text-white text-center p-8">Chargement...</div>
            <div v-if="error" class="p-4 bg-red-600/15 backdrop-blur-xl border border-red-600/30 rounded-lg text-white/95 text-sm">{{ error }}</div>
            <div v-if="userProfile && !loading" class="flex flex-col gap-6">
              <!-- Photo de profil -->
              <div class="flex justify-center mb-4">
                <img 
                  v-if="userProfile.picture"
                  :src="userProfile.picture" 
                  :alt="userProfile.name || 'Photo de profil'"
                  class="w-32 h-32 rounded-full border-4 border-white/30 object-cover"
                  referrerpolicy="no-referrer"
                  @error="(e) => { console.error('Image load error:', e); (e.target as HTMLImageElement).style.display = 'none' }"
                />
                <div 
                  v-if="!userProfile.picture"
                  class="w-32 h-32 rounded-full bg-white/15 border-4 border-white/30 flex items-center justify-center"
                >
                  <span class="text-4xl font-bold text-white">
                    {{ userProfile.name ? userProfile.name.charAt(0).toUpperCase() : '?' }}
                  </span>
                </div>
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-white/60 text-sm font-medium">Email</label>
                <p class="text-white text-base">{{ userProfile.email || 'Non disponible' }}</p>
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-white/60 text-sm font-medium">Nom</label>
                <p class="text-white text-base">{{ userProfile.name || 'Non disponible' }}</p>
              </div>
              <!--<div class="flex flex-col gap-2">
                <label class="text-white/60 text-sm font-medium">ID Utilisateur</label>
                <p class="text-white text-xs font-mono">{{ userProfile.sub || 'Non disponible' }}</p>
              </div>-->
              <div class="flex flex-col gap-2">
                <label class="text-white/60 text-sm font-medium">Statistiques</label>
                <div v-if="loadingStats" class="text-white/60 text-sm text-center py-4">Chargement des statistiques...</div>
                <div v-else class="grid grid-cols-3 gap-4 mt-2">
                  <div class="p-5 bg-white/10 backdrop-blur-xl border border-white/15 rounded-xl text-center">
                    <p class="text-white/60 text-xs mb-2">Parties jouées</p>
                    <p class="text-white text-2xl font-bold">{{ dailyStats?.totalPlayed ?? 0 }}</p>
                  </div>
                  <div class="p-5 bg-white/10 backdrop-blur-xl border border-white/15 rounded-xl text-center">
                    <p class="text-white/60 text-xs mb-2">Victoires</p>
                    <p class="text-white text-2xl font-bold">{{ dailyStats?.totalWon ?? 0 }}</p>
                  </div>
                  <div class="p-5 bg-white/10 backdrop-blur-xl border border-white/15 rounded-xl text-center">
                    <p class="text-white/60 text-xs mb-2">Taux</p>
                    <p class="text-white text-2xl font-bold">{{ calculateWinRate() }}</p>
                  </div>
                </div>
                <div v-if="!loadingStats" class="grid grid-cols-2 gap-4 mt-2">
                  <div class="p-5 bg-white/10 backdrop-blur-xl border border-white/15 rounded-xl text-center">
                    <p class="text-white/60 text-xs mb-2">Série actuelle</p>
                    <p class="text-white text-2xl font-bold">{{ dailyStats?.currentStreak ?? 0 }}</p>
                  </div>
                  <div class="p-5 bg-white/10 backdrop-blur-xl border border-white/15 rounded-xl text-center">
                    <p class="text-white/60 text-xs mb-2">Meilleure série</p>
                    <p class="text-white text-2xl font-bold">{{ dailyStats?.maxStreak ?? 0 }}</p>
                  </div>
                </div>
              </div>
              <!-- Bouton déconnexion -->
              <div class="flex justify-center mt-4">
                <button 
                  @click="handleLogout"
                  class="flex items-center gap-2 px-7 py-3 bg-red-600/20 backdrop-blur-xl border border-red-600/40 rounded-lg text-white text-base font-semibold cursor-pointer transition-all duration-200 hover:bg-red-600/30 hover:scale-105 active:scale-95"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
      </div>
    </div>

    <!-- Modal d'aide -->
    <Modal 
      :is-open="isHelpModalOpen"
      title="Comment jouer"
      @close="closeHelpModal"
    >
      <div class="flex flex-col gap-4">
        <h3 class="mt-2 mb-2 text-lg text-white font-semibold">Règles du jeu</h3>
        <p class="mb-2 opacity-95">Devinez le mot en 6 essais maximum !</p>
        
        <h3 class="mt-2 mb-2 text-lg text-white font-semibold">Comment jouer</h3>
        <ul class="mb-2 pl-6 opacity-95 list-disc">
          <li class="mb-1">Tapez un mot de 5 lettres</li>
          <li class="mb-1">Appuyez sur ENTRÉE pour valider</li>
          <li class="mb-1">Les couleurs changent pour vous indiquer si les lettres sont correctes :</li>
        </ul>

        <div class="flex flex-col gap-3 mt-4 p-4 bg-white/10 rounded-lg">
          <div class="p-3 rounded text-center font-medium bg-green-500/20 text-green-300">🟩 La lettre est au bon endroit</div>
          <div class="p-3 rounded text-center font-medium bg-yellow-500/20 text-yellow-200">🟨 La lettre existe mais au mauvais endroit</div>
          <div class="p-3 rounded text-center font-medium bg-gray-500/20 text-gray-300">⬜ La lettre n'existe pas dans le mot</div>
        </div>
      </div>
    </Modal>
</template>

