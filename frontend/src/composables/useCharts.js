import { onMounted } from 'vue'
import Chart from 'chart.js/auto'

export function useCharts() {
  const initImportTrendsChart = (ctx) => {
    if (!ctx) return

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Янв 2022', 'Апр 2022', 'Июл 2022', 'Окт 2022', 'Янв 2023', 'Апр 2023', 'Июл 2023', 'Окт 2023'],
        datasets: [{
          label: 'Объем импорта (тонн)',
          data: [85, 92, 78, 105, 115, 120, 110, 135],
          borderColor: '#4F46E5',
          backgroundColor: 'rgba(79, 70, 229, 0.05)',
          borderWidth: 2,
          fill: true,
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
          },
          tooltip: {
            mode: 'index',
            intersect: false,
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            grid: {
              drawBorder: false,
            }
          },
          x: {
            grid: {
              display: false,
            }
          }
        }
      }
    })
  }

  const initPriceAnalysisChart = (ctx) => {
    if (!ctx) return

    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Китай', 'Германия', 'США', 'Турция', 'Япония'],
        datasets: [{
          label: 'Средняя цена ($)',
          data: [1420, 1580, 1650, 1320, 1720],
          backgroundColor: [
            'rgba(79, 70, 229, 0.7)',
            'rgba(79, 70, 229, 0.6)',
            'rgba(79, 70, 229, 0.5)',
            'rgba(79, 70, 229, 0.4)',
            'rgba(79, 70, 229, 0.3)'
          ],
          borderColor: [
            'rgba(79, 70, 229, 1)',
            'rgba(79, 70, 229, 1)',
            'rgba(79, 70, 229, 1)',
            'rgba(79, 70, 229, 1)',
            'rgba(79, 70, 229, 1)'
          ],
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            grid: {
              drawBorder: false,
            }
          },
          x: {
            grid: {
              display: false,
            }
          }
        }
      }
    })
  }

  return {
    initImportTrendsChart,
    initPriceAnalysisChart
  }
}