"""
API routes для ЕАИС - Единая Аналитическая Информационная Система
"""
from fastapi import APIRouter, HTTPException, Query
from typing import List, Optional
from pydantic import BaseModel

router = APIRouter()


# ==================== MODELS ====================

class ProductTNVED(BaseModel):
    """Модель товара по классификатору ТН ВЭД"""
    code: str
    name: str
    level: int
    parent_code: Optional[str] = None
    has_children: bool = False


class ProductSearchResponse(BaseModel):
    """Ответ на поиск товаров"""
    items: List[ProductTNVED]
    total: int


# ==================== DATABASE ====================

TNVED_DATABASE = {
    "84": {
        "code": "84",
        "name": "Реакторы ядерные, котлы, оборудование и механические устройства",
        "level": 2,
        "parent_code": None,
        "has_children": True
    },
    "33": {
        "code": "33",
        "name": "Эфирные масла и резиноиды; парфюмерные средства",
        "level": 2,
        "parent_code": None,
        "has_children": True
    },
    "8428": {
        "code": "8428",
        "name": "Машины для подъема, перемещения, погрузки",
        "level": 4,
        "parent_code": "84",
        "has_children": True
    },
    "8472": {
        "code": "8472",
        "name": "Оборудование конторское прочее",
        "level": 4,
        "parent_code": "84",
        "has_children": True
    },
    "3303": {
        "code": "3303",
        "name": "Духи и туалетная вода",
        "level": 4,
        "parent_code": "33",
        "has_children": True
    },
    "842810": {
        "code": "842810",
        "name": "Лифты и скиповые подъемники",
        "level": 6,
        "parent_code": "8428",
        "has_children": False
    },
    "847290": {
        "code": "847290",
        "name": "Банкоматы и прочее конторское оборудование",
        "level": 6,
        "parent_code": "8472",
        "has_children": False
    },
    "330300": {
        "code": "330300",
        "name": "Парфюмерные, косметические средства",
        "level": 6,
        "parent_code": "3303",
        "has_children": False
    },
}


# ==================== API ENDPOINTS ====================

@router.get("/products/search", response_model=ProductSearchResponse)
async def search_products_by_code(
    prefix: str = Query(..., description="Префикс кода ТН ВЭД", min_length=1)
):
    """Поиск товаров по префиксу кода"""
    matching_items = []

    for code, data in TNVED_DATABASE.items():
        if code.startswith(prefix):
            matching_items.append(ProductTNVED(**data))

    matching_items.sort(key=lambda x: x.code)

    return ProductSearchResponse(
        items=matching_items,
        total=len(matching_items)
    )


@router.get("/products/search/description")
async def search_products_by_description(
    query: str = Query(..., description="Поиск по названию", min_length=2)
):
    """Поиск товаров по описанию"""
    query_lower = query.lower()
    matching_items = []

    for code, data in TNVED_DATABASE.items():
        if query_lower in data["name"].lower():
            matching_items.append(ProductTNVED(**data))

    matching_items.sort(key=lambda x: x.code)

    return ProductSearchResponse(
        items=matching_items,
        total=len(matching_items)
    )


@router.get("/products/root", response_model=ProductSearchResponse)
async def get_root_products():
    """Получить корневые разделы ТН ВЭД"""
    root_items = []

    for code, data in TNVED_DATABASE.items():
        if data["level"] == 2:
            root_items.append(ProductTNVED(**data))

    root_items.sort(key=lambda x: x.code)

    return ProductSearchResponse(
        items=root_items,
        total=len(root_items)
    )


@router.get("/products/code/{tn_ved_code}", response_model=ProductTNVED)
async def get_product_by_code(tn_ved_code: str):
    """Получить товар по коду"""
    if tn_ved_code not in TNVED_DATABASE:
        raise HTTPException(
            status_code=404,
            detail=f"Товар с кодом '{tn_ved_code}' не найден"
        )
