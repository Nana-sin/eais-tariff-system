<template>
  <div class="p-6">
    <h4 class="text-lg font-semibold text-gray-800 mb-4">Детальный анализ</h4>
    
    <div class="space-y-4">
      <!-- Dynamic Import Trends Chart -->
      <div>
        <button @click="$emit('toggle-section', 'importTrends')" 
                class="w-full flex justify-between items-center py-3 px-4 bg-gray-50 rounded-lg hover:bg-gray-100">
          <span class="font-medium text-gray-800">Динамика импорта</span>
          <i data-feather="chevron-down" class="w-5 h-5 text-gray-500 transition-transform duration-300" 
             :class="{'transform rotate-180': sections.importTrends}"></i>
        </button>
        <div v-if="sections.importTrends" class="mt-3 px-4 py-4 bg-white border border-gray-200 rounded-lg">
          <div class="h-64">
            <canvas ref="importTrendsChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Price Analysis Chart -->
      <div>
        <button @click="$emit('toggle-section', 'priceAnalysis')" 
                class="w-full flex justify-between items-center py-3 px-4 bg-gray-50 rounded-lg hover:bg-gray-100">
          <span class="font-medium text-gray-800">Анализ цен</span>
          <i data-feather="chevron-down" class="w-5 h-5 text-gray-500 transition-transform duration-300" 
             :class="{'transform rotate-180': sections.priceAnalysis}"></i>
        </button>
        <div v-if="sections.priceAnalysis" class="mt-3 px-4 py-4 bg-white border border-gray-200 rounded-lg">
          <div class="h-64">
            <canvas ref="priceAnalysisChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Suppliers Table -->
      <div>
        <button @click="$emit('toggle-section', 'suppliers')" 
                class="w-full flex justify-between items-center py-3 px-4 bg-gray-50 rounded-lg hover:bg-gray-100">
          <span class="font-medium text-gray-800">Основные поставщики</span>
          <i data-feather="chevron-down" class="w-5 h-5 text-gray-500 transition-transform duration-300" 
             :class="{'transform rotate-180': sections.suppliers}"></i>
        </button>
        <div v-if="sections.suppliers" class="mt-3 overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Страна</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Доля</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Объем</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ср. цена</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(supplier, index) in suppliersData" :key="index">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10 rounded-full overflow-hidden bg-gray-100 flex items-center justify-center">
                      <i data-feather="flag" class="text-gray-400"></i>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ supplier.country }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{{ supplier.share }}%</div>
                  <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                    <div class="bg-indigo-600 h-1.5 rounded-full" :style="'width: ' + supplier.share + '%'"></div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ supplier.volume }} т</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${{ supplier.avgPrice }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useCharts } from '../composables/useCharts'

export default {
  name: 'DetailedAnalysis',
  props: {
    sections: Object,
    suppliersData: Array
  },
  emits: ['toggle-section'],
  setup(props) {
    const importTrendsChart = ref(null)
    const priceAnalysisChart = ref(null)
    const { initImportTrendsChart, initPriceAnalysisChart } = useCharts()

    onMounted(() => {
      watch(
        () => props.sections.importTrends,
        (newVal) => {
          if (newVal) {
            nextTick(() => {
              initImportTrendsChart(importTrendsChart.value)
            })
          }
        },
        { immediate: true }
      )

      watch(
        () => props.sections.priceAnalysis,
        (newVal) => {
          if (newVal) {
            nextTick(() => {
              initPriceAnalysisChart(priceAnalysisChart.value)
            })
          }
        }
      )
    })

    return {
      importTrendsChart,
      priceAnalysisChart
    }
  }
}
</script>