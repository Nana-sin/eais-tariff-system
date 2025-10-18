<template>
  <div class="p-6 border-b border-gray-200">
    <div class="flex flex-col md:flex-row gap-6">
      <div class="flex-grow">
        <div class="flex justify-between items-start">
          <div>
            <h3 class="text-xl font-bold text-gray-800">{{ product.name }}</h3>
            <p class="text-gray-600">Код ТН ВЭД: {{ product.code }}</p>
            <p v-if="recommendationData" class="text-sm text-gray-500 mt-1">
              Статус анализа: 
              <span :class="statusClass" class="font-medium">{{ statusText }}</span>
            </p>
          </div>
          <div class="flex space-x-2">
            <button class="p-2 rounded-lg bg-gray-100 text-gray-600 hover:bg-gray-200 transition-colors duration-200">
              <i data-feather="download" class="w-4 h-4"></i>
            </button>
            <button class="p-2 rounded-lg bg-gray-100 text-gray-600 hover:bg-gray-200 transition-colors duration-200">
              <i data-feather="share-2" class="w-4 h-4"></i>
            </button>
          </div>
        </div>
        <div class="mt-4">
          <div v-if="recommendationData" class="inline-block bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
            <i data-feather="trending-up" class="inline mr-1 w-4 h-4"></i>
            Общий балл: {{ recommendationData.totalScore }}/100
          </div>
          <div v-else class="inline-block bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">
            <i data-feather="info" class="inline mr-1 w-4 h-4"></i>
            Готов к анализу
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ProductCard',
  props: {
    product: Object,
    recommendationData: Object
  },
  setup(props) {
    const statusClass = computed(() => {
      const status = props.recommendationData?.status
      switch (status) {
        case 'COMPLETED': return 'text-green-600'
        case 'IN_PROGRESS': return 'text-yellow-600'
        case 'FAILED': return 'text-red-600'
        default: return 'text-gray-600'
      }
    })

    const statusText = computed(() => {
      const status = props.recommendationData?.status
      switch (status) {
        case 'COMPLETED': return 'Завершен'
        case 'IN_PROGRESS': return 'В процессе'
        case 'FAILED': return 'Ошибка'
        case 'PENDING': return 'Ожидание'
        default: return 'Не начат'
      }
    })

    return {
      statusClass,
      statusText
    }
  }
}
</script>