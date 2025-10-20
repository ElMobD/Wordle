import { createApp } from 'vue'
import './style.css'
import { createPinia } from 'pinia'
import App from './App.vue'
import { i18n } from './i18n'
import { router } from './router' // <-- ajoute cette ligne

createApp(App)
.use(createPinia())
.use(i18n)
.use(router)
.mount('#app')
