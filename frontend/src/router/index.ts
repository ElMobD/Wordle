import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import Login from '../views/Login.vue'
import Callback from '../views/Callback.vue'
import Homepage from '../views/Homepage.vue'
import Settings from '../views/Settings.vue'
import DailyWord from '../views/DailyWord.vue'
import Contact from '../views/Contact.vue'

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
    component: () => import('../views/Multiplayer.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/lobby/:sessionCode',
    component: () => import('../views/Lobby.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: ':gameId',
        component: () => import('../views/GamePlay.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'leaderboard',
        component: () => import('../views/Leaderboard.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/',
    redirect: '/homepage'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const { checkAuth } = useAuth()

  if (to.meta.requiresAuth) {
    const isValidSession = await checkAuth()
    if (!isValidSession) {
      next('/login')
      return
    }
  }

  if (to.path === '/login') {
    const isValidSession = await checkAuth()
    if (isValidSession) {
      next('/homepage')
      return
    }
  }

  next()
})

export default router
