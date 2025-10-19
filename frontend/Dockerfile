# frontend/Dockerfile
 # Multi-stage build для минимизации размера образа

 # Stage 1: Build
 FROM node:20-alpine AS builder

 WORKDIR /app

 # Установка pnpm для быстрой установки зависимостей
 RUN npm install -g pnpm

 # Копирование файлов зависимостей
 COPY package*.json ./
 COPY pnpm-lock.yaml* ./

 # Установка зависимостей
 RUN pnpm install --frozen-lockfile

 # Копирование исходного кода
 COPY . .

 # Build аргументы
 ARG VITE_API_URL=http://localhost:8000
 ENV VITE_API_URL=$VITE_API_URL

 # Сборка приложения
 RUN pnpm run build

 # Stage 2: Production with Nginx
 FROM nginx:1.25-alpine

 # Копирование собранного приложения
 COPY --from=builder /app/dist /usr/share/nginx/html
 # Копирование конфигурации Nginx
 COPY nginx.conf /etc/nginx/conf.d/default.conf

 # Настройка прав доступа
 RUN chown -R nginx:nginx /usr/share/nginx/html &amp;&amp; \
     chmod -R 755 /usr/share/nginx/html &amp;&amp; \
     chown -R nginx:nginx /var/cache/nginx &amp;&amp; \
     chown -R nginx:nginx /var/log/nginx &amp;&amp; \
     touch /var/run/nginx.pid &amp;&amp; \
     chown -R nginx:nginx /var/run/nginx.pid

 # Переключение на непривилегированного пользователя
 USER nginx

 EXPOSE 8080

 # Health check
 HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
     CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

 CMD ["nginx", "-g", "daemon off;"]
