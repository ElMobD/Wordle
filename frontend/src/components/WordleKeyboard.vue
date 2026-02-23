<script setup lang="ts">
import { computed } from 'vue'

interface Guess {
  word: string
  mask: string
}

interface Props {
  disabled?: boolean
  guesses?: (Guess | string)[]
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  guesses: () => []
})

const emit = defineEmits<{
  keyPress: [key: string]
}>()

const keyboardRows = [
  ['A', 'Z', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
  ['Q', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M'],
  ['ENTER', 'W', 'X', 'C', 'V', 'B', 'N', 'BACKSPACE']
]

// Calculer le statut de chaque lettre basé sur les guesses
const letterStatus = computed(() => {
  const status: { [key: string]: 'correct' | 'wrong-position' | 'absent' } = {}
  
  for (const guess of props.guesses) {
    const isGuessObject = typeof guess === 'object' && guess !== null && 'word' in guess
    const word = isGuessObject ? (guess as Guess).word : (guess as string)
    const mask = isGuessObject ? (guess as Guess).mask : ''
    
    for (let i = 0; i < word.length; i++) {
      const letter = word[i].toUpperCase()
      const maskChar = mask && mask.length > i ? mask[i] : '0'
      
      // Priorité : correct > wrong-position > absent
      if (maskChar === '2') {
        status[letter] = 'correct'
      } else if (maskChar === '1' && status[letter] !== 'correct') {
        status[letter] = 'wrong-position'
      } else if (maskChar === '0' && !status[letter]) {
        status[letter] = 'absent'
      }
    }
  }
  
  return status
})

const handleKeyPress = (key: string) => {
  emit('keyPress', key)
}

const getKeyStatus = (key: string) => {
  return letterStatus.value[key] || 'default'
}
</script>

<template>
  <div class="keyboard">
    <div 
      v-for="(row, rowIndex) in keyboardRows"
      :key="rowIndex"
      class="keyboard-row"
    >
      <button
        v-for="key in row"
        :key="key"
        :class="[
          'key',
          {
            'key-special': key === 'ENTER' || key === 'BACKSPACE',
            'key-disabled': disabled,
            'key-correct': getKeyStatus(key) === 'correct',
            'key-wrong-position': getKeyStatus(key) === 'wrong-position',
            'key-absent': getKeyStatus(key) === 'absent'
          }
        ]"
        :disabled="disabled"
        @click="handleKeyPress(key)"
      >
        <span v-if="key === 'BACKSPACE'">⌫</span>
        <span v-else>{{ key }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.keyboard {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  width: 100%;
  max-width: 600px;
}

.keyboard-row {
  display: flex;
  gap: 0.375rem;
  justify-content: center;
}

.key {
  min-width: 2.5rem;
  height: 3.5rem;
  padding: 0 0.5rem;
  background: rgba(55, 65, 81, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(107, 114, 128, 0.6);
  border-radius: 6px;
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.key:hover:not(:disabled) {
  background: rgba(75, 85, 99, 0.9);
  border-color: rgba(147, 157, 171, 0.8);
  transform: scale(1.05);
}

.key-disabled,
.key:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.key:active {
  transform: scale(0.95);
}

.key-special {
  min-width: 4rem;
  font-size: 0.75rem;
}

.key-correct {
  background: rgba(34, 197, 94, 0.7);
  border-color: rgba(34, 197, 94, 1);
  border-width: 2px;
  color: #ffffff;
  font-weight: 700;
  box-shadow: 0 0 12px rgba(34, 197, 94, 0.6);
}

.key-correct:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.9);
  border-color: rgba(34, 197, 94, 1);
  box-shadow: 0 0 16px rgba(34, 197, 94, 0.8);
  transform: scale(1.08);
}

.key-wrong-position {
  background: #eab308;
  border-color: #ca8a04;
  border-width: 2px;
  color: #ffffff;
  font-weight: 700;
  box-shadow: 0 0 12px rgba(234, 179, 8, 0.6);
}

.key-wrong-position:hover:not(:disabled) {
  background: #ca8a04;
  border-color: #a16207;
  box-shadow: 0 0 16px rgba(234, 179, 8, 0.8);
  transform: scale(1.08);
}

.key-absent {
  background: #6b7280;
  border-color: #4b5563;
  border-width: 2px;
  color: #ffffff;
  opacity: 1;
}

.key-absent:hover:not(:disabled) {
  background: #4b5563;
  border-color: #374151;
  opacity: 1;
}

@media (max-width: 768px) {
  .key {
    min-width: 2rem;
    height: 3rem;
    font-size: 0.75rem;
  }
  
  .key-special {
    min-width: 3rem;
    font-size: 0.65rem;
  }
  
  .keyboard-row {
    gap: 0.25rem;
  }
}

@media (max-width: 480px) {
  .key {
    min-width: 1.75rem;
    height: 2.75rem;
    padding: 0 0.25rem;
    font-size: 0.7rem;
  }
  
  .key-special {
    min-width: 2.5rem;
    font-size: 0.6rem;
  }
}
</style>
