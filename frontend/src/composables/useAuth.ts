import { ref, computed } from 'vue'
import { setTokenCookie, removeTokenCookie } from '../utils/tokenCookie'

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
    setTokenCookie(jwtToken)
  }

  const logout = () => {
    user.value = null
    token.value = null
    isAuthenticated.value = false
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    removeTokenCookie()
  }

  const checkAuth = async () => {
    const stored = localStorage.getItem('isAuthenticated')
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    // Tous les trois doivent être présents pour être authentifié
    if (stored === 'true' && storedToken && storedUser) {
      isAuthenticated.value = true
      token.value = storedToken
      user.value = JSON.parse(storedUser)
      setTokenCookie(storedToken)
      // Vérifier auprès du serveur que l'utilisateur existe toujours
      try {
        const response = await fetch('http://localhost:8080/api/user/profile', {
          headers: {
            'Authorization': `Bearer ${storedToken}`
          }
        })
        if (!response.ok) {
          // L'utilisateur n'existe plus ou le token est invalide
          logout()
        }
        // Si ok, l'utilisateur est toujours valide
      } catch (error) {
        // Erreur réseau, on garde l'authentification pour l'instant
        console.error('Erreur lors de la vérification d\'authentification:', error)
      }
    } else {
      // Si l'un manque, on considère que c'est incohérent et on nettoie tout
      isAuthenticated.value = false
      token.value = null
      user.value = null
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      removeTokenCookie()
    }
  }

  const getToken = (): string | null => {
    const storedToken = localStorage.getItem('token')
    token.value = storedToken
    return storedToken
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
