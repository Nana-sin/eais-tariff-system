<template>
  <div id="app" class="min-h-screen flex flex-col bg-gray-50 font-sans vue-content">
    <Header />
    <main class="flex-grow">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script>
import { onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'

export default {
  name: 'App',
  components: {
    Header,
    Footer
  },
  setup() {
    const route = useRoute()

    onMounted(() => {
      nextTick(() => {
        if (typeof feather !== 'undefined') {
          feather.replace()
        }
      })
    })

    return {
      route
    }
  }
}
</script>

<style scoped>
.vue-content {
  opacity: 0;
  animation: fadeInUp 0.6s ease-out forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>