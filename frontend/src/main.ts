import { createApp } from 'vue'
import router from './router'
import './style.css'
import App from './App.vue'

// Initialiser le thème avant de monter l'app
const savedTheme = localStorage.getItem('theme') || 'dark'
document.documentElement.setAttribute('data-theme', savedTheme)

const app = createApp(App)

app.use(router)
app.mount('#app')
