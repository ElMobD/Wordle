<script setup lang="ts">
const emit = defineEmits<{
  keyPress: [key: string]
}>()

const keyboardRows = [
  ['A', 'Z', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
  ['Q', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M'],
  ['ENTER', 'W', 'X', 'C', 'V', 'B', 'N', 'BACKSPACE']
]

const handleKeyPress = (key: string) => {
  emit('keyPress', key)
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
            'key-special': key === 'ENTER' || key === 'BACKSPACE'
          }
        ]"
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
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
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

.key:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
}

.key:active {
  transform: scale(0.95);
}

.key-special {
  min-width: 4rem;
  font-size: 0.75rem;
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
