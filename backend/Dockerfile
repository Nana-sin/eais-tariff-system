# backend/Dockerfile
 # Multi-stage build для оптимизации размера образа

 # Stage 1: Builder
 FROM python:3.11-slim AS builder

 WORKDIR /build

 # Установка системных зависимостей
 RUN apt-get update &amp;&amp; apt-get install -y --no-install-recommends \
     gcc \
     &amp;&amp; rm -rf /var/lib/apt/lists/*

 # Копирование requirements и установка зависимостей
 COPY requirements.txt .
 RUN pip install --no-cache-dir --user -r requirements.txt

 # Stage 2: Production
 FROM python:3.11-slim
 WORKDIR /app

 # Копирование установленных пакетов из builder
 COPY --from=builder /root/.local /root/.local

 # Добавление PATH для pip packages
 ENV PATH=/root/.local/bin:$PATH

 # Копирование приложения
 COPY ./app /app/app
 COPY *.xlsx /app/

 # Создание непривилегированного пользователя
 RUN useradd -m -u 1000 appuser &amp;&amp; \
     chown -R appuser:appuser /app

 USER appuser

 # Expose порт
 EXPOSE 8000

 # Health check
 HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
     CMD python -c "import requests; requests.get('http://localhost:8000/health')"

 # Запуск с Gunicorn + Uvicorn workers
 CMD ["gunicorn", "app.main:app", \
      "--workers", "4", \
      "--worker-class", "uvicorn.workers.UvicornWorker", \
      "--bind", "0.0.0.0:8000", \
      "--access-logfile", "-", \
      "--error-logfile", "-"]
