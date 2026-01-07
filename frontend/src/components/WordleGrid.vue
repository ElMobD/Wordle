<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  guesses: string[]
  currentGuess: string
  maxGuesses?: number
  wordLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxGuesses: 6,
  wordLength: 5
})

// Générer les lignes de la grille
const gridRows = computed(() => {
  const rows = []
  
  // Ajouter les tentatives déjà effectuées
  for (let i = 0; i < props.guesses.length; i++) {
    const guess = props.guesses[i]
    rows.push({
      letters: guess ? guess.split('') : [],
      isCurrentGuess: false,
      isSubmitted: true
    })
  }
  
  // Ajouter la tentative en cours
  if (props.guesses.length < props.maxGuesses) {
    const currentLetters = props.currentGuess.split('')
    while (currentLetters.length < props.wordLength) {
      currentLetters.push('')
    }
    rows.push({
      letters: currentLetters,
      isCurrentGuess: true,
      isSubmitted: false
    })
  }
  
  // Ajouter les lignes vides restantes
  const emptyRowsCount = props.maxGuesses - rows.length
  for (let i = 0; i < emptyRowsCount; i++) {
    rows.push({
      letters: Array(props.wordLength).fill(''),
      isCurrentGuess: false,
      isSubmitted: false
    })
  }
  
  return rows
})
</script>

<template>
  <div class="wordle-grid">
    <div 
      v-for="(row, rowIndex) in gridRows" 
      :key="rowIndex"
      class="grid-row"
    >
      <div 
        v-for="(letter, letterIndex) in row.letters"
        :key="letterIndex"
        :class="[
          'grid-cell',
          {
            'has-letter': letter !== '',
            'is-current': row.isCurrentGuess,
            'is-submitted': row.isSubmitted
          }
        ]"
      >
        {{ letter }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.wordle-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
}

.grid-row {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.grid-cell {
  width: 3.5rem;
  height: 3.5rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  color: white;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
}

.grid-cell.has-letter {
  border-color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.1);
  animation: pop 0.1s ease-in-out;
}

.grid-cell.is-current {
  border-color: rgba(255, 255, 255, 0.6);
}

.grid-cell.is-submitted {
  background: rgba(107, 114, 128, 0.4);
  border-color: rgba(107, 114, 128, 0.6);
}

@keyframes pop {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@media (max-width: 480px) {
  .grid-cell {
    width: 2.5rem;
    height: 2.5rem;
    font-size: 1.5rem;
  }
  
  .grid-row {
    gap: 0.375rem;
  }
  
  .wordle-grid {
    gap: 0.375rem;
  }
}
</style>
