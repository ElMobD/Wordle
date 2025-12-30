<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const route = useRoute()
const { login } = useAuth()

onMounted(async () => {
  // Récupère le token du query parameter
  const token = route.query.token as string
  const userStr = route.query.user as string

  if (token && userStr) {
    try {
      // Parse les infos utilisateur
      const userData = JSON.parse(decodeURIComponent(userStr))
      
      // Stocke le token et l'user
      login(userData, token)
      
      // Redirige vers le dashboard
      router.push('/dashboard')
    } catch (error) {
      console.error('Erreur lors du parsing du callback', error)
      router.push('/login')
    }
  } else {
    console.error('Token ou user manquant')
    router.push('/login')
  }
})
</script>

<template>
  <div class="callback-container">
    <div class="loading">
      <p>Connexion en cours...</p>
      <div class="spinner"></div>
    </div>
  </div>
</template>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.loading {
  text-align: center;
  color: white;
}

.loading p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
