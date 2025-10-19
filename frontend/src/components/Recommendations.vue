<template>
  <div class="p-6 border-b border-gray-200">
    <h4 class="text-lg font-semibold text-gray-800 mb-4">Рекомендованные меры</h4>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="bg-blue-50 p-4 rounded-lg">
        <div class="flex items-start space-x-3">
          <div class="flex-shrink-0 bg-blue-100 p-2 rounded-full">
            <i data-feather="alert-triangle" class="text-blue-600 w-5 h-5"></i>
          </div>
          <div>
            <h5 class="font-medium text-blue-800">Риск снижения цен</h5>
            <p class="text-blue-700 text-sm mt-1">Рекомендуем рассмотреть возможность заключения долгосрочных контрактов</p>
          </div>
        </div>
      </div>
      <div class="bg-yellow-50 p-4 rounded-lg">
        <div class="flex items-start space-x-3">
          <div class="flex-shrink-0 bg-yellow-100 p-2 rounded-full">
            <i data-feather="bar-chart-2" class="text-yellow-600 w-5 h-5"></i>
          </div>
          <div>
            <h5 class="font-medium text-yellow-800">Оптимизация логистики</h5>
            <p class="text-yellow-700 text-sm mt-1">Рассмотреть альтернативные маршруты поставок через Казахстан</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Recommendations'
}
</script>