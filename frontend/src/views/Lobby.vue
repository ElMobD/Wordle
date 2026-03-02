<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '../components/Header.vue'
import LobbyChat from '../components/LobbyChat.vue'
import { useLobbySocket } from '../composables/useLobbySocket'
import { watch } from 'vue'

const router = useRouter()
const route = useRoute()
const sessionCode = route.params.sessionCode as string
const { connect, send, isConnected, lastMessage} = useLobbySocket()
const lobbyInfo = ref<any>(null)
const players = ref<any[]>([])
const nbrPlayers = ref(0)
const isHost = ref(false)
const userIdCookie = ref<string | null>(null)

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

onMounted(() => {
  userIdCookie.value = document.cookie.split('; ').find(row => row.startsWith('userId='))?.split('=')[1] || null
  if (!isConnected.value) connect(sessionCode)
  const sendLobbyInfo = () => {
    send({ type: 'LOBBYINFOS', sessionCode })
  }
  if (isConnected.value) {
    sendLobbyInfo()
  } else {
    const stop = watch(isConnected, (ok) => {
      if (ok) {
        sendLobbyInfo()
        stop()
      }
    })
  }
})

watch(lastMessage, (msg) => {
    if (msg && msg.type === 'lobbyInfo') {
      lobbyInfo.value = msg
      // Trie pour mettre l'hôte en premier
      const sortedPlayers = [...msg.players].sort((a, b) => {
        if (a.isHost) return -1
        if (b.isHost) return 1
        return 0
      })
      players.value = sortedPlayers
      nbrPlayers.value = players.value.length

      isHost.value = String(lobbyInfo.value.hostId) === String(userIdCookie.value)
    }else if (msg && msg.type === 'player_left') {
      // Retirer le joueur de la liste
      players.value = players.value.filter(p => String(p.id) !== String(msg.userId))
      nbrPlayers.value = players.value.length
      router.push('/multiplayer') // Rediriger vers l'accueil si un joueur quitte le lobby
    }
})
// Copie du code de session
const copied = ref(false)
function copySessionCode() {
  navigator.clipboard.writeText(sessionCode)
    .then(() => {
      copied.value = true
      setTimeout(() => copied.value = false, 1500)
    })
}
const quitLobby = () => {
  send({ type: 'LEAVE_LOBBY', sessionCode })
}
</script>


<template>
  <div class="relative flex flex-col min-h-screen w-full">
    <Header 
      title="LOBBY"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />
    <main class="flex flex-1 items-center justify-center p-4 md:p-8">
      <LobbyChat />
      <div class="w-full max-w-xl bg-white/10 border border-white/20 rounded-2xl shadow-2xl backdrop-blur-xl flex flex-col items-center px-4 py-8 md:px-8 md:py-12">
        <header class="w-full flex flex-col items-center mb-6">
          <h1 class="text-3xl font-extrabold text-white mb-1 tracking-wide text-center">Lobby de la Session</h1>
          <p class="text-teal-200 text-base text-center mb-6">En attente des autres joueurs pour débuter la partie.</p>
          <div class="flex items-center justify-center gap-2 mb-6">
            <span class="text-teal-200 font-bold text-base">CODE :</span>
            <code class="font-mono bg-black/30 px-4 py-2 rounded-lg text-cyan-300 font-bold tracking-widest text-lg shadow-md">{{ sessionCode }}</code>
            <button @click="copySessionCode" class="bg-gradient-to-tr from-green-500 to-green-800 text-white rounded-md px-3 py-1.5 font-semibold text-base shadow hover:scale-105 transition-transform">
              {{ copied ? 'Copié !' : 'Copier' }}
            </button>
          </div>
        </header>
        <div class="w-full mb-6">
          <div class="mt-4">
            <h2 class="text-xl font-bold text-white mb-4 text-center">
              Joueurs connectés
              <span class="text-teal-200 text-base align-middle ml-1">({{ nbrPlayers }})</span>
            </h2>
            <div class="flex flex-wrap gap-4 justify-center">
              <transition-group name="fade" tag="div" class="flex flex-wrap gap-4 justify-center w-full">
                <div v-for="player in players" :key="player.id"
                  :class="[
                    'flex flex-col items-center min-w-[180px] max-w-[240px] p-4 rounded-xl shadow-lg border transition-transform duration-200',
                    'bg-white/20 border-green-300/20 hover:scale-105',
                    String(player.id) === String(userIdCookie) ? 'border-cyan-400 ring-2 ring-cyan-300/30' : 'border-green-300/20'
                  ]">
                  <div class="flex flex-row items-center w-full justify-between">
                    <div class="w-12 h-12 rounded-full bg-gradient-to-tr from-gray-800 to-green-600 flex items-center justify-center overflow-hidden shadow">
                      <img v-if="player.picture" :src="player.picture" alt="Avatar" class="w-full h-full object-cover rounded-full" />
                      <span v-else class="text-xl font-bold text-teal-200 drop-shadow">
                        {{ player.name.charAt(0).toUpperCase() }}
                      </span>
                    </div>
                    <div class="flex-1 flex items-center justify-center">
                      <span class="text-base font-semibold text-white text-center">{{ player.name }}</span>
                    </div>
                    <div class="flex items-center justify-end min-w-8">
                      <span v-if="player.isHost" class="text-xl ml-1" title="Hôte">👑</span>
                    </div>
                  </div>
                  <span class="text-xs uppercase text-green-400 font-bold mt-1 tracking-wider">Prêt</span>
                </div>
              </transition-group>
              <p v-if="players.length === 0" class="text-center text-teal-200 italic mt-4">
                En attente de connexion des joueurs...
              </p>
            </div>
          </div>
        </div>
        <div class="mt-8 flex flex-col items-center">
          <button v-if="isHost" class="bg-gradient-to-tr from-green-500 to-green-800 text-white font-extrabold rounded-full px-10 py-3 text-lg shadow-lg flex items-center gap-2 hover:scale-105 transition-transform">
            <span class="text-xl">🚀</span> LANCER LA PARTIE
          </button>
          <p v-else class="text-teal-200 italic animate-pulse mt-2">En attente du lancement par l'hôte...</p>
          <div class="flex justify-center mt-8">
          <button @click="quitLobby" class="bg-red-600 hover:bg-red-700 text-white font-bold rounded-full px-8 py-3 shadow-lg transition-all">
            Quitter le lobby
          </button>
      </div>
        </div>
      </div>
    </main>
  </div>
</template>