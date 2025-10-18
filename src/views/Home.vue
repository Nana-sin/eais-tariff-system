<template>
  <div class="container mx-auto px-4 py-8">
    <SearchSection 
      :search-query="searchQuery"
      :show-suggestions="showSuggestions"
      :suggestions="suggestions"
      :selected-product="selectedProduct"
      :loading="loading"
      :analysis-loading="analysisLoading"
      @update:searchQuery="updateSearchQuery"
      @fetch-suggestions="fetchSuggestions"
      @select-suggestion="selectSuggestion"
      @analyze-product="analyzeProduct"
    />
    
    <ResultsSection 
      v-if="showResults"
      :selected-product="selectedProduct"
      :recommendation-data="recommendationData"
      :sections="sections"
      :suppliers-data="suppliersData"
      :analysis-loading="analysisLoading"
      @toggle-section="toggleSection"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import SearchSection from '../components/SearchSection.vue'
import ResultsSection from '../components/ResultsSection.vue'

// Мок-данные
const mockProducts = [
  { code: '8542', name: 'Интегральные схемы', category: 'Электроника' },
  { code: '8703', name: 'Легковые автомобили', category: 'Автомобили' },
  { code: '2710', name: 'Нефть сырая', category: 'Нефтепродукты' },
  { code: '7207', name: 'Полуфабрикаты из железа', category: 'Металлургия' },
  { code: '0306', name: 'Креветки мороженые', category: 'Продукты питания' },
]

const mockRecommendation = {
  requestId: 'req-12345',
  tnVedCode: '8542',
  productName: 'Интегральные схемы',
  status: 'COMPLETED',
  totalScore: 85.5,
  summary: 'Высокий потенциал для применения мер ТТП. Рекомендуется рассмотреть антидемпинговые пошлины.',
  measures: [
    {
      id: 1,
      measureType: 'ANTI_DUMPING_CHINA',
      measureName: 'Антидемпинговые пошлины на товары из Китая',
      applicable: true,
      score: 90.0,
      reasoning: 'Высокая доля импорта из Китая (45%) и значительная разница в ценах',
      details: 'Рекомендуемая ставка: 25-35%',
      importShare: 45.0,
      productionCapacity: 75.0,
      priceDifference: 40.0
    },
    {
      id: 2,
      measureType: 'TARIFF_35_50',
      measureName: 'Повышенные таможенные пошлины',
      applicable: true,
      score: 80.5,
      reasoning: 'Наличие отечественных производителей с достаточными мощностями',
      details: 'Рекомендуемая ставка: 15-25%',
      importShare: 45.0,
      productionCapacity: 75.0,
      priceDifference: 25.0
    }
  ],
  createdAt: '2024-01-15T10:30:00Z',
  completedAt: '2024-01-15T10:35:00Z'
}

export default {
  name: 'Home',
  components: {
    SearchSection,
    ResultsSection
  },
  setup() {
    const searchQuery = ref('')
    const showSuggestions = ref(false)
    const suggestions = ref([])
    const selectedProduct = ref(null)
    const showResults = ref(false)
    const loading = ref(false)
    const analysisLoading = ref(false)
    const recommendationData = ref(null)
    
    const sections = ref({
      importTrends: true,
      priceAnalysis: false,
      suppliers: false
    })

    const suppliersData = ref([
      { country: 'Китай', share: 45, volume: 558, avgPrice: 1420 },
      { country: 'Германия', share: 22, volume: 273, avgPrice: 1580 },
      { country: 'США', share: 15, volume: 186, avgPrice: 1650 },
      { country: 'Турция', share: 10, volume: 124, avgPrice: 1320 },
      { country: 'Япония', share: 8, volume: 99, avgPrice: 1720 }
    ])

    const updateSearchQuery = (value) => {
      searchQuery.value = value
    }

    const fetchSuggestions = () => {
      if (searchQuery.value.length > 2) {
        loading.value = true
        showSuggestions.value = true
        
        // Имитация API запроса
        setTimeout(() => {
          suggestions.value = mockProducts.filter(product => 
            product.name.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
            product.code.includes(searchQuery.value)
          )
          loading.value = false
        }, 500)
      } else {
        showSuggestions.value = false
        suggestions.value = []
      }
    }

    const selectSuggestion = (product) => {
      selectedProduct.value = product
      searchQuery.value = `${product.code} - ${product.name}`
      showSuggestions.value = false
    }

    const analyzeProduct = () => {
      if (selectedProduct.value) {
        analysisLoading.value = true
        showResults.value = true
        
        // Имитация анализа
        setTimeout(() => {
          recommendationData.value = {
            ...mockRecommendation,
            tnVedCode: selectedProduct.value.code,
            productName: selectedProduct.value.name
          }
          analysisLoading.value = false
        }, 2000)
      }
    }

    const toggleSection = (section) => {
      sections.value[section] = !sections.value[section]
    }

    return {
      searchQuery,
      showSuggestions,
      suggestions,
      selectedProduct,
      showResults,
      loading,
      analysisLoading,
      recommendationData,
      sections,
      suppliersData,
      updateSearchQuery,
      fetchSuggestions,
      selectSuggestion,
      analyzeProduct,
      toggleSection
    }
  }
}
</script>