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
  console.log('User ID depuis cookie:', userIdCookie.value)
  if (!isConnected.value) connect()
  const sendLobbyInfo = () => {
    send({ type: 'LOBBYINFOS', sessionCode })
  }
  if (isConnected.value) {
    sendLobbyInfo()
  } else {
    const stop = watch(isConnected, (ok) => {
      if (ok) {
        console.log('WebSocket is now connected, sending LOBBYINFO')
        sendLobbyInfo()
        stop()
      }
    })
  }
})

watch(lastMessage, (msg) => {
    if (msg && msg.type === 'lobbyInfo') {
      lobbyInfo.value = msg
      players.value = msg.players
      nbrPlayers.value = players.value.length
      console.log('lobbyInfo host ID:', lobbyInfo.value.hostId)
      console.log('Current user ID from cookie:', userIdCookie.value)
      isHost.value = String(lobbyInfo.value.hostId) === String(userIdCookie.value)
      console.log('Is current user the host?', isHost.value)
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
        <header class="lobby-header">
          <h1 class="form-title">Lobby de la Session</h1>
          <p class="form-description">En attente des autres joueurs pour débuter la partie.</p>
          
          <div class="session-code-copy">
            <span class="code-label">CODE :</span>
            <code class="code-value">{{ sessionCode }}</code>
            <button class="copy-btn" @click="copySessionCode">
              {{ copied ? 'Copié !' : 'Copier' }}
            </button>
          </div>
        </header>

        <div class="form-content">
          <div class="players-section">
            <h2 class="players-title">
              Joueurs connectés 
              <span class="player-count">({{ nbrPlayers ? nbrPlayers : "caca" }})</span>
            </h2>
            
            <div class="players-list">
              <div v-for="player in players" :key="player.id" class="player-card">
                <div class="player-avatar">
                  <img v-if="player.picture" :src="player.picture" alt="Avatar" />
                  <span v-else class="avatar-placeholder">
                    {{ player.name}}
                  </span>
                </div>
                <div class="player-info">
                  <div class="player-name-wrapper">
                    <span class="player-name">{{ player.name }}</span>
                    <span v-if="player.isHost" class="host-badge" title="Hôte">👑</span>
                  </div>
                  <span class="player-status">Prêt</span>
                </div>
              </div>

              <p v-if="players.length === 0" class="placeholder-text">
                En attente de connexion des joueurs...
              </p>
            </div>
          </div>
        </div>

        <div class="lobby-actions">
           <button class="start-btn" v-if="isHost">
             LANCER LA PARTIE
           </button>
           <p v-else class="wait-msg">En attente du lancement par l'hôte...</p>
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

.players-section {
        margin-top: 2rem;
      }
      .players-title {
        font-size: 1.5rem;
        font-weight: 600;
        color: #fff;
        margin-bottom: 1rem;
        text-align: center;
      }
      .players-list {
        display: flex;
        flex-wrap: wrap;
        gap: 1.5rem;
        justify-content: center;
      }
      .player-card {
        display: flex;
        align-items: center;
        background: rgba(255,255,255,0.08);
        border-radius: 16px;
        padding: 1rem 1.5rem;
        box-shadow: 0 4px 16px rgba(0,0,0,0.08);
        min-width: 180px;
        max-width: 240px;
        transition: transform 0.2s;
      }
      .player-card:hover {
        transform: scale(1.04);
      }
      .player-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 1rem;
        overflow: hidden;
      }
      .player-avatar img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
      }
      .avatar-placeholder {
        font-size: 1.8rem;
        font-weight: bold;
        color: #888;
      }
      .player-info {
        display: flex;
        flex-direction: column;
      }
      .player-name {
        font-size: 1.1rem;
        font-weight: 500;
        color: #fff;
      }
      .host-badge {
        font-size: 1rem;
        color: #ffd700;
        margin-left: 0.5rem;
      }
      .player-date {
        font-size: 0.95rem;
        margin-top: 0.2rem;
        color: #a0e7a0;
      }
              .session-code-copy {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          margin-bottom: 2rem;
        }
        .copy-btn {
          background: #4caf50;
          color: #fff;
          border: none;
          border-radius: 6px;
          padding: 0.3rem 0.8rem;
          font-size: 1rem;
          cursor: pointer;
          transition: background 0.2s;
        }
        .copy-btn:hover {
          background: #388e3c;
        }
        .copied-feedback {
          color: #ffd700;
          font-size: 1rem;
          margin-left: 0.5rem;
        }

/* Ajustements de la section code */
.code-value {
  font-family: 'Courier New', monospace;
  background: rgba(0, 0, 0, 0.3);
  padding: 0.4rem 1rem;
  border-radius: 8px;
  color: #00ffcc;
  font-weight: bold;
  letter-spacing: 2px;
  font-size: 1.2rem;
}

.code-label {
  color: rgba(255, 255, 255, 0.5);
  font-weight: 600;
}

/* Badge de joueur */
.player-count {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.5);
  vertical-align: middle;
}

.player-name-wrapper {
  display: flex;
  align-items: center;
}

.player-status {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: #4caf50;
  font-weight: bold;
  margin-top: 2px;
}

/* Boutons et Actions */
.lobby-actions {
  margin-top: 3rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.start-btn {
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
  color: white;
  border: none;
  padding: 1rem 3rem;
  border-radius: 50px;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 25px rgba(76, 175, 80, 0.4);
}

.wait-msg {
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}
</style>
