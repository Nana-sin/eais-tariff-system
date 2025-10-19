import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import History from '../views/History.vue'
import ExpertOpinions from '../views/ExpertOpinions.vue'
import Administration from '../views/Administration.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: 'Главная - TradeMind Analytica Pro'
    }
  },
  {
    path: '/history',
    name: 'History',
    component: History,
    meta: {
      title: 'История анализов - TradeMind Analytica Pro'
    }
  },
  {
    path: '/expert-opinions',
    name: 'ExpertOpinions',
    component: ExpertOpinions,
    meta: {
      title: 'Экспертные заключения - TradeMind Analytica Pro'
    }
  },
  {
    path: '/administration',
    name: 'Administration',
    component: Administration,
    meta: {
      title: 'Администрирование - TradeMind Analytica Pro'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// Глобальный хук для изменения title страницы
router.beforeEach((to, from, next) => {
  const title = to.meta.title || 'TradeMind Analytica Pro'
  document.title = title
  next()
})

export default router