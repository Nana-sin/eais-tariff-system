// Временные мок-данные для разработки, пока API не готово
export const mockProducts = [
  { code: '8542', name: 'Интегральные схемы', category: 'Электроника' },
  { code: '8703', name: 'Легковые автомобили', category: 'Автомобили' },
  { code: '2710', name: 'Нефть сырая', category: 'Нефтепродукты' },
  { code: '7207', name: 'Полуфабрикаты из железа', category: 'Металлургия' },
  { code: '0306', name: 'Креветки мороженые', category: 'Продукты питания' },
]

export const mockRecommendation = {
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
    },
    {
      id: 3,
      measureType: 'EAEU_REGULATION',
      measureName: 'Специальные меры ЕАЭС',
      applicable: false,
      score: 30.0,
      reasoning: 'Товар не подпадает под специальное регулирование ЕАЭС',
      details: 'Не применяется',
      importShare: 10.0,
      productionCapacity: 0.0,
      priceDifference: 5.0
    }
  ],
  createdAt: '2024-01-15T10:30:00Z',
  completedAt: '2024-01-15T10:35:00Z'
}

// Мок-функции для разработки
export const mockApi = {
  searchProducts: (query) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const results = mockProducts.filter(product =>
          product.name.toLowerCase().includes(query.toLowerCase()) ||
          product.code.includes(query)
        )
        resolve({ data: results })
      }, 500)
    })
  },

  getRecommendation: (productCode) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ data: { ...mockRecommendation, tnVedCode: productCode } })
      }, 1000)
    })
  }
}