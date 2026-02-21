import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import Login from '../views/Login.vue'
import Callback from '../views/Callback.vue'
import Homepage from '../views/Homepage.vue'
import Settings from '../views/Settings.vue'
import DailyWord from '../views/DailyWord.vue'
import Contact from '../views/Contact.vue'
import Multiplayer from '../views/Multiplayer.vue'

const routes = [
  {
    path: '/login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/callback',
    component: Callback,
    meta: { requiresAuth: false }
  },
  {
    path: '/homepage',
    component: Homepage,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    component: Settings,
    meta: { requiresAuth: true }
  },
  {
    path: '/daily-word',
    component: DailyWord,
    meta: { requiresAuth: true }
  },
  {
    path: '/contact',
    component: Contact,
    meta: { requiresAuth: true }
  },
  {
    path: '/multiplayer',
    component: Multiplayer,
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    redirect: () => {
      const { isAuthenticated } = useAuth()
      return isAuthenticated.value ? '/homepage' : '/login'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const { isAuthenticated, checkAuth } = useAuth()
  
  // Vérifier l'état d'authentification (y compris serveur)
  await checkAuth()
  
  // Si l'utilisateur essaie d'accéder à une route protégée sans être connecté
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    next('/login')
  }
  // Si l'utilisateur est connecté et essaie d'accéder au login
  else if (to.path === '/login' && isAuthenticated.value) {
    next('/homepage')
  }
  // Sinon, autoriser la navigation
  else {
    next()
  }
})

export default router
