<template>
  <header class="bg-white shadow-sm py-4 px-6 sticky top-0 z-10">
    <div class="container mx-auto flex justify-between items-center">
      <router-link to="/" class="flex items-center space-x-2">
        <i data-feather="activity" class="text-indigo-600"></i>
        <h1 class="text-xl font-bold text-gray-800">Syscall Syndicate Analytica</h1>
      </router-link>
      <nav class="hidden md:flex space-x-6">
        <router-link 
          to="/" 
          class="font-medium transition-colors duration-200"
          :class="{'text-indigo-600': $route.name === 'Home', 'text-gray-600 hover:text-indigo-600': $route.name !== 'Home'}"
        >
          Главная
        </router-link>
        <router-link 
          to="/history" 
          class="font-medium transition-colors duration-200"
          :class="{'text-indigo-600': $route.name === 'History', 'text-gray-600 hover:text-indigo-600': $route.name !== 'History'}"
        >
          История
        </router-link>
        <router-link 
          to="/expert-opinions" 
          class="font-medium transition-colors duration-200"
          :class="{'text-indigo-600': $route.name === 'ExpertOpinions', 'text-gray-600 hover:text-indigo-600': $route.name !== 'ExpertOpinions'}"
        >
          Экспертные заключения
        </router-link>
        <router-link 
          to="/administration" 
          class="font-medium transition-colors duration-200"
          :class="{'text-indigo-600': $route.name === 'Administration', 'text-gray-600 hover:text-indigo-600': $route.name !== 'Administration'}"
        >
          Администрирование
        </router-link>
      </nav>
      <div class="flex items-center space-x-4">
        <button class="p-2 rounded-full bg-gray-100 text-gray-600 hover:bg-gray-200">
          <i data-feather="bell"></i>
        </button>
      </div>
    </div>
  </header>
</template>

<script>
import { watch } from 'vue'
import { useRoute } from 'vue-router'

export default {
  name: 'Header',
  setup() {
    const route = useRoute()

    // Обновляем feather icons при смене маршрута
    watch(() => route.path, () => {
      if (typeof feather !== 'undefined') {
        setTimeout(() => feather.replace(), 100)
      }
    })

    return {
      route
    }
  }
}
</script>