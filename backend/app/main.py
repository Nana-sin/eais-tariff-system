from fastapi import FastAPI
 from fastapi.middleware.cors import CORSMiddleware
 from app.api import routes
 import os

 app = FastAPI(
     title="EAIS Tariff System API",
     description="Единая Аналитическая Информационная Система",
     version="1.0.0"
 )
# CORS настройки для взаимодействия с фронтендом
 app.add_middleware(
     CORSMiddleware,
     allow_origins=os.getenv("ALLOWED_ORIGINS", "*").split(","),
     allow_credentials=True,
     allow_methods=["*"],
     allow_headers=["*"],
 )

 # Подключение роутов
 app.include_router(routes.router, prefix="/api/v1")

 @app.get("/health")
 async def health_check():
     return {"status": "healthy"}

 @app.get("/")
 async def root():
     return {"message": "EAIS API is running"}
