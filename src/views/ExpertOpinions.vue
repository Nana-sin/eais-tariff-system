<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-xl shadow-md p-6 custom-shadow">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">Экспертные заключения</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="(opinion, index) in expertOpinions" :key="index" 
               class="border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow duration-200">
            <div class="flex items-center mb-4">
              <div class="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center">
                <i data-feather="user-check" class="text-indigo-600"></i>
              </div>
              <div class="ml-4">
                <h3 class="font-semibold text-gray-800">{{ opinion.expert }}</h3>
                <p class="text-sm text-gray-500">{{ opinion.position }}</p>
              </div>
            </div>
            <h4 class="font-medium text-gray-800 mb-2">{{ opinion.product }}</h4>
            <p class="text-sm text-gray-600 mb-4 line-clamp-3">{{ opinion.summary }}</p>
            <div class="flex justify-between items-center">
              <span class="text-xs text-gray-500">{{ opinion.date }}</span>
              <button @click="viewFullOpinion(opinion)" 
                      class="text-indigo-600 hover:text-indigo-800 text-sm font-medium">
                Читать полностью
              </button>
            </div>
          </div>
        </div>

        <div v-if="expertOpinions.length === 0" class="text-center py-8">
          <i data-feather="users" class="mx-auto h-12 w-12 text-gray-400"></i>
          <h3 class="mt-2 text-sm font-medium text-gray-900">Нет экспертных заключений</h3>
          <p class="mt-1 text-sm text-gray-500">Экспертные заключения появятся после анализа товаров.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'ExpertOpinions',
  setup() {
    const expertOpinions = ref([
      {
        expert: 'Иван Петров',
        position: 'Старший аналитик',
        product: 'Интегральные схемы (8542)',
        summary: 'Рекомендуется увеличить объем закупок в связи с ожидаемым ростом цен на 15-20% в следующем квартале.',
        date: '2024-01-12',
        fullText: 'На основе анализа рыночных данных и трендов в полупроводниковой промышленности, рекомендуется увеличить объем закупок интегральных схем на 15-20% в следующем квартале. Ожидается рост цен из-за увеличения спроса со стороны автомобильной промышленности и IoT устройств.'
      },
      {
        expert: 'Мария Сидорова',
        position: 'Логистический эксперт',
        product: 'Легковые автомобили (8703)',
        summary: 'Оптимизация логистических маршрутов через Казахстан может снизить затраты на 12%.',
        date: '2024-01-08',
        fullText: 'Анализ логистических потоков показал, что использование альтернативных маршрутов через Казахстан может значительно снизить транспортные расходы. Рекомендуется переговорить с новыми логистическими партнерами в регионе.'
      }
    ])

    const viewFullOpinion = (opinion) => {
      alert(`Полное заключение от ${opinion.expert}:\n\n${opinion.fullText}`)
    }

    return {
      expertOpinions,
      viewFullOpinion
    }
  }
}
</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>