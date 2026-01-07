<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authenticatedFetch } from '../utils/api'
import Header from '../components/Header.vue'
import Modal from '../components/Modal.vue'

const router = useRouter()

const goHome = () => router.push('/homepage')
const goSettings = () => router.push('/settings')
const goToContact = () => router.push('/contact')
const goToProfile = () => router.push('/settings?tab=profil')

interface User {
  id: number
  name: string
  email: string
  picture?: string
}

interface FriendRequest {
  id: number
  requester: User
  receiver: User
  status: string
  createdAt: string
}

const activeTab = ref<'search' | 'friends' | 'requests' | 'sent'>('search')
const friends = ref<User[]>([])
const pendingRequests = ref<FriendRequest[]>([])
const sentRequests = ref<FriendRequest[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const isHelpModalOpen = ref(false)
const searchUsername = ref('')
const searchResults = ref<User[]>([])
const searchLoading = ref(false)
const selectedUser = ref<User | null>(null)
const isUserModalOpen = ref(false)
const notification = ref<{ message: string; type: 'success' | 'error' } | null>(null)
const notificationTimeout = ref<NodeJS.Timeout | null>(null)

const showHelp = () => {
  isHelpModalOpen.value = true
}

const closeHelpModal = () => {
  isHelpModalOpen.value = false
}

const openUserModal = (user: User) => {
  selectedUser.value = user
  isUserModalOpen.value = true
}

const closeUserModal = () => {
  isUserModalOpen.value = false
  selectedUser.value = null
}

const showNotification = (message: string, type: 'success' | 'error' = 'success', duration: number = 3000) => {
  if (notificationTimeout.value) {
    clearTimeout(notificationTimeout.value)
  }
  notification.value = { message, type }
  notificationTimeout.value = setTimeout(() => {
    notification.value = null
  }, duration)
}

const getRelationshipStatus = (userId: number): 'friend' | 'request-sent' | 'request-received' | 'none' => {
  // Vérifier si c'est un ami
  if (friends.value.some(f => f.id === userId)) {
    return 'friend'
  }
  
  // Vérifier s'il y a une demande envoyée
  if (sentRequests.value.some(r => r.receiver.id === userId)) {
    return 'request-sent'
  }
  
  // Vérifier s'il y a une demande reçue
  if (pendingRequests.value.some(r => r.requester.id === userId)) {
    return 'request-received'
  }
  
  return 'none'
}

const loadFriends = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await authenticatedFetch('http://localhost:8080/api/friends/list')
    friends.value = await response.json()
  } catch (err) {
    error.value = 'Erreur lors du chargement des amis'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadPendingRequests = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await authenticatedFetch('http://localhost:8080/api/friends/requests/pending')
    pendingRequests.value = await response.json()
  } catch (err) {
    error.value = 'Erreur lors du chargement des demandes'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadSentRequests = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await authenticatedFetch('http://localhost:8080/api/friends/requests/sent')
    sentRequests.value = await response.json()
  } catch (err) {
    error.value = 'Erreur lors du chargement des demandes envoyées'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const searchUsers = async () => {
  if (!searchUsername.value.trim()) return
  
  searchLoading.value = true
  try {
    const response = await authenticatedFetch(`http://localhost:8080/api/friends/search?query=${encodeURIComponent(searchUsername.value)}`)
    searchResults.value = await response.json()
  } catch (err) {
    console.error('Erreur lors de la recherche:', err)
  } finally {
    searchLoading.value = false
  }
}

const sendFriendRequest = async (userId: number) => {
  try {
    const response = await authenticatedFetch(`http://localhost:8080/api/friends/request/send/${userId}`, {
      method: 'POST'
    })
    if (!response.ok) {
      throw new Error('Erreur lors de l\'envoi')
    }
    showNotification('Demande d\'amitié envoyée', 'success')
    await loadSentRequests()
  } catch (err) {
    showNotification('Erreur lors de l\'envoi', 'error')
    console.error(err)
  }
}

const acceptRequest = async (requestId: number) => {
  try {
    const response = await authenticatedFetch(`http://localhost:8080/api/friends/request/${requestId}/accept`, {
      method: 'POST'
    })
    if (!response.ok) {
      throw new Error('Erreur lors de l\'acceptation')
    }
    showNotification('Demande acceptée', 'success')
    await loadPendingRequests()
    await loadFriends()
  } catch (err) {
    showNotification('Erreur lors de l\'acceptation', 'error')
    console.error(err)
  }
}

const rejectRequest = async (requestId: number) => {
  try {
    const response = await authenticatedFetch(`http://localhost:8080/api/friends/request/${requestId}/reject`, {
      method: 'POST'
    })
    if (!response.ok) {
      throw new Error('Erreur lors du rejet')
    }
    showNotification('Demande refusée', 'success')
    await loadPendingRequests()
  } catch (err) {
    showNotification('Erreur lors du rejet', 'error')
    console.error(err)
  }
}

const removeFriend = async (friendId: number) => {
  try {
    const response = await authenticatedFetch(`http://localhost:8080/api/friends/remove/${friendId}`, {
      method: 'DELETE'
    })
    if (!response.ok) {
      throw new Error('Erreur lors de la suppression')
    }
    showNotification('Ami retiré', 'success')
    await loadFriends()
  } catch (err) {
    showNotification('Erreur lors de la suppression', 'error')
    console.error(err)
  }
}

const switchTab = (tab: 'search' | 'friends' | 'requests' | 'sent') => {
  activeTab.value = tab
}

onMounted(async () => {
  // Charger toutes les données en parallèle au montage du composant
  await Promise.all([
    loadFriends(),
    loadPendingRequests(),
    loadSentRequests()
  ])
})
</script>

<template>
  <div class="view-container">
    <Header 
      title="AMIS"
      @home="goHome"
      @settings="goSettings"
      @help="showHelp"
      @contact="goToContact"
      @profile="goToProfile"
    />
    
    <main class="friends-content">
      <!-- Notification Toast -->
      <transition name="fade">
        <div v-if="notification" :class="['notification', notification.type]">
          {{ notification.message }}
        </div>
      </transition>

      <div class="friends-card">
        <!-- Tabs -->
        <div class="tabs">
          <button 
            :class="['tab', { active: activeTab === 'search' }]"
            @click="switchTab('search')"
          >
            Rechercher des amis
          </button>
          <button 
            :class="['tab', { active: activeTab === 'friends' }]"
            @click="switchTab('friends')"
          >
            Mes amis ({{ friends.length }})
          </button>
          <button 
            :class="['tab', { active: activeTab === 'requests' }]"
            @click="switchTab('requests')"
          >
            Demandes reçues ({{ pendingRequests.length }})
          </button>
          <button 
            :class="['tab', { active: activeTab === 'sent' }]"
            @click="switchTab('sent')"
          >
            Demandes envoyées ({{ sentRequests.length }})
          </button>
        </div>

        <!-- Search Section -->
        <div v-if="activeTab === 'search'" class="search-section">
          <h3 class="section-title">Rechercher un ami</h3>
          <div class="search-bar">
            <input
              v-model="searchUsername"
              type="text"
              placeholder="Rechercher par nom ou email..."
              @keyup.enter="searchUsers"
            />
            <button @click="searchUsers" :disabled="searchLoading">
              {{ searchLoading ? 'Recherche...' : 'Rechercher' }}
            </button>
          </div>
          
          <div v-if="searchResults.length > 0" class="search-results">
            <div v-for="user in searchResults" :key="user.id" class="user-card" @click="openUserModal(user)" style="cursor: pointer;">
              <div class="user-info">
                <div class="user-avatar">
                  <img v-if="user.picture" :src="user.picture" :alt="user.name" class="avatar-image" />
                  <span v-else>{{ (user.name || '?').charAt(0).toUpperCase() }}</span>
                </div>
                <div>
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-username">{{ user.email }}</div>
                </div>
              </div>
              <template v-if="getRelationshipStatus(user.id) === 'friend'">
                <div class="status-badge friend">
                  ✓ Ami
                </div>
              </template>
              <template v-else-if="getRelationshipStatus(user.id) === 'request-sent'">
                <div class="status-badge pending-sent">
                  ⏳ Demande envoyée
                </div>
              </template>
              <template v-else-if="getRelationshipStatus(user.id) === 'request-received'">
                <div class="status-badge pending-received">
                  ⭐ Demande reçue
                </div>
              </template>
              <template v-else>
                <button @click.stop="sendFriendRequest(user.id)" class="add-button">
                  Ajouter
                </button>
              </template>
            </div>
          </div>
          
          <div v-else-if="searchUsername && !searchLoading" class="empty-state">
            Aucun résultat trouvé
          </div>
          <div v-else-if="!searchUsername" class="empty-state">
            Entrez un nom ou un email pour rechercher
          </div>
        </div>

        <!-- Friends List -->
        <div v-if="activeTab === 'friends'" class="list-section">
          <div v-if="loading" class="loading">Chargement...</div>
          <div v-else-if="friends.length === 0" class="empty-state">
            Aucun ami pour le moment
          </div>
          <div v-else class="user-list">
            <div v-for="friend in friends" :key="friend.id" class="user-card" @click="openUserModal(friend)" style="cursor: pointer;">
              <div class="user-info">
                <div class="user-avatar">
                  <img v-if="friend.picture" :src="friend.picture" :alt="friend.name" class="avatar-image" />
                  <span v-else>{{ (friend.name || '?').charAt(0).toUpperCase() }}</span>
                </div>
                <div>
                  <div class="user-name">{{ friend.name }}</div>
                  <div class="user-username">{{ friend.email }}</div>
                </div>
              </div>
              <button @click.stop="removeFriend(friend.id)" class="remove-button">
                Retirer
              </button>
            </div>
          </div>
        </div>

        <!-- Pending Requests -->
        <div v-if="activeTab === 'requests'" class="list-section">
          <div v-if="loading" class="loading">Chargement...</div>
          <div v-else-if="pendingRequests.length === 0" class="empty-state">
            Aucune demande en attente
          </div>
          <div v-else class="user-list">
            <div v-for="request in pendingRequests" :key="request.id" class="user-card" @click="openUserModal(request.requester)" style="cursor: pointer;">
              <div class="user-info">
                <div class="user-avatar">
                  <img v-if="request.requester.picture" :src="request.requester.picture" :alt="request.requester.name" class="avatar-image" />
                  <span v-else>{{ (request.requester.name || '?').charAt(0).toUpperCase() }}</span>
                </div>
                <div>
                  <div class="user-name">{{ request.requester.name }}</div>
                  <div class="user-username">{{ request.requester.email }}</div>
                </div>
              </div>
              <div class="action-buttons">
                <button @click.stop="acceptRequest(request.id)" class="accept-button">
                  Accepter
                </button>
                <button @click.stop="rejectRequest(request.id)" class="reject-button">
                  Refuser
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Sent Requests -->
        <div v-if="activeTab === 'sent'" class="list-section">
          <div v-if="loading" class="loading">Chargement...</div>
          <div v-else-if="sentRequests.length === 0" class="empty-state">
            Aucune demande envoyée
          </div>
          <div v-else class="user-list">
            <div v-for="request in sentRequests" :key="request.id" class="user-card" @click="openUserModal(request.receiver)" style="cursor: pointer;">
              <div class="user-info">
                <div class="user-avatar">
                  <img v-if="request.receiver.picture" :src="request.receiver.picture" :alt="request.receiver.name" class="avatar-image" />
                  <span v-else>{{ (request.receiver.name || '?').charAt(0).toUpperCase() }}</span>
                </div>
                <div>
                  <div class="user-name">{{ request.receiver.name }}</div>
                  <div class="user-username">{{ request.receiver.email }}</div>
                </div>
              </div>
              <div class="status-badge" :class="request.status.toLowerCase()">
                {{ request.status === 'PENDING' ? 'En attente' : request.status }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- User Profile Modal -->
    <Modal 
      :is-open="isUserModalOpen"
      :title="selectedUser?.name || 'Profil joueur'"
      @close="closeUserModal"
    >
      <div v-if="selectedUser" class="user-profile-content">
        <div class="profile-header">
          <div class="profile-avatar">
            <img v-if="selectedUser.picture" :src="selectedUser.picture" :alt="selectedUser.name" class="avatar-image-large" />
            <div v-else class="avatar-placeholder">{{ (selectedUser.name || '?').charAt(0).toUpperCase() }}</div>
          </div>
        </div>
        <div class="profile-info">
          <div class="info-row">
            <span class="label">Nom:</span>
            <span class="value">{{ selectedUser.name }}</span>
          </div>
          <div class="info-row">
            <span class="label">Email:</span>
            <span class="value">{{ selectedUser.email }}</span>
          </div>
        </div>
      </div>
    </Modal>

    <!-- Help Modal -->
    <Modal 
      :is-open="isHelpModalOpen"
      title="Aide - Gestion des amis"
      @close="closeHelpModal"
    >
      <div class="help-content">
        <p><strong>Ajouter un ami :</strong> Recherchez un utilisateur par son nom d'utilisateur et envoyez-lui une demande.</p>
        <p><strong>Demandes reçues :</strong> Acceptez ou refusez les demandes d'amitié que vous avez reçues.</p>
        <p><strong>Demandes envoyées :</strong> Consultez le statut de vos demandes d'amitié envoyées.</p>
        <p><strong>Retirer un ami :</strong> Vous pouvez retirer un ami de votre liste à tout moment.</p>
      </div>
    </Modal>
  </div>
</template>

<style scoped>
.view-container {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

.friends-content {
  position: relative;
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 2rem;
  overflow-y: auto;
}

.friends-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 2rem;
  max-width: 800px;
  width: 100%;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.tab {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  padding: 1rem 1.5rem;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.tab:hover {
  color: white;
}

.tab.active {
  color: white;
  border-bottom-color: white;
}

.search-section {
  margin-bottom: 2rem;
}

.section-title {
  color: white;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
}

.search-bar {
  display: flex;
  gap: 0.5rem;
}

.search-bar input {
  flex: 1;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 0.875rem 1rem;
  color: white;
  font-size: 1rem;
}

.search-bar input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.search-bar button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.875rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.search-bar button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
}

.search-bar button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.search-results {
  margin-top: 1rem;
}

.list-section {
  min-height: 200px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.user-card {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.2rem;
  overflow: hidden;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  color: white;
  font-weight: 600;
  font-size: 1rem;
}

.user-username {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.add-button,
.accept-button,
.reject-button,
.remove-button {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.add-button,
.accept-button {
  background: rgba(76, 175, 80, 0.3);
  color: white;
}

.add-button:hover,
.accept-button:hover {
  background: rgba(76, 175, 80, 0.5);
}

.reject-button,
.remove-button {
  background: rgba(244, 67, 54, 0.3);
  color: white;
}

.reject-button:hover,
.remove-button:hover {
  background: rgba(244, 67, 54, 0.5);
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
}

.status-badge.pending {
  background: rgba(255, 193, 7, 0.3);
  color: white;
}

.status-badge.friend {
  background: rgba(76, 175, 80, 0.3);
  color: white;
}

.status-badge.pending-sent {
  background: rgba(33, 150, 243, 0.3);
  color: white;
}

.status-badge.pending-received {
  background: rgba(255, 152, 0, 0.3);
  color: white;
}

.loading,
.empty-state {
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
  padding: 2rem;
  font-size: 1.1rem;
}

.help-content p {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.8;
  margin-bottom: 1rem;
}

.user-profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.profile-header {
  display: flex;
  justify-content: center;
}

.profile-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-image-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 3rem;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  font-weight: 600;
}

.value {
  color: white;
  font-size: 1.1rem;
}

.notification {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 1rem 2rem;
  border-radius: 12px;
  font-weight: 600;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  color: white;
}

.notification.success {
  background: rgba(76, 175, 80, 0.8);
  backdrop-filter: blur(10px);
}

.notification.error {
  background: rgba(244, 67, 54, 0.8);
  backdrop-filter: blur(10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .friends-card {
    padding: 1rem;
  }
  
  .tabs {
    flex-direction: column;
  }
  
  .tab {
    text-align: left;
  }
  
  .user-card {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .action-buttons {
    width: 100%;
  }
  
  .action-buttons button {
    flex: 1;
  }
}
</style>
