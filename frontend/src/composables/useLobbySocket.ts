import { ref, onUnmounted } from 'vue'

function createLobbySocket() {
  const socket = ref<WebSocket | null>(null)
  const isConnected = ref(false)
  const firstMessageReceived = ref<any>(null)
  const lastMessage = ref<any>(null)
  const error = ref<string | null>(null)
  var dataToSend: any = null

  // Ouvre la connexion WebSocket
  function connect(code ?: string) {
    socket.value = new WebSocket('ws://localhost/ws/lobby')
    socket.value.onopen = () => {
        console.log('WebSocket connecté')
        isConnected.value = true
        error.value = null
        if(code){
          dataToSend = { type: 'JOIN', sessionCode: code }
          console.log('Envoi du message JOIN:', dataToSend)
          send(dataToSend)
        }
    }
    socket.value.onclose = () => {
        console.log('WebSocket déconnecté')
        isConnected.value = false
    }
    socket.value.onerror = (e) => {
        console.error('WebSocket error:', e)
        error.value = 'Erreur WebSocket'
    }
    socket.value.onmessage = (event) => {
      try {
        const parsed = JSON.parse(event.data)
        if (!firstMessageReceived.value) {
          firstMessageReceived.value = parsed
        }
        lastMessage.value = parsed
        if(parsed.userId) {
          //console.log('User ID reçu:', parsed.userId)
          //document.cookie = `userId=${parsed.userId}; path=/` // Stocke le userId dans les cookies
        }
      } catch (e) {
        lastMessage.value = event.data
      }
    }
  }

  // Envoie un message JSON
  function send(data: any) {
    if (
      socket.value &&
      socket.value.readyState === WebSocket.OPEN
    ) {
      socket.value.send(JSON.stringify(data))
    } else {
      console.warn('WebSocket non prêt, message ignoré', data)
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

let socketInstance: ReturnType<typeof createLobbySocket> | null = null
export function useLobbySocket() {
  if (!socketInstance) {
    socketInstance = createLobbySocket()
  }
  return socketInstance
}
