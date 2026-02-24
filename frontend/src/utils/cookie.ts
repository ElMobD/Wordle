import { useAuth } from '../composables/useAuth'

/**
 * Place le token JWT dans un cookie nommé "token" pour Nginx
 */
export function setTokenCookie() {
  const { getToken } = useAuth()
  const token = getToken()
  if (token) {
    document.cookie = `token=${token}; path=/; secure; samesite=strict`
  }
}
