<script setup lang="ts">
    import { useAuthStore } from '../stores/auth'
    const auth = useAuthStore()
    auth.fetchUser() // exemple: pour vérifier la session au montage
</script>

<template>
    <div class="container-app">
        <p v-if="auth.user">Bonjour {{ auth.user.name }}</p>
        <button @click="auth.logout">Déconnexion</button>
    </div>
</template>

