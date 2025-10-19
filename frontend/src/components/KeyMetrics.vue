<template>
  <div class="p-6 border-b border-gray-200">
    <h4 class="text-lg font-semibold text-gray-800 mb-4">Ключевые показатели</h4>
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-white p-4 rounded-lg border border-gray-200">
        <div class="text-sm text-gray-500 mb-1">Объем импорта</div>
        <div class="text-2xl font-bold text-gray-800">1,240 т</div>
        <div class="text-xs text-green-600 flex items-center mt-1">
          <i data-feather="arrow-up" class="w-3 h-3 mr-1"></i>
          +12% за год
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg border border-gray-200">
        <div class="text-sm text-gray-500 mb-1">Средняя цена</div>
        <div class="text-2xl font-bold text-gray-800">$1,450</div>
        <div class="text-xs text-red-600 flex items-center mt-1">
          <i data-feather="arrow-down" class="w-3 h-3 mr-1"></i>
          -5% за год
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg border border-gray-200">
        <div class="text-sm text-gray-500 mb-1">Доля рынка</div>
        <div class="text-2xl font-bold text-gray-800">8.4%</div>
        <div class="text-xs text-green-600 flex items-center mt-1">
          <i data-feather="arrow-up" class="w-3 h-3 mr-1"></i>
          +1.2 п.п.
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg border border-gray-200">
        <div class="text-sm text-gray-500 mb-1">Кол-во поставщиков</div>
        <div class="text-2xl font-bold text-gray-800">24</div>
        <div class="text-xs text-gray-500 mt-1">из 6 стран</div>
      </div>
    </div>
    <div class="mt-3 text-xs text-gray-500">
      Источник: UN Comtrade, период: 2022–2024
    </div>
  </div>
</template>

<script>
export default {
  name: 'KeyMetrics'
}
</script>