import { ref, computed } from 'vue'

const isAuthenticated = ref(false)
const user = ref<any>(null)
const token = ref<string | null>(null)

export function useAuth() {
  const login = (userData: any, jwtToken: string) => {
    user.value = userData
    token.value = jwtToken
    isAuthenticated.value = true
    localStorage.setItem('isAuthenticated', 'true')
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('token', jwtToken)
  }

  const logout = () => {
    user.value = null
    token.value = null
    isAuthenticated.value = false
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  const checkAuth = () => {
    const stored = localStorage.getItem('isAuthenticated')
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    // Tous les trois doivent être présents pour être authentifié
    if (stored === 'true' && storedToken && storedUser) {
      isAuthenticated.value = true
      token.value = storedToken
      user.value = JSON.parse(storedUser)
    } else {
      // Si l'un manque, on considère que c'est incohérent et on nettoie tout
      isAuthenticated.value = false
      token.value = null
      user.value = null
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    }
  }

  const getToken = (): string | null => {
    return token.value || localStorage.getItem('token')
  }

  return {
    isAuthenticated: computed(() => isAuthenticated.value),
    user: user,
    token: computed(() => token.value),
    login,
    logout,
    checkAuth,
    getToken
  }
}
