<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-xl shadow-md p-6 custom-shadow">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">Администрирование</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <!-- Статистика -->
          <div class="bg-gray-50 p-6 rounded-lg">
            <h3 class="text-lg font-semibold text-gray-800 mb-4">Статистика системы</h3>
            <div class="space-y-3">
              <div class="flex justify-between">
                <span class="text-gray-600">Всего анализов:</span>
                <span class="font-medium">156</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Активных пользователей:</span>
                <span class="font-medium">24</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Обновление данных:</span>
                <span class="font-medium">Сегодня 08:00</span>
              </div>
            </div>
          </div>

          <!-- Быстрые действия -->
          <div class="bg-gray-50 p-6 rounded-lg">
            <h3 class="text-lg font-semibold text-gray-800 mb-4">Быстрые действия</h3>
            <div class="space-y-2">
              <button @click="updateData" class="w-full text-left px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 flex items-center justify-between">
                <span>Обновить данные</span>
                <i data-feather="refresh-cw" class="w-4 h-4"></i>
              </button>
              <button @click="backupSystem" class="w-full text-left px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 flex items-center justify-between">
                <span>Создать резервную копию</span>
                <i data-feather="save" class="w-4 h-4"></i>
              </button>
              <button @click="showLogs" class="w-full text-left px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 flex items-center justify-between">
                <span>Просмотреть логи</span>
                <i data-feather="file-text" class="w-4 h-4"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- Настройки системы -->
        <div class="border-t border-gray-200 pt-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">Настройки системы</h3>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h4 class="font-medium text-gray-800">Автоматическое обновление данных</h4>
                <p class="text-sm text-gray-600">Ежедневное обновление в 08:00</p>
              </div>
              <button :class="autoUpdate ? 'bg-indigo-600' : 'bg-gray-200'" 
                      class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                      @click="autoUpdate = !autoUpdate">
                <span :class="autoUpdate ? 'translate-x-5' : 'translate-x-0'" 
                      class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out" />
              </button>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <h4 class="font-medium text-gray-800">Уведомления по email</h4>
                <p class="text-sm text-gray-600">Отправка отчетов на email</p>
              </div>
              <button :class="emailNotifications ? 'bg-indigo-600' : 'bg-gray-200'" 
                      class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                      @click="emailNotifications = !emailNotifications">
                <span :class="emailNotifications ? 'translate-x-5' : 'translate-x-0'" 
                      class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'Administration',
  setup() {
    const autoUpdate = ref(true)
    const emailNotifications = ref(false)

    const updateData = () => {
      alert('Запущено обновление данных системы...')
    }

    const backupSystem = () => {
      alert('Создание резервной копии системы...')
    }

    const showLogs = () => {
      alert('Открытие журнала системных логов...')
    }

    return {
      autoUpdate,
      emailNotifications,
      updateData,
      backupSystem,
      showLogs
    }
  }
}
</script>