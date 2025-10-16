<script setup lang="ts">
  import { onMounted } from 'vue';
  import NavItem from './NavItem.vue';
  onMounted(() => {
  const menuToggle = document.getElementById('menu-toggle')
  const mobileMenu = document.getElementById('mobile-menu')
  if (menuToggle && mobileMenu) {
    menuToggle.addEventListener('click', function () {
      mobileMenu.classList.toggle('hidden')
    })
  }
})
</script>

<template>
<nav class="bg-white border border-gray-200 dark:border-gray-700 px-2 sm:px-4 py-2.5 rounded dark:bg-gray-800 shadow">
  <div class="container flex flex-wrap justify-between items-center mx-auto">
    <a href="/" class="flex items-center">
      <span class="self-center text-xl font-semibold whitespace-nowrap dark:text-white">
        WORDLE +
      </span>
    </a>

    <div class="flex items-center">
      <button
        id="menu-toggle"
        type="button"
        class="inline-flex items-center p-2 ml-3 text-sm text-gray-500 rounded-lg hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600 md:hidden"
      >
        <span class="sr-only">Open main menu</span>
        <!-- Hamburger icon -->
        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 6h16M4 12h16m-7 6h7"
          />
        </svg>
      </button>
    </div>

    <div
      class="w-full md:block md:w-auto hidden"
      id="mobile-menu"
    >
      <ul class="flex flex-col mt-4 md:flex-row md:space-x-8 md:mt-0 md:text-sm md:font-medium">
        <NavItem label="Home" to="/">
          <template #icon>
            <img src="../assets/settings.svg" alt="Home" class="w-5 h-5" />
          </template>
        </NavItem>
        <NavItem label="Home" to="/login">
          <template #icon>
            <img src="../assets/question.svg" alt="login" class="w-5 h-5" />
          </template>
        </NavItem>
      </ul>
    </div>

  </div>
</nav>
</template>

