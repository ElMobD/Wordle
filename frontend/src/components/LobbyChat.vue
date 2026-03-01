<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useLobbySocket } from '../composables/useLobbySocket'
import { useRoute } from 'vue-router'
const route = useRoute()

const { connect, send, lastMessage, isConnected } = useLobbySocket()
const messages = ref<Array<{ user: string; text: string }>>([])
const newMessage = ref('')
const sessionCode = route.params.sessionCode as string

onMounted(() => {
  if (!isConnected.value) connect(sessionCode)
})

function sendMessage() {
  if (!isConnected.value) connect(sessionCode)
  if (newMessage.value.trim() !== '') {
    send({ type: 'CHAT', sessionCode, message: newMessage.value })
    newMessage.value = ''
  }
}

watch(lastMessage, (msg) => {
    console.log('Nouveau message WebSocket:', msg)
  if (msg && msg.type === 'CHAT') {
    messages.value.push({ user: msg.user || 'Anonyme', text: msg.text })
  }
})
</script>

<template>
  <div class="lobby-chat">
    <div class="chat-messages">
      <div v-for="(msg, idx) in messages" :key="idx" class="chat-message">
        <span class="chat-user">{{ msg.user }} :</span>
        <span class="chat-text">{{ msg.text }}</span>
      </div>
    </div>
    <form class="chat-input-wrapper" @submit.prevent="sendMessage">
      <input
        v-model="newMessage"
        type="text"
        class="chat-input"
        placeholder="Écrire un message..."
        autocomplete="off"
      />
      <button type="submit" class="chat-send-btn">Envoyer</button>
    </form>
  </div>
</template>

<style scoped>
 .lobby-chat {
   position: absolute;
   right: 2rem;
   bottom: 2rem;
   background: rgba(255,255,255,0.08);
   border-radius: 16px;
   padding: 1rem;
   max-width: 350px;
   width: 100%;
   display: flex;
   flex-direction: column;
   box-shadow: 0 4px 16px rgba(0,0,0,0.08);
   z-index: 100;
 }
.chat-messages {
  flex: 1;
  margin-bottom: 1rem;
  max-height: 200px;
  overflow-y: scroll;
  scrollbar-width: none; /* Firefox */
}
.chat-messages::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}
.chat-message {
  margin-bottom: 0.5rem;
  word-break: break-word;
}
.chat-user {
  font-weight: bold;
  color: #4caf50;
  margin-right: 0.5rem;
}
.chat-text {
  color: #fff;
}
.chat-input-wrapper {
  display: flex;
  gap: 0.5rem;
}
.chat-input {
  flex: 1;
  padding: 0.5rem;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-size: 1rem;
}
.chat-send-btn {
  background: #4caf50;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.chat-send-btn:hover {
  background: #388e3c;
}
</style>
