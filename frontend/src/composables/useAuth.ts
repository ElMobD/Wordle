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
    // Supprime le cookie userId
    document.cookie = 'userId=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/';
  }

  const checkAuth = async (): Promise<boolean> => {
    const storedToken = localStorage.getItem('token')

    // Sans token, on considère l'utilisateur non authentifié.
    if (!storedToken) {
      logout()
      return false
    }

    token.value = storedToken
    setTokenCookie(storedToken)

    // Hydrate l'état user local si disponible (cache UI),
    // mais l'autorisation reste validée par l'API.
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch {
        localStorage.removeItem('user')
      }
    }

    try {
      const response = await fetch('http://localhost/api/user/check', {
        headers: {
          'Authorization': `Bearer ${storedToken}`
        }
      })
      console.log('Vérification d\'authentification, réponse API:', response)
      if (!response.ok) {
        logout()
        return false
      }

      const data = await response.json()
      if (!data?.authenticated) {
        logout()
        return false
      }

      isAuthenticated.value = true
      localStorage.setItem('isAuthenticated', 'true')

      if (data.userId) {
        document.cookie = `userId=${data.userId}; path=/`
      }

      return true
    } catch (error) {
      // En cas d'erreur réseau, on conserve la session locale temporairement.
      isAuthenticated.value = true
      console.error('Erreur lors de la vérification d\'authentification:', error)
      return true
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
