<div align="center">

# 🏛️ ЕАИС - Единая Аналитическая Информационная Система

### Система для оценки эффективности мер таможенно-тарифного регулирования

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Python](https://img.shields.io/badge/python-3.11+-blue.svg)](https://www.python.org/)
[![Vue.js](https://img.shields.io/badge/vue.js-3.4+-brightgreen.svg)](https://vuejs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.104+-009688.svg)](https://fastapi.tiangolo.com/)
[![Docker](https://img.shields.io/badge/docker-ready-2496ED.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/kubernetes-ready-326CE5.svg)](https://kubernetes.io/)

[Быстрый старт](#-быстрый-старт) •
[Возможности](#-возможности) •
[Документация](#-документация) •
[Архитектура](#️-архитектура) •
[Разработка](#-разработка)

</div>

---

## 📋 Описание

**ЕАИС** - современная веб-платформа для комплексного анализа и оценки эффективности торгово-политических мер в области таможенно-тарифного регулирования. Система предоставляет инструменты для анализа импорта, классификации товаров по ТН ВЭД и формирования рекомендаций по применению защитных мер.

### 🎯 Основные возможности

- 📊 **Интерактивный анализ** - визуализация торговых данных с помощью динамических графиков
- 🔍 **Классификация товаров** - работа со справочником ТН ВЭД ЕАЭС
- 🌍 **Анализ по странам** - оценка импорта из дружественных и недружественных стран
- 📈 **Рекомендательная система** - алгоритмический подбор оптимальных защитных мер
- 🔐 **Многопользовательский режим** - разграничение прав доступа (аналитик, эксперт, администратор)
- 📱 **Адаптивный интерфейс** - работа на любых устройствах
- 🚀 **Микросервисная архитектура** - масштабируемость и отказоустойчивость

---

## 🏗️ Архитектура

Система построена на современном технологическом стеке с микросервисной архитектурой:

```
┌─────────────────────────────────────────────────────────┐
│                    Nginx Ingress                        │
└────────────────┬────────────────────────┬───────────────┘
                 │                        │
        ┌────────▼────────┐      ┌────────▼─────────┐
        │   Frontend      │      │    Backend       │
        │   (Vue.js)      │◄────►│   (FastAPI)      │
        │   + Nginx       │      │   + Uvicorn      │
        └─────────────────┘      └──────┬───────────┘
                                        │
                                 ┌──────▼───────┐
                                 │ PostgreSQL   │
                                 │   (future)   │
                                 └──────────────┘
```

### 🛠️ Технологический стек

#### Frontend
- **Vue.js 3.4** - прогрессивный JavaScript фреймворк
- **Vite 5.0** - современный сборщик с hot module replacement
- **Vue Router 4.2** - маршрутизация SPA
- **Chart.js 4.4** - визуализация данных
- **Tailwind CSS 3.4** - utility-first CSS фреймворк
- **Axios** - HTTP клиент
- **Nginx** - веб-сервер для production

#### Backend
- **Python 3.11+** - основной язык программирования
- **FastAPI 0.104** - высокопроизводительный веб-фреймворк
- **Uvicorn** - ASGI сервер
- **Gunicorn** - process manager для production
- **Pandas** - анализ данных
- **Pydantic** - валидация данных
- **OpenPyXL** - работа с Excel файлами

#### Infrastructure
- **Docker** - контейнеризация
- **Docker Compose** - оркестрация контейнеров
- **Kubernetes** - оркестрация в production
- **Nginx Ingress** - маршрутизация трафика
- **HPA** - автоматическое масштабирование

---

## 🚀 Быстрый старт

### Предварительные требования

- [Docker](https://www.docker.com/) 24.0+
- [Docker Compose](https://docs.docker.com/compose/) 2.0+
- [Git](https://git-scm.com/)

Для Kubernetes:
- [kubectl](https://kubernetes.io/docs/tasks/tools/) 1.28+
- Kubernetes кластер (minikube, k3s, или облачный провайдер)

### ⚡ Установка за 3 шага

```bash
# 1. Клонируйте репозиторий
git clone https://github.com/Nana-sin/eais-tariff-system.git
cd eais-tariff-system

# 2. Запустите приложение
docker-compose up -d

# 3. Откройте в браузере
# Frontend:  http://localhost:5173
# Backend:   http://localhost:8000
# API Docs:  http://localhost:8000/docs
```

### 🎥 Демо

После запуска приложение будет доступно:

- **Frontend**: http://localhost:5173 - интерфейс пользователя
- **Backend API**: http://localhost:8000 - REST API сервер
- **API Documentation**: http://localhost:8000/docs - интерактивная документация Swagger
- **ReDoc**: http://localhost:8000/redoc - альтернативная документация

---

## 📚 Документация

### Структура проекта

```
eais-tariff-system/
├── 📁 backend/                 # Python FastAPI бэкенд
│   ├── app/
│   │   └── main.py            # Точка входа приложения
│   ├── Dockerfile             # Production образ
│   ├── Dockerfile.dev         # Development образ
│   └── requirements.txt       # Python зависимости
│
├── 📁 frontend/               # Vue.js фронтенд
│   ├── src/
│   │   ├── api/              # API клиент
│   │   ├── components/       # Vue компоненты
│   │   ├── views/            # Страницы приложения
│   │   └── router/           # Роутинг
│   ├── Dockerfile            # Production образ
│   ├── Dockerfile.dev        # Development образ
│   ├── nginx.conf            # Nginx конфигурация
│   └── package.json          # Node.js зависимости
│
├── 📁 k8s/                    # Kubernetes манифесты
│   ├── namespace.yaml
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   ├── ingress.yaml
│   └── hpa.yaml              # Auto-scaling
│
├── 📁 docs/                   # Документы и данные
│   ├── bancomats.xlsx        # Данные по банкоматам
│   ├── lifts.xlsx            # Данные по лифтам
│   └── parfums.xlsx          # Данные по парфюмерии
│
├── docker-compose.yml        # Development окружение
├── docker-compose.prod.yml   # Production окружение
└── .env.example              # Пример переменных окружения
```

### 🔧 Конфигурация

Создайте `.env` файл на основе `.env.example`:

```bash
cp .env.example .env
```

Основные переменные окружения:

```env
# Backend
ENVIRONMENT=development
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080

# Frontend
VITE_API_URL=http://localhost:8000

# Security (измените в production!)
SECRET_KEY=your-secret-key-here
JWT_SECRET=your-jwt-secret-here
```

---

## 💻 Разработка

### Локальная разработка

```bash
# Запуск в режиме разработки с hot reload
docker-compose up

# Просмотр логов
docker-compose logs -f

# Просмотр логов конкретного сервиса
docker-compose logs -f backend
docker-compose logs -f frontend

# Перезапуск сервиса
docker-compose restart backend

# Остановка
docker-compose down
```

### Разработка без Docker

#### Backend

```bash
cd backend

# Создание виртуального окружения
python -m venv venv
source venv/bin/activate  # Linux/Mac
# или
venv\Scripts\activate     # Windows

# Установка зависимостей
pip install -r requirements.txt

# Запуск сервера
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

#### Frontend

```bash
cd frontend

# Установка зависимостей
npm install
# или
pnpm install

# Запуск dev сервера
npm run dev
# или
pnpm dev
```

### 🧪 Тестирование

```bash
# Backend тесты
docker-compose exec backend pytest

# Frontend тесты
docker-compose exec frontend npm run test

# Линтинг
docker-compose exec frontend npm run lint
```

---

## 🏭 Production развертывание

### Docker Compose

```bash
# Запуск production версии
docker-compose -f docker-compose.prod.yml up -d

# Проверка статуса
docker-compose -f docker-compose.prod.yml ps

# Просмотр логов
docker-compose -f docker-compose.prod.yml logs -f
```

### Kubernetes

```bash
# Применение всех манифестов
kubectl apply -f k8s/

# Проверка статуса
kubectl get all -n eais-system

# Просмотр логов
kubectl logs -f deployment/backend-deployment -n eais-system

# Масштабирование
kubectl scale deployment backend-deployment --replicas=5 -n eais-system
```

Подробная инструкция по развертыванию в [docs/deployment.md](docs/deployment.md)

---

## 🎨 Особенности интерфейса

### Страницы приложения

- **🏠 Главная** - обзор системы и быстрый старт анализа
- **📊 Анализ данных** - интерактивные графики и таблицы
- **🔍 Поиск товаров** - навигация по справочнику ТН ВЭД
- **📋 История запросов** - архив проведенных анализов
- **👥 Экспертные мнения** - оценка рекомендаций специалистами
- **⚙️ Администрирование** - управление системой и пользователями

### Функциональные возможности

#### Для аналитиков
- Создание запросов на анализ товаров
- Выбор кода ТН ВЭД и страны-импортёра
- Просмотр детального анализа с визуализацией
- Получение рекомендаций по защитным мерам

#### Для экспертов
- Рассмотрение запросов от аналитиков
- Утверждение или отклонение рекомендаций
- Добавление экспертных комментариев
- Просмотр статистики по решениям

#### Для администраторов
- Управление пользователями и правами доступа
- Мониторинг работы системы
- Обновление справочников
- Настройка параметров алгоритма

---

## 📈 API документация

API построен на FastAPI с автоматической генерацией документации.

### Основные endpoints

#### Аутентификация
```http
POST /api/v1/auth/register     # Регистрация пользователя
POST /api/v1/auth/login         # Вход в систему
```

#### Рекомендации
```http
POST   /api/v1/recommendations          # Создать запрос на анализ
GET    /api/v1/recommendations/{id}     # Получить рекомендацию
GET    /api/v1/recommendations/user/{id} # Рекомендации пользователя
```

#### Классификация товаров
```http
GET    /api/v1/products/search          # Поиск по коду ТН ВЭД
GET    /api/v1/products/code/{code}     # Получить товар по коду
GET    /api/v1/products/root            # Получить корневые разделы
```

Полная документация: http://localhost:8000/docs

---

## 🔐 Безопасность

- ✅ JWT аутентификация для API
- ✅ CORS настройки для защиты от XSS
- ✅ Валидация входных данных с Pydantic
- ✅ Секреты через Kubernetes Secrets
- ✅ HTTPS через Nginx Ingress (production)
- ✅ Непривилегированные пользователи в Docker контейнерах
- ✅ Security headers в Nginx

---

## 🤝 Участие в разработке

Мы приветствуем вклад в проект! 

### Как внести вклад

1. **Fork** репозитория
2. Создайте **feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit** изменения: `git commit -m 'Add amazing feature'`
4. **Push** в ветку: `git push origin feature/amazing-feature`
5. Откройте **Pull Request**

### Правила разработки

- Следуйте PEP 8 для Python кода
- Используйте ESLint/Prettier для JavaScript
- Пишите тесты для новых функций
- Обновляйте документацию
- Пишите понятные commit сообщения

---

## 🐛 Troubleshooting

### Backend не запускается

```bash
# Проверьте логи
docker-compose logs backend

# Проверьте health endpoint
curl http://localhost:8000/health
```

### Frontend не подключается к Backend

```bash
# Проверьте переменные окружения
docker-compose exec frontend env | grep VITE

# Проверьте CORS настройки
docker-compose exec backend env | grep ALLOWED_ORIGINS
```

### Проблемы с Docker

```bash
# Очистка и пересборка
docker-compose down
docker system prune -f
docker-compose build --no-cache
docker-compose up -d
```

Больше решений в [docs/troubleshooting.md](docs/troubleshooting.md)

---

## 📊 Статус проекта

- ✅ Backend API (FastAPI)
- ✅ Frontend UI (Vue.js)
- ✅ Docker контейнеризация
- ✅ Kubernetes манифесты
- ✅ Базовая аутентификация
- ✅ Анализ импорта
- ✅ Справочник ТН ВЭД
- 🔄 PostgreSQL интеграция (в разработке)
- 🔄 Расширенная авторизация (в разработке)
- 📅 Экспорт отчётов (планируется)
- 📅 Email уведомления (планируется)

---

## 🗺️ Roadmap

### v1.1 (Q1 2026)
- [ ] PostgreSQL база данных
- [ ] JWT refresh tokens
- [ ] Расширенная аналитика
- [ ] Экспорт в Excel/PDF
- [ ] Email уведомления

### v2.0 (Q2 2026)
- [ ] Машинное обучение для прогнозирования
- [ ] Интеграция с внешними API (ФТС, ЕЭК)
- [ ] Многоязычность (EN, RU, KZ)
- [ ] Мобильное приложение
- [ ] Продвинутая визуализация (D3.js)

---

## 📄 Лицензия

Проект распространяется под лицензией MIT. См. файл [LICENSE](LICENSE) для подробностей.

---

## 👥 Авторы

- **Nana-sin** - *Создатель и главный разработчик* - [GitHub](https://github.com/Nana-sin)

---

## 🙏 Благодарности

Проект использует следующие open-source технологии:

- [FastAPI](https://fastapi.tiangolo.com/) - современный Python веб-фреймворк
- [Vue.js](https://vuejs.org/) - прогрессивный JavaScript фреймворк
- [Docker](https://www.docker.com/) - платформа контейнеризации
- [Kubernetes](https://kubernetes.io/) - система оркестрации контейнеров
- [Nginx](https://nginx.org/) - высокопроизводительный веб-сервер
- [Chart.js](https://www.chartjs.org/) - библиотека визуализации
- [Tailwind CSS](https://tailwindcss.com/) - utility-first CSS фреймворк

---

## 📞 Контакты и поддержка

- 📧 **Issues**: [GitHub Issues](https://github.com/Nana-sin/eais-tariff-system/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/Nana-sin/eais-tariff-system/discussions)
- 📖 **Wiki**: [Документация](https://github.com/Nana-sin/eais-tariff-system/wiki)

---

<div align="center">

### ⭐ Если проект полезен, поставьте звезду!

**Разработано с ❤️ для эффективного таможенно-тарифного регулирования**

[![GitHub stars](https://img.shields.io/github/stars/Nana-sin/eais-tariff-system?style=social)](https://github.com/Nana-sin/eais-tariff-system/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Nana-sin/eais-tariff-system?style=social)](https://github.com/Nana-sin/eais-tariff-system/network/members)
[![GitHub watchers](https://img.shields.io/github/watchers/Nana-sin/eais-tariff-system?style=social)](https://github.com/Nana-sin/eais-tariff-system/watchers)

</div>
