<script setup lang="ts">
import { computed } from 'vue'

interface Guess {
  word: string
  mask: string
}

interface Props {
  guesses: Guess[] | string[]
  currentGuess: string
  maxGuesses?: number
  wordLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxGuesses: 6,
  wordLength: 5
})

const getMaskColor = (letter: string, maskChar: string | undefined) => {
  if (maskChar === '2') return 'green'   // Bon endroit
  if (maskChar === '1') return 'yellow'  // Mauvais endroit
  return 'gray'                           // Absent
}

// Générer les lignes de la grille
const gridRows = computed(() => {
  const rows: any[] = []
  
  // Ajouter les tentatives déjà effectuées
  for (let i = 0; i < props.guesses.length; i++) {
    const guess = props.guesses[i]
    const isGuessObject = typeof guess === 'object' && guess !== null && 'word' in guess
    const word = isGuessObject ? (guess as Guess).word : (guess as string)
    const mask = isGuessObject ? (guess as Guess).mask : ''
    
    const letters = word.split('').map((letter, idx) => ({
      char: letter,
      color: mask && mask.length > idx ? getMaskColor(letter, mask[idx]) : 'none'
    }))
    
    rows.push({
      letters,
      isCurrentGuess: false,
      isSubmitted: true
    })
  }
  
  // Ajouter la tentative en cours
  if (props.guesses.length < props.maxGuesses) {
    const currentLetters = props.currentGuess.split('').map(char => ({
      char,
      color: 'none'
    }))
    while (currentLetters.length < props.wordLength) {
      currentLetters.push({ char: '', color: 'none' })
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
      letters: Array(props.wordLength).fill({ char: '', color: 'none' }),
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
        v-for="(letterObj, letterIndex) in row.letters"
        :key="letterIndex"
        :class="[
          'grid-cell',
          {
            'has-letter': letterObj.char !== '',
            'is-current': row.isCurrentGuess,
            'is-submitted': row.isSubmitted,
            'color-green': letterObj.color === 'green',
            'color-yellow': letterObj.color === 'yellow',
            'color-gray': letterObj.color === 'gray'
          }
        ]"
      >
        {{ letterObj.char }}
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

/* Couleurs du mask */
.grid-cell.color-green {
  background: rgba(34, 197, 94, 0.7);
  border-color: rgba(34, 197, 94, 1);
}

.grid-cell.color-yellow {
  background: rgba(202, 138, 4, 0.7);
  border-color: rgba(202, 138, 4, 1);
}

.grid-cell.color-gray {
  background: rgba(107, 114, 128, 0.7);
  border-color: rgba(107, 114, 128, 1);
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
