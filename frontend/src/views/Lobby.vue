<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '../components/Header.vue'
import { useLobbySocket } from '../composables/useLobbySocket'
import { watch } from 'vue'

const router = useRouter()
const route = useRoute()
const sessionCode = route.params.sessionCode as string
const { connect, send, isConnected, lastMessage, error, disconnect } = useLobbySocket()
const lobbyInfo = ref<any>(null)

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
  if (!isConnected.value) connect()
  console.log('Connecting to lobby with session code:', sessionCode)
  // Attendre que la connexion soit ouverte avant d’envoyer
  const sendLobbyInfo = () => {
    console.log('Sending LOBBYINFO for session code:', sessionCode)
    send({ type: 'LOBBYINFOS', sessionCode })
  }
  if (isConnected.value) {
    sendLobbyInfo()
  } else {
    const stop = watch(isConnected, (ok) => {
        console.log('WebSocket connection status changed:', ok)
      if (ok) {
        console.log('WebSocket is now connected, sending LOBBYINFO')
        sendLobbyInfo()
        stop()
      }
    })
  }
})

watch(lastMessage, (msg) => {
    console.log('Received WebSocket message:', msg)
    console.log(msg.players)
  if (msg && msg.type === 'lobbyInfo') {
    lobbyInfo.value = msg.data
  }
})
</script>

<template>
  <div class="lobby-wrapper">
    <div class="gradient-overlay"></div>

    <Header 
      title="LOBBY"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />

    <main class="main-content">
      <div class="form-container">
        <h2 class="form-title">Code de la session</h2>
        <p class="form-description">{{ sessionCode }}</p>
        <div class="form-content">
          <div v-if="lobbyInfo">
            <pre>{{ lobbyInfo }}</pre>
          </div>
          <div v-else>
            <p class="placeholder-text">Chargement des informations du lobby...</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.lobby-wrapper {
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
</style>
