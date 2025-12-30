<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { user, logout } = useAuth()

const userObject = computed(() => user.value || {})

const handleLogout = () => {
  logout()
  router.push('/login')
}

const formatKey = (key: string | number): string => {
  return String(key)
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
    .trim()
}

const formatValue = (value: any): string => {
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}
</script>


<template>
  <div class="dashboard-container">
    <nav class="navbar">
      <h1>Wordle</h1>
      <button @click="handleLogout" class="logout-btn">Déconnexion</button>
    </nav>

    <main class="dashboard-content">
      <div v-if="user" class="user-info">
        <h2>Informations utilisateur</h2>
        <div class="info-grid">
          <div v-for="(value, key) in userObject" :key="key" class="info-card">
            <span class="info-label">{{ formatKey(key) }}</span>
            <span class="info-value">{{ formatValue(value) }}</span>
          </div>
        </div>
      </div>
      <div v-else class="no-user">
        <p>Aucun utilisateur connecté</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.navbar {
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.navbar h1 {
  margin: 0;
  font-size: 1.5rem;
}

.logout-btn {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  color: white;
}

.user-info h2 {
  font-size: 2rem;
  margin-bottom: 2rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.info-card {
  background: rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-label {
  font-size: 0.9rem;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 1.1rem;
  font-weight: 500;
  word-break: break-all;
}

.no-user {
  text-align: center;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}
</style>
