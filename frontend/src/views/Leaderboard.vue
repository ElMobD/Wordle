<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '../components/Header.vue'

import { authenticatedFetch } from '../utils/api'

const router = useRouter()
const route = useRoute()
const sessionCode = route.params.sessionCode as string


interface PlayerScore {
  userId: number
  username: string
  email: string
  totalScore: number
  wins: number
  losses: number
  averageAttempts: number
}

const loading = ref(true)
const leaderboard = ref<PlayerScore[]>([])
const error = ref('')

onMounted(async () => {
  // La connexion WebSocket est déjà établie depuis Lobby.vue
  // Pas besoin de reconnecter
  await fetchLeaderboard()
})

const fetchLeaderboard = async () => {
  try {
    console.log(`Fetching leaderboard for session: ${sessionCode}`)
    const res = await authenticatedFetch(`http://localhost/api/session/${sessionCode}/leaderboard`)
    console.log('Response status:', res.status, 'OK:', res.ok)
    if (res.ok) {
      const data = await res.json()
      console.log('Leaderboard data:', data)
      leaderboard.value = data.sort((a: PlayerScore, b: PlayerScore) => b.totalScore - a.totalScore)
      console.log('Leaderboard after assignment:', leaderboard.value)
      console.log('Loading:', loading.value, 'Error:', error.value)
    } else {
      const text = await res.text()
      console.error('Response error:', text)
      error.value = `Erreur ${res.status}: Impossible de charger le classement`
    }
  } catch (e) {
    console.error('Erreur lors du chargement du leaderboard:', e)
    error.value = `Erreur de connexion: ${e instanceof Error ? e.message : 'Erreur inconnue'}`
  } finally {
    loading.value = false
    console.log('Loading set to false. Loading:', loading.value, 'Leaderboard length:', leaderboard.value.length)
  }
}

const goHome = () => router.push('/homepage')
const goToSettings = () => router.push('/settings')
const goToContact = () => router.push('/contact')
const goToProfile = () => router.push('/settings?tab=profil')
const backToLobby = () => router.push(`/lobby/${sessionCode}`)

const getRankEmoji = (index: number) => {
  if (index === 0) return '🥇'
  if (index === 1) return '🥈'
  if (index === 2) return '🥉'
  return `${index + 1}`
}
</script>

<template>
  <div class="relative flex flex-col min-h-screen w-full">
    <div class="fixed inset-0 bg-white/5 pointer-events-none z-0"></div>
    <Header 
      title="CLASSEMENT"
      @home="goHome"
      @settings="goToSettings"
      @contact="goToContact"
      @profile="goToProfile"
    />
    
    <main class="relative flex-1 flex items-center justify-center p-4 md:p-8">
      <div class="w-full max-w-2xl bg-white/10 border border-white/20 rounded-2xl shadow-2xl backdrop-blur-xl flex flex-col items-center px-4 py-8 md:px-8 md:py-12">
        <header class="w-full flex flex-col items-center mb-8">
          <h1 class="text-4xl font-extrabold text-white mb-2 tracking-wide text-center">🏆 Classement Final</h1>
          <p class="text-teal-200 text-base text-center">Session terminée - Résultats de la partie</p>
        </header>

        <div v-if="loading" class="text-white text-xl">Chargement du classement...</div>
        <div v-else-if="error" class="text-red-400 text-xl">{{ error }}</div>
        <div v-else class="w-full space-y-3">
          <div 
            v-for="(player, index) in leaderboard" 
            :key="player.userId"
            class="flex items-center gap-4 p-4 rounded-xl border transition-all hover:scale-[1.02]"
            :class="[
              index === 0 ? 'bg-gradient-to-r from-yellow-500/20 to-yellow-600/20 border-yellow-400/40' :
              index === 1 ? 'bg-gradient-to-r from-gray-400/20 to-gray-500/20 border-gray-400/40' :
              index === 2 ? 'bg-gradient-to-r from-orange-500/20 to-orange-600/20 border-orange-400/40' :
              'bg-white/5 border-white/20'
            ]"
          >
            <!-- Rang -->
            <div class="text-3xl font-bold min-w-[3rem] text-center">
              {{ getRankEmoji(index) }}
            </div>

            <!-- Avatar + Nom -->
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <div class="w-12 h-12 rounded-full bg-gradient-to-tr from-gray-800 to-green-600 flex items-center justify-center overflow-hidden shadow">
                <span class="text-xl font-bold text-teal-200 drop-shadow">
                  {{ player.username.charAt(0).toUpperCase() }}
                </span>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-white font-bold text-lg truncate">{{ player.username }}</span>
                <div class="flex gap-3 text-xs text-white/70">
                  <span>✅ {{ player.wins }} victoires</span>
                  <span>❌ {{ player.losses }} défaites</span>
                </div>
              </div>
            </div>

            <!-- Score -->
            <div class="flex flex-col items-end">
              <span class="text-2xl font-extrabold text-white">{{ player.totalScore }}</span>
              <span class="text-xs text-white/70">points</span>
            </div>
          </div>
        </div>

        <div class="mt-8 flex gap-4">
          <button 
            @click="goHome" 
            class="bg-gradient-to-tr from-green-500 to-green-800 text-white font-bold rounded-full px-8 py-3 shadow-lg hover:scale-105 transition-transform"
          >
            Accueil
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
