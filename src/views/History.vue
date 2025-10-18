<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-xl shadow-md p-6 custom-shadow">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">История анализов</h2>
        
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Товар</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Код ТН ВЭД</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Дата анализа</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Статус</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Действия</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(item, index) in historyItems" :key="index">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-gray-900">{{ item.productName }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.code }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ item.date }}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" 
                        :class="statusClass(item.status)">
                    {{ item.status }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <button @click="viewAnalysis(item)" class="text-indigo-600 hover:text-indigo-900 mr-3">
                    Просмотреть
                  </button>
                  <button @click="deleteAnalysis(item)" class="text-red-600 hover:text-red-900">
                    Удалить
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="historyItems.length === 0" class="text-center py-8">
          <i data-feather="file-text" class="mx-auto h-12 w-12 text-gray-400"></i>
          <h3 class="mt-2 text-sm font-medium text-gray-900">Нет данных</h3>
          <p class="mt-1 text-sm text-gray-500">Вы еще не проводили анализ товаров.</p>
          <div class="mt-6">
            <router-link to="/" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700">
              <i data-feather="plus" class="mr-2 -ml-1 h-5 w-5"></i>
              Начать анализ
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'History',
  setup() {
    const router = useRouter()
    
    const historyItems = ref([
      {
        productName: 'Интегральные схемы',
        code: '8542',
        date: '2024-01-15',
        status: 'Завершен'
      },
      {
        productName: 'Легковые автомобили',
        code: '8703',
        date: '2024-01-10',
        status: 'Завершен'
      },
      {
        productName: 'Нефть сырая',
        code: '2710',
        date: '2024-01-05',
        status: 'В процессе'
      }
    ])

    const statusClass = (status) => {
      const classes = {
        'Завершен': 'bg-green-100 text-green-800',
        'В процессе': 'bg-yellow-100 text-yellow-800',
        'Ошибка': 'bg-red-100 text-red-800'
      }
      return classes[status] || 'bg-gray-100 text-gray-800'
    }

    const viewAnalysis = (item) => {
      // Переход на главную с предзаполненным поиском
      router.push({
        path: '/',
        query: { product: item.code }
      })
    }

    const deleteAnalysis = (item) => {
      if (confirm(`Удалить анализ для товара "${item.productName}"?`)) {
        historyItems.value = historyItems.value.filter(i => i.code !== item.code)
      }
    }

    return {
      historyItems,
      statusClass,
      viewAnalysis,
      deleteAnalysis
    }
  }
}
</script>