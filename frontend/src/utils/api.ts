import { useAuth } from '../composables/useAuth'
import router from '../router'

/**
 * Effectue une requête fetch authentifiée
 * Ajoute automatiquement le token JWT au header Authorization
 * Gère les réponses 401 en déconnectant l'utilisateur
 */
export async function authenticatedFetch(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const { logout, checkAuth, getToken } = useAuth()

  // Sync refs with localStorage before using the token
  checkAuth()

  // 1️⃣ Prépare les headers
  const headers = new Headers(options.headers || {})
  
  // 2️⃣ Ajoute le token JWT si disponible
  const jwt = getToken()
  if (jwt) {
    headers.set('Authorization', `Bearer ${jwt}`)
  }

  // 3️⃣ Ajoute Content-Type pour JSON si pas déjà défini
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }

  // 4️⃣ Merge headers avec le reste des options
  const finalOptions: RequestInit = {
    ...options,
    headers
  }

  try {
    const response = await fetch(url, finalOptions)

    // 5️⃣ Gère le 401 Unauthorized
    if (response.status === 401) {
      logout()
      await router.push('/login')
      throw new Error('Session expirée - veuillez vous reconnecter')
    }

    return response
  } catch (error) {
    console.error('Erreur lors de la requête:', error)
    throw error
  }
}
