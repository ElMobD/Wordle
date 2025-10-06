<script setup lang="ts">
//import HelloWorld from './components/HelloWorld.vue'
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
const switchLang = () => {
  locale.value = locale.value === 'fr' ? 'en' : 'fr'
}
</script>

<template>
  <div class="text-center mt-10">
    <p class="text-2xl font-bold">{{ $t('hello') }}</p>
    <button @click="switchLang" class="mt-4 bg-blue-500 text-white px-4 py-2 rounded">
      {{ locale === 'fr' ? 'Passer en anglais' : 'Switch to French' }}
    </button>
  </div>
</template>

