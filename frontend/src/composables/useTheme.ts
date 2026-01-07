import { ref, watch, onMounted } from 'vue'

type Theme = 'light' | 'dark'

const theme = ref<Theme>('light')

export function useTheme() {
  onMounted(() => {
    // Charger le thème depuis localStorage
    const savedTheme = localStorage.getItem('theme') as Theme | null
    if (savedTheme) {
      theme.value = savedTheme
    }
    
    applyTheme()
  })

  watch(theme, () => {
    applyTheme()
    localStorage.setItem('theme', theme.value)
  })

  const applyTheme = () => {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  const toggleTheme = () => {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  const isDark = () => theme.value === 'dark'

  return {
    theme,
    toggleTheme,
    isDark
  }
}
