<script setup lang="ts">
import { useAuth } from '../composables/useAuth'
import { computed, onMounted } from 'vue'

interface Props {
  title: string
  showSettings?: boolean
}

withDefaults(defineProps<Props>(), {
  showSettings: true
})

defineEmits<{
  home: []
  settings: []
  help: []
  contact: []
  profile: []
}>()

const { user, checkAuth } = useAuth()
const userPicture = computed(() => user.value?.picture)
const userName = computed(() => user.value?.name || '?')

onMounted(() => {
  checkAuth()
})
</script>

<template>
  <header class="glass-header">
    <div class="header-left">
      <button 
        @click="$emit('home')"
        class="icon-button back-button"
        title="Accueil"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-3m0 0l7-4 7 4M5 9v10a1 1 0 001 1h2a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1h2a1 1 0 001-1V9m-9 0l7-4" />
        </svg>
      </button>
    </div>
    
    <h1 class="header-title">{{ title }}</h1>
    
    <div class="header-right">
      <button 
        v-if="showSettings"
        @click="$emit('settings')"
        class="icon-button"
        title="Paramètres"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
      </button>
      
      <button 
        @click="$emit('help')"
        class="icon-button"
        title="Aide"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>
      
      <button 
        @click="$emit('contact')"
        class="icon-button"
        title="Contact"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      </button>

      <button 
        @click="$emit('profile')"
        class="profile-button"
        title="Mon profil"
      >
        <img v-if="userPicture" :src="userPicture" :alt="userName" class="profile-image" />
        <div v-else class="profile-placeholder">{{ userName.charAt(0).toUpperCase() }}</div>
      </button>
    </div>
  </header>
</template>

<style scoped>
.glass-header {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  z-index: 1;
}

.header-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 1.875rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.05em;
  white-space: nowrap;
  pointer-events: none;
}

.header-right {
  display: flex;
  gap: 1rem;
  align-items: center;
  z-index: 1;
}

.icon-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 12px;
  cursor: pointer;
  padding: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.profile-button {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  padding: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  overflow: hidden;
}

.profile-button:hover {
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-placeholder {
  color: white;
  font-weight: 700;
  font-size: 1.2rem;
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
}

@media (max-width: 480px) {
  .glass-header {
    padding: 1rem 1.25rem;
  }

  .header-title {
    font-size: 1.25rem;
  }

  .header-right {
    gap: 0.5rem;
  }

  .icon-button {
    padding: 0.5rem;
  }

  .profile-button {
    width: 36px;
    height: 36px;
  }
}
</style>
