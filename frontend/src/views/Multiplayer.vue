<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import Header from '../components/Header.vue'
import { useLobbySocket } from '../composables/useLobbySocket'
import { watch } from 'vue'
import { authenticatedFetch } from '../utils/api'


// ...tes refs existantes...
const { connect, send, isConnected, lastMessage} = useLobbySocket()
const sessionCode = ref<string | null>(null)
const creationError = ref<string | null>(null)

import { onMounted } from 'vue'

onMounted(async () => {
  try {
    const res = await authenticatedFetch('http://localhost/api/session/current')
    if (res.ok) {
      const data = await res.json()
      if (data.sessionCode) {
        router.push(`/lobby/${data.sessionCode}`)
      }
    }
  } catch (e) {
    console.error('Erreur lors de la vérification de session:', e)
  }
})

const router = useRouter()
const mode = ref<'menu' | 'create' | 'join'>('menu')
const joinCode = ref('')

const showCreateForm = () => {
  mode.value = 'create'
}

const showJoinForm = () => {
  mode.value = 'join'
}

const backToMenu = () => {
  mode.value = 'menu'
  joinCode.value = ''
}

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

const rounds = ref(5)
const timeLimit = ref(60)
const wordLength = ref(5)

const isFormValid = computed(() => {
  return (
    rounds.value >= 1 && rounds.value <= 20 &&
    timeLimit.value >= 10 && timeLimit.value <= 300 &&
    wordLength.value >= 4 && wordLength.value <= 8
  )
})

// Surveille la réponse WebSocket
watch(lastMessage, (msg) => {
    console.log('WebSocket message reçu dans Vue:', msg)
    if (!msg) return
    if (msg.type === 'session_created') {
      sessionCode.value = msg.sessionCode
      // Redirige vers la vue lobby
      router.push(`/lobby/${msg.sessionCode}`)
    } else if (msg.type === 'error') {
      creationError.value = msg.message
      if (msg.sessionCode) {
        router.push(`/lobby/${msg.sessionCode}`)
      }
    }
})

const createSession = () => {
  if (!isFormValid.value) return
  // TODO: implémenter création de session réelle (WebSocket)
  console.log('Créer une session', { rounds: rounds.value, timeLimit: timeLimit.value, wordLength: wordLength.value })
  creationError.value = null
  sessionCode.value = null
  if (!isConnected.value) connect()
  // Attendre que la connexion soit ouverte avant d’envoyer
  const sendCreate = () => {
    send({
      type: 'CREATE',
      rounds: rounds.value,
      timeLimit: timeLimit.value,
      wordLength: wordLength.value
    })
  }
  if (isConnected.value) {
    sendCreate()
    console.log('Message de création envoyé')
  } else {
    // On attend l'ouverture effective
    console.log('En attente de la connexion WebSocket pour envoyer la création...')
    const stop = watch(isConnected, (ok) => {
      if (ok) {
        console.log('Connexion WebSocket établie, envoi du message de création...')
        sendCreate()
        stop()
      }
    })
  }
}

const joinSession = () => {
  if (joinCode.value.trim()) {
    // TODO: implémenter rejoindre session
    console.log('Rejoindre session:', joinCode.value)
  }
}
</script>

<template>
      <div class="relative flex flex-col min-h-screen w-full">
        <div class="fixed inset-0 bg-white/5 pointer-events-none z-0"></div>
        <Header 
          title="MULTIJOUEUR"
          @home="goHome"
          @settings="goToSettings"
          @help="showHelp"
          @contact="goToContact"
          @profile="goToProfile"
        />
        <main class="relative flex-1 flex items-center justify-center p-8">
          <div class="flex flex-col gap-8 items-center w-full max-w-md">
            <!-- Menu principal -->
            <div v-if="mode === 'menu'" class="flex flex-col gap-6 w-full">
              <button @click="showCreateForm" class="rounded-xl px-8 py-5 text-xl font-bold bg-white/10 text-white border border-white/20 hover:bg-white/20 transition">Créer une partie</button>
              <button @click="showJoinForm" class="rounded-xl px-8 py-5 text-xl font-bold bg-white/5 text-white/80 border border-white/20 hover:bg-white/10 transition">Rejoindre une partie</button>
            </div>
            <!-- Formulaire créer -->
            <div v-else-if="mode === 'create'" class="w-full bg-white/5 rounded-xl p-6 flex flex-col gap-4 border border-white/10">
              <h2 class="text-2xl font-bold text-white mb-2">Créer une partie</h2>
              <p class="text-white/80 mb-4">Configuration de la session multijoueur</p>
              <div class="flex flex-col gap-4">
                <div class="flex flex-col gap-1">
                  <label for="rounds" class="text-white font-medium">Nombre de manches</label>
                  <input id="rounds" type="number" v-model.number="rounds" min="1" max="20" class="rounded-lg px-4 py-2 bg-white/10 border border-white/20 text-white focus:border-white outline-none" />
                </div>
                <div class="flex flex-col gap-1">
                  <label for="timeLimit" class="text-white font-medium">Temps par manche (secondes)</label>
                  <input id="timeLimit" type="number" v-model.number="timeLimit" min="10" max="300" class="rounded-lg px-4 py-2 bg-white/10 border border-white/20 text-white focus:border-white outline-none" />
                </div>
                <div class="flex flex-col gap-1">
                  <label for="wordLength" class="text-white font-medium">Longueur du mot</label>
                  <input id="wordLength" type="number" v-model.number="wordLength" min="4" max="8" class="rounded-lg px-4 py-2 bg-white/10 border border-white/20 text-white focus:border-white outline-none" />
                </div>
              </div>
              <div class="flex flex-row gap-4 justify-end mt-4">
                <button @click="backToMenu" class="rounded-lg px-6 py-2 bg-white/10 text-white/80 border border-white/20 hover:bg-white/20 transition">Retour</button>
                <button @click="createSession" class="rounded-lg px-6 py-2 bg-indigo-600 text-white font-semibold border border-indigo-600 hover:bg-indigo-700 transition" :disabled="!isFormValid">Créer</button>
              </div>
            </div>
            <!-- Formulaire rejoindre -->
            <div v-else-if="mode === 'join'" class="w-full bg-white/5 rounded-xl p-6 flex flex-col gap-4 border border-white/10">
              <h2 class="text-2xl font-bold text-white mb-2">Rejoindre une partie</h2>
              <p class="text-white/80 mb-4">Entrez le code de la session</p>
              <input 
                v-model="joinCode"
                type="text"
                placeholder="Code de la partie (ex: ABC123)"
                class="rounded-lg px-4 py-2 bg-white/10 border border-white/20 text-white focus:border-white outline-none text-center text-lg tracking-widest"
                maxlength="10"
                @keyup.enter="joinSession"
              />
              <div class="flex flex-row gap-4 justify-end mt-4">
                <button @click="backToMenu" class="rounded-lg px-6 py-2 bg-white/10 text-white/80 border border-white/20 hover:bg-white/20 transition">Retour</button>
                <button @click="joinSession" class="rounded-lg px-6 py-2 bg-indigo-600 text-white font-semibold border border-indigo-600 hover:bg-indigo-700 transition" :disabled="!joinCode.trim()">Rejoindre</button>
              </div>
            </div>
          </div>
        </main>
      </div>
</template>

