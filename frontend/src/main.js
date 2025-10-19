import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)

// Глобальные обработчики ошибок
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue error:', err, info)
}

document.addEventListener('DOMContentLoaded', function() {
  app.mount('#app')
  
  // Инициализация feather icons
  if (typeof feather !== 'undefined') {
    setTimeout(() => {
      feather.replace()
    }, 100)
  }
})

// Fallback стили
const style = document.createElement('style')
style.textContent = `
  .bg-indigo-600 { background-color: #4f46e5 !important; }
  .text-indigo-600 { color: #4f46e5 !important; }
  .bg-gray-50 { background-color: #f9fafb !important; }
  .bg-white { background-color: #ffffff !important; }
  .shadow-sm { box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05) !important; }
  
  .vue-content {
    animation: fadeIn 0.5s ease-in;
  }
  
  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
  }
`
document.head.appendChild(style)