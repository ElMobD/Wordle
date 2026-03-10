<script setup lang="ts">
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'
import { ref } from 'vue'
import Header from '../components/Header.vue'
import Modal from '../components/Modal.vue'
import { useAuth } from '../composables/useAuth'
import LoadingScreen from '../components/LoadingScreen.vue'

const HOMEPAGE_LOADING_KEY = 'homepage_loading_seen'
const isRouteLoading = ref(sessionStorage.getItem(HOMEPAGE_LOADING_KEY) !== '1')
const MIN_LOADING_MS = 3000

const { checkAuth } = useAuth()
const router = useRouter()
const isHelpModalOpen = ref(false)

const goToSettings = () => {
  router.push('/settings')
}

const showHelp = () => {
  isHelpModalOpen.value = true
}

const closeHelpModal = () => {
  isHelpModalOpen.value = false
}

const goHome = () => {
  router.push('/homepage')
}

const goToContact = () => {
  router.push('/contact')
}

const goToProfile = () => {
  router.push('/settings?tab=profil')
}
onMounted(async () => {
    checkAuth();

    if (isRouteLoading.value) {
      await new Promise((resolve) => setTimeout(resolve, MIN_LOADING_MS))
      isRouteLoading.value = false
      sessionStorage.setItem(HOMEPAGE_LOADING_KEY, '1')
    }
})
</script>

<template>
  <div class="relative flex flex-col min-h-[100dvh] w-full overflow-y-auto">
    <!-- Couche d'atténuation du gradient -->
    <div class="fixed inset-0 bg-white/5 pointer-events-none"></div>

    <!-- Header -->
    <Header 
      title="WORDLE+"
      @home="goHome"
      @settings="goToSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />

    <!-- Contenu principal -->
    <main class="relative flex-1 flex items-center justify-center px-4 py-6 sm:px-6 lg:px-8 xl:px-12">
      <div class="flex flex-col gap-4 sm:gap-6 items-center w-full max-w-[760px]">
        <button 
          @click="router.push('/daily-word')"
          class="w-full px-8 sm:px-12 lg:px-16 py-7 sm:py-9 lg:py-11 bg-white/15 backdrop-blur-2xl border border-white/25 rounded-[24px] sm:rounded-[28px] text-white text-3xl sm:text-4xl xl:text-5xl font-bold cursor-pointer transition-all duration-200 shadow-[0_20px_60px_rgba(0,0,0,0.15),_inset_0_1px_0_rgba(255,255,255,0.3)] hover:bg-white/25 hover:scale-[1.02] active:scale-95"
        >
          Mot du jour
        </button>
        <button 
          @click="router.push('/multiplayer')"
          class="w-full px-7 sm:px-10 lg:px-12 py-6 sm:py-8 lg:py-10 bg-white/10 backdrop-blur-2xl border border-white/25 rounded-[24px] sm:rounded-[28px] text-white text-2xl sm:text-3xl xl:text-4xl font-bold cursor-pointer transition-all duration-200 shadow-[0_20px_60px_rgba(0,0,0,0.15),_inset_0_1px_0_rgba(255,255,255,0.3)] hover:bg-white/20 hover:scale-[1.02] active:scale-95"
        >
          Multijoueur
        </button>
      </div>
    </main>

    <!-- Modal d'aide -->
    <Modal 
      :is-open="isHelpModalOpen"
      title="Comment jouer"
      @close="closeHelpModal"
    >
      <div class="flex flex-col gap-4">
        <h3 class="mt-2 mb-2 text-lg text-white font-semibold">Règles du jeu</h3>
        <p class="mb-2 opacity-95">Devinez le mot en 6 essais maximum !</p>
        
        <h3 class="mt-2 mb-2 text-lg text-white font-semibold">Comment jouer</h3>
        <ul class="mb-2 pl-6 opacity-95 list-disc">
          <li class="mb-1">Tapez un mot de 5 lettres</li>
          <li class="mb-1">Appuyez sur ENTRÉE pour valider</li>
          <li class="mb-1">Les couleurs changent pour vous indiquer si les lettres sont correctes :</li>
        </ul>

        <div class="flex flex-col gap-3 mt-4 p-4 bg-white/10 rounded-lg">
          <div class="p-3 rounded text-center font-medium bg-green-500/20 text-green-300">🟩 La lettre est au bon endroit</div>
          <div class="p-3 rounded text-center font-medium bg-yellow-500/20 text-yellow-200">🟨 La lettre existe mais au mauvais endroit</div>
          <div class="p-3 rounded text-center font-medium bg-gray-500/20 text-gray-300">⬜ La lettre n'existe pas dans le mot</div>
        </div>
      </div>
    </Modal>
  </div>
  <LoadingScreen v-if="isRouteLoading" message="Chargement de l'application..." />
</template>
