<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToSettings = () => {
  router.push('/settings')
}

const showHelp = () => {
  // Ouvre un modal ou affiche l'aide
  console.log('Help clicked')
}
</script>

<template>
  <div class="h-screen w-screen flex flex-col">
    <!-- Header fixe -->
    <header class="h-20 bg-black/20 backdrop-blur-md flex items-center justify-between px-8 border-b border-white/20 flex-shrink-0">
      <h1 class="text-3xl font-black text-white tracking-widest">WORDLE +</h1>
      
      <div class="flex items-center gap-6">
        <!-- Settings icon -->
        <button 
          @click="goToSettings"
          class="text-white hover:bg-white/20 p-2 rounded-lg transition-all duration-200"
          title="Paramètres"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        </button>
        
        <!-- Help icon -->
        <button 
          @click="showHelp"
          class="text-white hover:bg-white/20 p-2 rounded-lg transition-all duration-200"
          title="Aide"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>
      </div>
    </header>

    <!-- Content area -->
    <div class="flex-1 overflow-hidden">
      <!-- Le contenu de la page s'affiche ici -->
      <div class="h-full w-full flex items-center justify-center">
        <button 
          @click="router.push('/daily-word')"
          class="text-5xl font-bold text-white bg-white/20 hover:bg-white/30 px-16 py-12 rounded-3xl border-4 border-white transition-all duration-300"
        >
          Mot du jour
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
