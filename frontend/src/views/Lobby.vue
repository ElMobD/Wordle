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
    <div class="background-gradient"></div>
    <Header 
      title="LOBBY"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />
    <main class="main-content">
      <LobbyChat />
      <div class="glass-panel">
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
              <span class="player-count">({{ nbrPlayers }})</span>
            </h2>
            <div class="players-list">
              <transition-group name="fade" tag="div">
                <div v-for="player in players" :key="player.id" 
                  class="player-card gartic-style" 
                  :class="{ 'me-card': String(player.id) === String(userIdCookie) }">
                  <div class="player-card-content">
                    <div class="player-avatar">
                      <img v-if="player.picture" :src="player.picture" alt="Avatar" />
                      <span v-else class="avatar-placeholder">
                        {{ player.name.charAt(0).toUpperCase() }}
                      </span>
                    </div>
                    <div class="player-name-block">
                      <span class="player-name">{{ player.name }}</span>
                    </div>
                    <div class="player-host-block">
                      <span v-if="player.isHost" class="host-badge" title="Hôte">👑</span>
                    </div>
                  </div>
                  <span class="player-status">Prêt</span>
                </div>
              
              </transition-group>
              <p v-if="players.length === 0" class="placeholder-text">
                En attente de connexion des joueurs...
              </p>
            </div>
          </div>
        </div>
        <div class="lobby-actions">
          <button class="start-btn" v-if="isHost">
            <span class="start-icon">🚀</span> LANCER LA PARTIE
          </button>
          <p v-else class="wait-msg">En attente du lancement par l'hôte...</p>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Fond dégradé immersif */
.background-gradient {
  position: fixed;
  inset: 0;
  z-index: -1;
  background: linear-gradient(120deg, #23243a 0%, #2e7d32 100%);
  opacity: 0.95;
}

.lobby-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

.main-content {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

/* Effet glassmorphism sur le panneau central */
.glass-panel {
  max-width: 600px;
  width: 100%;
  padding: 3rem 2rem;
  background: rgba(30, 40, 60, 0.55);
  border-radius: 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  border: 1.5px solid rgba(255,255,255,0.12);
  backdrop-filter: blur(32px) saturate(1.2);
  -webkit-backdrop-filter: blur(32px) saturate(1.2);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.form-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: #fff;
  margin-bottom: 0.5rem;
  text-align: center;
  letter-spacing: 1px;
}

.form-description {
  font-size: 1.08rem;
  color: #b2dfdb;
  text-align: center;
  margin-bottom: 2rem;
}

.form-content {
  margin-bottom: 2rem;
  width: 100%;
}

.placeholder-text {
  text-align: center;
  color: #b2dfdb;
  font-style: italic;
  margin-top: 1.5rem;
}

.players-section {
  margin-top: 2rem;
}
.players-title {
  font-size: 1.45rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 1.2rem;
  text-align: center;
  letter-spacing: 0.5px;
}
.players-list {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  justify-content: center;
}
/* Style inspiré de Gartic Phone pour les cartes joueurs */
.gartic-style {
  background: rgba(255,255,255,0.13);
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.10);
  border: 2px solid rgba(76,175,80,0.18);
  min-width: 220px;
  max-width: 260px;
  margin: 0.5rem 0.5rem;
  padding: 0.7rem 1.2rem 0.5rem 1.2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: box-shadow 0.18s, transform 0.18s;
}
.gartic-style:hover {
  box-shadow: 0 8px 24px rgba(76,175,80,0.22);
  transform: scale(1.04);
}
.player-card-content {
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  justify-content: space-between;
}
.player-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #23243a 60%, #4caf50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(76,175,80,0.10);
}
.player-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.avatar-placeholder {
  font-size: 1.6rem;
  font-weight: bold;
  color: #b2dfdb;
  text-shadow: 0 2px 8px #23243a;
}
.player-name-block {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.player-name {
  font-size: 1.08rem;
  font-weight: 600;
  color: #fff;
  text-align: center;
}
.player-host-block {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 32px;
}
.host-badge {
  font-size: 1.25rem;
  color: #ffd700;
  margin-left: 0.2rem;
}
.player-status {
  font-size: 0.78rem;
  text-transform: uppercase;
  color: #4caf50;
  font-weight: bold;
  margin-top: 6px;
  letter-spacing: 1px;
  text-align: center;
}

.session-code-copy {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.7rem;
  margin-bottom: 2rem;
}
.copy-btn {
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.38rem 1.1rem;
  font-size: 1.08rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
  box-shadow: 0 2px 8px rgba(76,175,80,0.10);
}
.copy-btn:hover {
  background: #388e3c;
  transform: scale(1.07);
}
.copied-feedback {
  color: #ffd700;
  font-size: 1rem;
  margin-left: 0.5rem;
}

.code-value {
  font-family: 'Courier New', monospace;
  background: rgba(0, 0, 0, 0.32);
  padding: 0.45rem 1.1rem;
  border-radius: 10px;
  color: #00ffcc;
  font-weight: bold;
  letter-spacing: 2px;
  font-size: 1.22rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.10);
}
.code-label {
  color: #b2dfdb;
  font-weight: 700;
  font-size: 1.05rem;
}
.player-count {
  font-size: 1rem;
  color: #b2dfdb;
  vertical-align: middle;
  margin-left: 0.3rem;
}
.player-name-wrapper {
  display: flex;
  align-items: center;
}

/* Boutons et Actions */
.lobby-actions {
  margin-top: 2.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.start-btn {
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
  color: white;
  border: none;
  padding: 1.1rem 3.2rem;
  border-radius: 50px;
  font-weight: 800;
  font-size: 1.15rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 20px rgba(76, 175, 80, 0.18);
  display: flex;
  align-items: center;
  gap: 0.7rem;
}
.start-btn:hover {
  transform: translateY(-3px) scale(1.04);
  box-shadow: 0 15px 25px rgba(76, 175, 80, 0.32);
}
.start-icon {
  font-size: 1.3rem;
}
.wait-msg {
  color: #b2dfdb;
  font-style: italic;
  animation: pulse 2s infinite;
  margin-top: 0.7rem;
}
@keyframes pulse {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

/* Animation d'apparition des joueurs */
.fade-enter-active, .fade-leave-active {
  transition: all 0.35s cubic-bezier(.4,2,.3,1);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
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
/* Badge et style pour le joueur courant */
              .me-badge {
                display: none;
              }
              .me-card {
                border: 2.5px solid #00ffcc !important;
                box-shadow: 0 0 0 4px rgba(0,255,204,0.10);
              }
@keyframes pulse {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}
</style>
