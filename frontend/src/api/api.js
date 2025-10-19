import axios from 'axios'

// Базовый URL API
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// Создаем экземпляр axios
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Интерцептор для добавления токена
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Интерцептор для обработки ошибок
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Токен истек или невалидный
      localStorage.removeItem('authToken')
      localStorage.removeItem('userData')
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

// API методы
export const api = {
  // Аутентификация
  auth: {
    register: (userData) => 
      apiClient.post('/api/v1/auth/register', userData),
    
    login: (credentials) => 
      apiClient.post('/api/v1/auth/login', credentials)
  },

  // Рекомендации мер ТТП
  recommendations: {
    create: (recommendationData) => 
      apiClient.post('/api/v1/recommendations', recommendationData),
    
    getById: (requestId) => 
      apiClient.get(`/api/v1/recommendations/${requestId}`),
    
    getByUser: (userId, pageable = { page: 0, size: 10 }) => 
      apiClient.get(`/api/v1/recommendations/user/${userId}`, { params: pageable }),
    
    getByStatus: (status, pageable = { page: 0, size: 10 }) => 
      apiClient.get(`/api/v1/recommendations/status/${status}`, { params: pageable })
  },

  // Классификации товаров
  classifications: {
    create: (classificationData, userId) => 
      apiClient.post('/api/v1/classifications', classificationData, {
        headers: { 'X-User-Id': userId }
      }),
    
    getById: (id) => 
      apiClient.get(`/api/v1/classifications/${id}`),
    
    getByUser: (userId, pageable = { page: 0, size: 10 }) => 
      apiClient.get(`/api/v1/classifications/user/${userId}`, { params: pageable }),
    
    getByStatus: (status, pageable = { page: 0, size: 10 }) => 
      apiClient.get(`/api/v1/classifications/status/${status}`, { params: pageable }),
    
    approve: (id, userId, comment = '') => 
      apiClient.post(`/api/v1/classifications/${id}/approve`, null, {
        headers: { 'X-User-Id': userId },
        params: { comment }
      }),
    
    reject: (id, userId, comment) => 
      apiClient.post(`/api/v1/classifications/${id}/reject`, null, {
        headers: { 'X-User-Id': userId },
        params: { comment }
      })
  },

  // Справочник товаров ТН ВЭД
  products: {
    searchByPrefix: (prefix) => 
      apiClient.get('/api/v1/products/search', { params: { prefix } }),
    
    searchByDescription: (query) => 
      apiClient.get('/api/v1/products/search/description', { params: { query } }),
    
    getRoot: () => 
      apiClient.get('/api/v1/products/root'),
    
    getByCode: (tnVedCode) => 
      apiClient.get(`/api/v1/products/code/${tnVedCode}`),
    
    getChildren: (parentCode) => 
      apiClient.get(`/api/v1/products/children/${parentCode}`)
  }
}

// Вспомогательные функции
export const authHelper = {
  // Сохранение данных аутентификации
  setAuthData: (token, userData) => {
    localStorage.setItem('authToken', token)
    localStorage.setItem('userData', JSON.stringify(userData))
  },

  // Получение данных пользователя
  getUserData: () => {
    const userData = localStorage.getItem('userData')
    return userData ? JSON.parse(userData) : null
  },

  // Получение токена
  getToken: () => {
    return localStorage.getItem('authToken')
  },

  // Проверка авторизации
  isAuthenticated: () => {
    return !!localStorage.getItem('authToken')
  },

  // Выход
  logout: () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('userData')
  },

  // Проверка роли
  hasRole: (role) => {
    const userData = authHelper.getUserData()
    return userData?.role === role
  }
}

export default api