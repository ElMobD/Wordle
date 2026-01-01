<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { authenticatedFetch } from '../utils/api'

const router = useRouter()
const { user, logout } = useAuth()
const activeTab = ref<'settings' | 'user'>('settings')
const userProfile = ref<any>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const goBack = () => {
  router.push('/homepage')
}

const handleLogout = () => {
  logout()
  router.push('/login')
}

onMounted(async () => {
  console.log('Settings mounted')
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
  <div class="h-screen w-screen flex flex-col bg-gradient-to-b from-blue-600 to-red-600">
    <!-- Header -->
    <header class="h-20 bg-black/20 backdrop-blur-md flex items-center justify-between px-8 border-b border-white/20 flex-shrink-0">
      <h1 class="text-3xl font-black text-white tracking-widest">PARAMÈTRES</h1>
      
      <button 
        @click="goBack"
        class="text-white hover:bg-white/20 p-2 rounded-lg transition-all duration-200"
        title="Retour"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
      </button>
    </header>

    <!-- Content area -->
    <div class="flex-1 overflow-auto">
      <div class="max-w-4xl mx-auto py-8 px-6">
        <!-- Tabs -->
        <div class="flex gap-4 mb-8 border-b border-white/20">
          <button 
            @click="activeTab = 'settings'"
            :class="[
              'px-6 py-3 font-bold text-lg transition-all duration-200',
              activeTab === 'settings' 
                ? 'text-white border-b-2 border-white' 
                : 'text-white/60 hover:text-white'
            ]"
          >
            Paramètres
          </button>
          <button 
            @click="activeTab = 'user'"
            :class="[
              'px-6 py-3 font-bold text-lg transition-all duration-200',
              activeTab === 'user' 
                ? 'text-white border-b-2 border-white' 
                : 'text-white/60 hover:text-white'
            ]"
          >
            Profil
          </button>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="space-y-6">
          <div class="bg-white/10 backdrop-blur-md rounded-2xl p-8 border border-white/20">
            <h2 class="text-2xl font-bold text-white mb-6">Paramètres de jeu</h2>
            
            <div class="space-y-6">
              <div class="flex items-center justify-between">
                <label class="text-white font-semibold">Mode sombre</label>
                <input type="checkbox" class="w-6 h-6 cursor-pointer" checked disabled />
              </div>
              
              <div class="flex items-center justify-between">
                <label class="text-white font-semibold">Son</label>
                <input type="checkbox" class="w-6 h-6 cursor-pointer" checked />
              </div>
              
              <div class="flex items-center justify-between">
                <label class="text-white font-semibold">Notifications</label>
                <input type="checkbox" class="w-6 h-6 cursor-pointer" />
              </div>
            </div>
          </div>

          <div class="bg-white/10 backdrop-blur-md rounded-2xl p-8 border border-white/20">
            <h2 class="text-2xl font-bold text-white mb-6">À propos</h2>
            <p class="text-white/80">Wordle+ v1.0.0</p>
            <p class="text-white/60 text-sm mt-2">Un jeu de mots amusant et addictif</p>
          </div>
        </div>

        <!-- User Tab -->
        <div v-if="activeTab === 'user'" class="space-y-6">
          <div class="bg-white/10 backdrop-blur-md rounded-2xl p-8 border border-white/20">
            <h2 class="text-2xl font-bold text-white mb-8">Informations du profil</h2>
            
            <div v-if="loading" class="text-white text-center">
              Chargement...
            </div>

            <div v-if="error" class="bg-red-500/20 border border-red-500 rounded-lg p-4 text-red-300">
              {{ error }}
            </div>

            <div v-if="userProfile && !loading" class="space-y-6">
              <!-- Photo de profil -->
              <div class="flex justify-center mb-8">
                <div class="relative">
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
                    class="w-32 h-32 rounded-full bg-white/20 border-4 border-white/30 flex items-center justify-center"
                  >
                    <span class="text-4xl font-bold text-white">
                      {{ userProfile.name ? userProfile.name.charAt(0).toUpperCase() : '?' }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div>
                <label class="block text-white/60 text-sm mb-2">Email</label>
                <p class="text-white text-lg">{{ userProfile.email || 'Non disponible' }}</p>
              </div>
              
              <div>
                <label class="block text-white/60 text-sm mb-2">Nom</label>
                <p class="text-white text-lg">{{ userProfile.name || 'Non disponible' }}</p>
              </div>
              
              <div>
                <label class="block text-white/60 text-sm mb-2">ID Utilisateur</label>
                <p class="text-white text-lg font-mono text-sm">{{ userProfile.sub || 'Non disponible' }}</p>
              </div>
              
              <div>
                <label class="block text-white/60 text-sm mb-2">Statistiques</label>
                <div class="grid grid-cols-3 gap-4 mt-4">
                  <div class="bg-white/5 rounded-lg p-4">
                    <p class="text-white/60 text-sm">Parties jouées</p>
                    <p class="text-white text-2xl font-bold">0</p>
                  </div>
                  <div class="bg-white/5 rounded-lg p-4">
                    <p class="text-white/60 text-sm">Victoires</p>
                    <p class="text-white text-2xl font-bold">0</p>
                  </div>
                  <div class="bg-white/5 rounded-lg p-4">
                    <p class="text-white/60 text-sm">Taux</p>
                    <p class="text-white text-2xl font-bold">0%</p>
                  </div>
                </div>
              </div>
              
              <!-- Bouton déconnexion -->
              <div class="flex justify-center mb-6">
                <button 
                  @click="handleLogout"
                  class="bg-red-500/80 hover:bg-red-600 text-white font-bold py-3 px-6 rounded-lg transition-all duration-200 flex items-center gap-2"
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
</template>
