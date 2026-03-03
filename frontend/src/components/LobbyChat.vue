<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useLobbySocket } from '../composables/useLobbySocket'
import { useAuth } from '../composables/useAuth'

const props = defineProps<{ sessionCode: string }>()
const { user } = useAuth()
const { connect, send, lastMessage, isConnected } = useLobbySocket()
const messages = ref<Array<{ user: string; text: string; userId?: number; system?: boolean }>>([])
const newMessage = ref('')
const sessionCode = props.sessionCode


// Récupère l'userId courant depuis le cookie
function getUserIdFromCookie() {
  const match = document.cookie.match(/(?:^|; )userId=([^;]*)/)
  return match ? match[1] : undefined
}
const currentUserId = getUserIdFromCookie()

onMounted(() => {
  if (!isConnected.value) {
    connect(sessionCode)
    const unwatch = watch(isConnected, (ok) => {
      if (ok) {
        send({ type: 'GET_CHAT_HISTORY', sessionCode })
        unwatch()
      }
    })
  } else {
    console.log('[LobbyChat] WebSocket déjà connectée, envoi direct de la requête d\'historique du chat')
    send({ type: 'GET_CHAT_HISTORY', sessionCode })
    console.log('[LobbyChat] Requête d\'historique du chat envoyée:', { type: 'GET_CHAT_HISTORY', sessionCode })
  }
})

function sendMessage() {
  if (!isConnected.value) connect(sessionCode)
  if (newMessage.value.trim() !== '') {
    send({ type: 'CHAT', sessionCode, message: newMessage.value })
    newMessage.value = ''
  }
}

watch(lastMessage, (msg) => {
  console.log('Nouveau message WebSocket dans lobbyChat:', msg)
  if (msg && msg.type === 'chat') {
    messages.value.push({ user: msg.userName || 'Anonyme', text: msg.message, userId: msg.userId })
  } else if (msg && msg.type === 'chat_history' && Array.isArray(msg.messages)) {
    // On remplit l'historique du chat
    messages.value = msg.messages.map((m: { userName?: string; message: string; userId?: number }) => ({
      user: m.userName || 'Anonyme',
      text: m.message,
      userId: m.userId
    }))
  } else if (msg && msg.type === 'player_joined') {
    messages.value.push({
      user: '',
      text: `${msg.userName} a rejoint le lobby.`,
      system: true
    })
  } else if (msg && msg.type === 'player_left') {
    messages.value.push({
      user: '',
      text: `${msg.userName} a quitté le lobby.`,
      system: true
    })
  }
})
</script>

<template>
  <div class="absolute right-8 bottom-8 max-w-sm w-full flex flex-col bg-white/10 border border-white/20 rounded-2xl shadow-2xl backdrop-blur-xl z-50">
    <div class="flex-1 overflow-y-auto p-4 max-h-64 scrollbar-none">
      <div v-for="(msg, idx) in messages" :key="idx" class="mb-2 flex items-start">
        <template v-if="msg.system">
          <span class="italic text-gray-400">{{ msg.text }}</span>
        </template>
        <template v-else>
          <span :class="['font-bold mr-2 whitespace-nowrap', (currentUserId && String(msg.userId) === String(currentUserId)) ? 'text-green-400' : 'text-red-400']">
            {{ msg.user }} :
          </span>
          <span class="text-white break-words">{{ msg.text }}</span>
        </template>
      </div>
    </div>
    <form class="flex gap-2 p-4 border-t border-white/10 bg-white/5" @submit.prevent="sendMessage">
      <input
        v-model="newMessage"
        type="text"
        class="flex-1 px-3 py-2 rounded-lg border border-gray-300 bg-white/20 text-white placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-green-400"
        placeholder="Écrire un message..."
        autocomplete="off"
      />
      <button type="submit" class="bg-gradient-to-tr from-green-500 to-green-800 text-white rounded-lg px-4 py-2 font-semibold shadow hover:scale-105 transition-transform">
        Envoyer
      </button>
    </form>
  </div>
</template>

<style scoped>
.scrollbar-none::-webkit-scrollbar {
  display: none;
}
.scrollbar-none {
  scrollbar-width: none;
}
</style>
