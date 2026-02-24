import { ref, onUnmounted } from 'vue'

export function useLobbySocket() {
  const socket = ref<WebSocket | null>(null)
  const isConnected = ref(false)
  const lastMessage = ref<any>(null)
  const error = ref<string | null>(null)

  // Ouvre la connexion WebSocket
  function connect() {
    // Utilise le chemin du backend (adapter si besoin)
    socket.value = new WebSocket('ws://localhost/ws/lobby')

    socket.value.onopen = () => {
      isConnected.value = true
      error.value = null
    }
    socket.value.onclose = () => {
      isConnected.value = false
    }
    socket.value.onerror = (e) => {
      error.value = 'Erreur WebSocket'
    }
    socket.value.onmessage = (event) => {
      try {
        lastMessage.value = JSON.parse(event.data)
      } catch {
        lastMessage.value = event.data
      }
    }
  }

  // Envoie un message JSON
  function send(data: any) {
    if (socket.value && isConnected.value) {
      socket.value.send(JSON.stringify(data))
    }
  }

  // Ferme la connexion
  function disconnect() {
    if (socket.value) {
      socket.value.close()
      socket.value = null
    }
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connect,
    send,
    disconnect,
    isConnected,
    lastMessage,
    error,
    socket
  }
}
