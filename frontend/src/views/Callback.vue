<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { setTokenCookie } from '../utils/cookie'
import LoadingScreen from '../components/LoadingScreen.vue'

const router = useRouter()
const route = useRoute()
const { login } = useAuth()

onMounted(async () => {
  // Récupère seulement le token du query parameter
  const token = route.query.token as string

  if (token) {
    try {
      // Stocke le token et récupère les infos utilisateur depuis l'API
      const response = await fetch('http://localhost:8080/api/user/profile', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.ok) {
        const userData = await response.json()
        // Stocke le token et l'user
        login(userData, token)
        setTokenCookie()
        // Redirige vers le dashboard
        router.push('/homepage')
      } else {
        console.error('Erreur lors de la récupération du profil')
        router.push('/login')
      }
    } catch (error) {
      console.error('Erreur lors du callback', error)
      router.push('/login')
    }
  } else {
    console.error('Token manquant')
    router.push('/login')
  }
})
</script>

<template>
  <LoadingScreen message="Connexion en cours..." />
</template>

<style scoped>
</style>
