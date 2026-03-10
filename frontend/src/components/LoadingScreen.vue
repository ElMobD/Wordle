<script setup lang="ts">
const tiles = ['W', 'O', 'R', 'D', 'L']

withDefaults(
  defineProps<{
    message?: string
    messageSub?: string
    fullscreen?: boolean
  }>(),
  {
    message: 'Chargement...',
    fullscreen: true
  }
)
</script>

<template>
  <div
    :class="[
      fullscreen
        ? 'fixed inset-0 z-[150]'
        : 'relative w-full min-h-[260px] rounded-2xl overflow-hidden',
      'loading-root'
    ]"
  >
    <div class="loading-bg-orb loading-bg-orb-a"></div>
    <div class="loading-bg-orb loading-bg-orb-b"></div>
    <div class="loading-bg-grid"></div>

    <div class="loading-shell">
      <div class="loading-brand">WORDLE+</div>

      <div class="loading-tiles" aria-hidden="true">
        <div
          v-for="(tile, idx) in tiles"
          :key="`${tile}-${idx}`"
          class="loading-tile"
          :style="{ '--delay': `${idx * 0.12}s` }"
        >
          {{ tile }}
        </div>
      </div>

      <div class="loading-progress" aria-hidden="true">
        <div class="loading-progress-bar"></div>
      </div>

      <p class="loading-message">{{ message }}</p>
      <p class="loading-sub">{{ messageSub }}</p>
    </div>
  </div>
</template>

<style scoped>
.loading-root {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 20%, var(--loading-orb-a), transparent 42%),
    radial-gradient(circle at 88% 78%, var(--loading-orb-b), transparent 42%),
    linear-gradient(180deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
}

.loading-bg-orb {
  position: absolute;
  border-radius: 9999px;
  filter: blur(48px);
  opacity: 0.55;
  animation: orb-float 6.5s ease-in-out infinite;
}

.loading-bg-orb-a {
  width: min(34vw, 430px);
  height: min(34vw, 430px);
  left: -8%;
  top: -10%;
  background: var(--loading-orb-a);
}

.loading-bg-orb-b {
  width: min(28vw, 360px);
  height: min(28vw, 360px);
  right: -6%;
  bottom: -9%;
  background: var(--loading-orb-b);
  animation-delay: 0.8s;
}

.loading-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: radial-gradient(circle at center, black 32%, transparent 100%);
  opacity: 0.38;
}

.loading-shell {
  position: relative;
  z-index: 1;
  width: min(92vw, 640px);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 24px;
  background: linear-gradient(155deg, var(--loading-shell-start), var(--loading-shell-end));
  backdrop-filter: blur(22px);
  padding: clamp(1.4rem, 2.4vw, 2rem);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(0.8rem, 1.6vw, 1.1rem);
  box-shadow:
    0 24px 65px rgba(0, 0, 0, 0.42),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.loading-brand {
  color: var(--loading-text);
  font-size: clamp(1.2rem, 2vw, 1.7rem);
  font-weight: 800;
  letter-spacing: 0.18em;
}

.loading-tiles {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: clamp(0.35rem, 1vw, 0.75rem);
  width: min(100%, 420px);
}

.loading-tile {
  --tile-size: clamp(2.7rem, 7vw, 4rem);
  width: var(--tile-size);
  height: var(--tile-size);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--loading-text);
  font-size: clamp(1rem, 2.1vw, 1.5rem);
  font-weight: 800;
  text-transform: uppercase;
  transform-style: preserve-3d;
  animation: tile-flip 1.7s ease-in-out infinite;
  animation-delay: var(--delay);
  box-shadow:
    0 8px 22px rgba(0, 0, 0, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

.loading-tile:nth-child(1) {
  background: var(--loading-tile-1);
}

.loading-tile:nth-child(2) {
  background: var(--loading-tile-2);
}

.loading-tile:nth-child(3) {
  background: var(--loading-tile-3);
}

.loading-tile:nth-child(4) {
  background: var(--loading-tile-4);
}

.loading-tile:nth-child(5) {
  background: var(--loading-tile-5);
}

.loading-progress {
  width: min(92%, 420px);
  height: 7px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  overflow: hidden;
}

.loading-progress-bar {
  width: 35%;
  height: 100%;
  border-radius: inherit;
  background: var(--loading-progress);
  animation: progress-slide 1.7s ease-in-out infinite;
}

.loading-message {
  margin: 0;
  color: var(--loading-text);
  font-size: clamp(0.95rem, 1.7vw, 1.1rem);
  font-weight: 700;
  letter-spacing: 0.04em;
}

.loading-sub {
  margin: 0;
  color: var(--loading-subtext);
  font-size: clamp(0.78rem, 1.35vw, 0.92rem);
  letter-spacing: 0.02em;
}

@keyframes tile-flip {
  0%,
  100% {
    transform: perspective(700px) rotateX(0deg) translateY(0);
    filter: brightness(1);
  }
  40% {
    transform: perspective(700px) rotateX(88deg) translateY(-3px);
    filter: brightness(1.07);
  }
  55% {
    transform: perspective(700px) rotateX(0deg) translateY(0);
    filter: brightness(1);
  }
}

@keyframes progress-slide {
  0% {
    transform: translateX(-130%);
  }
  60% {
    transform: translateX(180%);
  }
  100% {
    transform: translateX(220%);
  }
}

@keyframes orb-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(0, -16px, 0) scale(1.04);
  }
}

@media (max-width: 640px) {
  .loading-shell {
    width: 92vw;
    border-radius: 18px;
    padding: 1.1rem 0.9rem;
  }

  .loading-tiles {
    gap: 0.45rem;
    justify-items: center;
  }
}
</style>
