<script setup lang="ts">
import { ref } from 'vue'

const loading = ref(false)
const error = ref('')


const loginWithGoogle = () => {
  // Redirection vers le backend pour OAuth2 Google
  window.location.href = 'http://localhost:8080/oauth2/authorization/google'
}
</script>


<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Wordle</h1>
      <p>Connectez-vous pour jouer</p>
      

      <div v-if="error" class="error">{{ error }}</div>

      <div class="oauth-divider">
        <span>ou</span>
      </div>

      <button class="oauth-button" @click="loginWithGoogle" :disabled="loading">
        Se connecter avec Google
      </button>
    </div>
  </div>
</template>
<style scoped>
</style>
