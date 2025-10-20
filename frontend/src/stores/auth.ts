import { defineStore } from 'pinia'

type Me = { name?: string; email?: string; sub?: string } | null
const API = import.meta.env.VITE_API_URL

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as Me,
    checked: false,
    loading: false,
  }),

  actions: {
    // Lance le flux Google côté backend
    login(redirectTo?: string) {
      // on garde la destination pour après le login
      if (redirectTo) {
        sessionStorage.setItem('redirectAfterLogin', redirectTo)
      }
      window.location.href = `${API}/oauth2/authorization/google`
    },

    // Récupère l'utilisateur courant (cookie JSESSIONID inclus)
    async fetchUser() {
      this.loading = true
      try {
        const res = await fetch(`${API}/api/me`, { credentials: 'include' })
        if (res.status === 401) {
          this.user = null
          return false
        }
        const data = await res.json()
        this.user = data?.authenticated ? { name: data.name, email: data.email, sub: data.sub } : null
        return !!this.user
      } finally {
        this.checked = true
        this.loading = false
      }
    },

    // Déconnexion
    logout() {
        // Récupère le token CSRF placé par Spring dans un cookie lisible
        const token = document.cookie.split('; ')
            .find(c => c.startsWith('XSRF-TOKEN='))?.split('=')[1]
        fetch(`${API}/logout`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': token || ''
            }
        }).finally(() => {
            window.location.href = '/login'
            this.user = null
            this.checked = false
        })
    }
  },
})
