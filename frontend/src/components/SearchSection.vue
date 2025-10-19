<template>
  <section class="max-w-4xl mx-auto mb-12">
    <div class="bg-white rounded-xl shadow-md p-6 custom-shadow">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">Анализ товаров</h2>
      <div class="space-y-4">
        <div>
          <label for="search" class="block text-sm font-medium text-gray-700 mb-1">
            Код/Наименование товара
          </label>
          <div class="relative">
            <input 
              type="text" 
              id="search" 
              :value="searchQuery"
              @input="$emit('update:searchQuery', $event.target.value); $emit('fetch-suggestions')"
              class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="Начните вводить код или название товара">
            
            <!-- Индикатор загрузки -->
            <div v-if="loading" class="absolute right-3 top-3">
              <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-indigo-600"></div>
            </div>
            
            <div v-if="showSuggestions" class="absolute z-10 mt-1 w-full bg-white shadow-lg rounded-md py-1 max-h-60 overflow-auto">
              <div v-if="suggestions.length === 0" class="px-4 py-2 text-gray-500 text-sm">
                Ничего не найдено
              </div>
              <div v-else v-for="(item, index) in suggestions" :key="index" 
                   @click="$emit('select-suggestion', item)"
                   class="px-4 py-2 hover:bg-indigo-50 cursor-pointer border-b border-gray-100 last:border-b-0">
                <div class="font-medium text-gray-800">{{ item.code }} - {{ item.name }}</div>
                <div class="text-xs text-gray-500">{{ item.category }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="flex flex-col sm:flex-row gap-4">
          <button 
            @click="$emit('analyze-product')"
            :disabled="!selectedProduct || analysisLoading"
            class="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white py-3 px-6 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200">
            <div class="flex items-center justify-center space-x-2">
              <i data-feather="search" class="w-4 h-4"></i>
              <span v-if="!analysisLoading">Анализировать</span>
              <span v-else>Анализ...</span>
            </div>
          </button>
          <button class="flex-1 border border-indigo-600 text-indigo-600 hover:bg-indigo-50 py-3 px-6 rounded-lg font-medium transition-colors duration-200">
            <div class="flex items-center justify-center space-x-2">
              <i data-feather="upload" class="w-4 h-4"></i>
              <span>Пакетная загрузка</span>
            </div>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'SearchSection',
  props: {
    searchQuery: String,
    showSuggestions: Boolean,
    suggestions: Array,
    selectedProduct: Object,
    loading: Boolean,
    analysisLoading: Boolean
  },
  emits: ['update:searchQuery', 'fetch-suggestions', 'select-suggestion', 'analyze-product']
}
</script>