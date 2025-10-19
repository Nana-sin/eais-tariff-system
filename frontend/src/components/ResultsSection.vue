<template>
  <section class="max-w-6xl mx-auto transition-all duration-300">
    <div class="bg-white rounded-xl shadow-md overflow-hidden custom-shadow">
      <ProductCard :product="selectedProduct" :recommendationData="recommendationData" />
      
      <!-- Индикатор загрузки анализа -->
      <div v-if="analysisLoading" class="p-8 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">Проводим анализ мер ТТП...</p>
      </div>
      
      <template v-else>
        <Recommendations :recommendationData="recommendationData" />
        <KeyMetrics :recommendationData="recommendationData" />
        <DetailedAnalysis 
          :sections="sections"
          :suppliersData="suppliersData"
          :recommendationData="recommendationData"
          @toggle-section="$emit('toggle-section', $event)"
        />
      </template>

      <!-- Action Buttons -->
      <div class="p-6 bg-gray-50 rounded-b-xl flex flex-wrap justify-between gap-4">
        <div class="flex space-x-3">
          <button class="flex items-center space-x-2 bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200">
            <i data-feather="download" class="w-4 h-4"></i>
            <span>Экспорт в Excel</span>
          </button>
          <button class="flex items-center space-x-2 bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200">
            <i data-feather="file-text" class="w-4 h-4"></i>
            <span>Экспорт в PDF</span>
          </button>
        </div>
        <button class="flex items-center space-x-2 bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors duration-200">
          <i data-feather="send" class="w-4 h-4"></i>
          <span>Отправить в АНО</span>
        </button>
      </div>
    </div>
  </section>
</template>

<script>
import ProductCard from './ProductCard.vue'
import Recommendations from './Recommendations.vue'
import KeyMetrics from './KeyMetrics.vue'
import DetailedAnalysis from './DetailedAnalysis.vue'

export default {
  name: 'ResultsSection',
  components: {
    ProductCard,
    Recommendations,
    KeyMetrics,
    DetailedAnalysis
  },
  props: {
    selectedProduct: Object,
    recommendationData: Object,
    sections: Object,
    suppliersData: Array,
    analysisLoading: Boolean
  },
  emits: ['toggle-section']
}
</script>