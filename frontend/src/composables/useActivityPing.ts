import { ref, onMounted, onUnmounted, type Ref } from 'vue'
/**
 * Composable pour gérer l'activité utilisateur, envoyer un ping WebSocket si actif,
 * et tenter de se reconnecter automatiquement si déconnecté.
 * @param sendPing Fonction à appeler pour envoyer le ping (ex: send({type: 'PING'}))
 * @param isConnected Fonction ou ref qui retourne true si le websocket est connecté
 * @param connect Fonction à appeler pour tenter une reconnexion
 * @param intervalMs Intervalle de vérification (par défaut 30s)
 * @param timeoutMs Délai d'inactivité max avant de ne plus pinger (par défaut 30s)
 */

export function useActivityPing(
  sendPing: () => void,
  isConnected: Ref<boolean> | (() => boolean),
  connect: (sessionCode?: string) => void,
  sessionCode?: string,
  intervalMs = 30_000,
  timeoutMs = 30_000
) {
  const lastActivity = ref(Date.now())
  let pingInterval: number | undefined
  let lastReconnectAttempt = 0
  const RECONNECT_COOLDOWN = 5_000 // 5s min entre deux tentatives

  function getIsConnected() {
    return typeof isConnected === 'function' ? isConnected() : isConnected.value
  }

  function updateActivity() {
    lastActivity.value = Date.now()
    // Si websocket déconnecté, tenter de se reconnecter (anti-spam 5s)
    if (!getIsConnected() && Date.now() - lastReconnectAttempt > RECONNECT_COOLDOWN) {
      lastReconnectAttempt = Date.now()
      connect(sessionCode)
    }
  }

  onMounted(() => {
    window.addEventListener('mousemove', updateActivity)
    window.addEventListener('keydown', updateActivity)
    window.addEventListener('mousedown', updateActivity)
    window.addEventListener('touchstart', updateActivity)
    pingInterval = window.setInterval(() => {
      if (Date.now() - lastActivity.value < timeoutMs && getIsConnected()) {
        sendPing()
      }
    }, intervalMs)
  })

  onUnmounted(() => {
    window.removeEventListener('mousemove', updateActivity)
    window.removeEventListener('keydown', updateActivity)
    window.removeEventListener('mousedown', updateActivity)
    window.removeEventListener('touchstart', updateActivity)
    if (pingInterval) clearInterval(pingInterval)
  })

  return { lastActivity }
}
