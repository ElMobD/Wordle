<script setup lang="ts">
import Navbar from './components/Navbar.vue'
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
const switchLang = () => {
  locale.value = locale.value === 'fr' ? 'en' : 'fr'
}
</script>

<template>
  <div class="h-screen w-full bg-gray-50 dark:bg-neutral-900 text-gray-900 dark:text-neutral-100 overflow-hidden">
    <Navbar />
    <router-view />
  </div>
</template>