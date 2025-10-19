import pandas as pd
import numpy as np
from typing import Dict, Tuple, List

class InteractiveTradeAnalysis:
    def __init__(self):
        # Список недружественных стран (согласно распоряжению Правительства РФ №430-р)
        self.unfriendly_countries = [
            'United States', 'Canada', 'United Kingdom', 
            'European Union Nes', 'Germany', 'France', 'Italy', 'Spain',
            'Netherlands', 'Belgium', 'Poland', 'Sweden', 'Finland',
            'Denmark', 'Ireland', 'Austria', 'Portugal', 'Greece',
            'Czech Republic', 'Hungary', 'Slovakia', 'Slovenia',
            'Lithuania', 'Latvia', 'Estonia', 'Malta', 'Cyprus',
            'Luxembourg', 'Croatia', 'Romania', 'Bulgaria',
            'Japan', 'South Korea', 'Australia', 'New Zealand',
            'Norway', 'Switzerland', 'Ukraine', 'Montenegro',
            'Albania', 'Iceland', 'Liechtenstein'
        ]
        
        # База данных товаров
        self.goods_database = {
            '842810': {
                'name': 'Лифты',
                'current_rate': 0,
                'wto_rate': 0.05,
                'production_2024': 307,
                'consumption_2024': 306,
                'certification': 'да',
                'government_procurement': 'да',
                'order_4114': 'нет',
                'data_file': 'лифты.xlsx'
            },
            '330300': {
                'name': 'Парфюмерия', 
                'current_rate': 0.065,
                'wto_rate': 0.065,
                'production_2024': 657,
                'consumption_2024': 576,
                'certification': 'нет',
                'government_procurement': 'нет',
                'order_4114': 'нет',
                'data_file': 'парфюмерия.xlsx'
            },
            '847290': {
                'name': 'Банкоматы',
                'current_rate': 0,
                'wto_rate': 0,
                'production_2024': 224,
                'consumption_2024': 217,
                'certification': 'нет',
                'government_procurement': 'нет',
                'order_4114': 'нет',
                'data_file': 'банкоматы.xlsx'
            }
        }

    def get_user_input(self):
        """Получение данных от пользователя"""
        print("=" * 60)
        print("АНАЛИЗ ТОРГОВО-ПОЛИТИЧЕСКИХ МЕР")
        print("=" * 60)
        
        # Вывод доступных товаров
        print("\nДоступные товары для анализа:")
        for code, info in self.goods_database.items():
            print(f"Код ТН ВЭД: {code} - {info['name']}")
        
        # Ввод кода ТН ВЭД
        while True:
            tnved_code = input("\nВведите код ТН ВЭД (6 цифр): ").strip()
            if tnved_code in self.goods_database:
                break
            else:
                print("Ошибка: код не найден в базе данных. Попробуйте снова.")
        
        # Ввод страны для анализа
        country = input("Введите название страны для анализа: ").strip()
        
        return tnved_code, country

    def load_import_data(self, file_path: str) -> Tuple[pd.DataFrame, pd.DataFrame]:
        """Загрузка данных об импорте из Excel файла"""
        try:
            # Чтение стоимостных данных
            value_data = pd.read_excel(file_path, sheet_name=0)
            # Чтение весовых данных  
            weight_data = pd.read_excel(file_path, sheet_name=1)
            
            return value_data, weight_data
        except Exception as e:
            print(f"Ошибка при загрузке данных: {e}")
            return None, None

    def classify_country(self, country: str) -> str:
        """Классификация страны на дружественную или недружественную"""
        if country in self.unfriendly_countries or any(unfriendly in country for unfriendly in self.unfriendly_countries):
            return "недружественная"
        else:
            return "дружественная"

    def analyze_specific_country(self, value_data: pd.DataFrame, weight_data: pd.DataFrame, country: str) -> Dict:
        """Анализ данных по конкретной стране"""
        result = {}
        
        # Проверяем наличие страны в данных
        country_value_data = value_data[value_data.iloc[:, 0] == country]
        country_weight_data = weight_data[weight_data.iloc[:, 0] == country]
        
        if country_value_data.empty:
            result['found'] = False
            return result
        
        result['found'] = True
        
        # Получаем данные по годам
        result['import_2022'] = country_value_data.iloc[0, 1] if country_value_data.shape[1] > 1 else 0
        result['import_2023'] = country_value_data.iloc[0, 2] if country_value_data.shape[1] > 2 else 0
        result['import_2024'] = country_value_data.iloc[0, 3] if country_value_data.shape[1] > 3 else 0
        
        # Расчет динамики
        if result['import_2023'] > 0:
            result['growth_2024_vs_2023'] = ((result['import_2024'] - result['import_2023']) / result['import_2023']) * 100
        else:
            result['growth_2024_vs_2023'] = 0
        
        # Расчет средней цены
        if not country_weight_data.empty and country_weight_data.iloc[0, 3] > 0:
            result['unit_price_2024'] = result['import_2024'] / country_weight_data.iloc[0, 3]
        else:
            result['unit_price_2024'] = 0
        
        # Классификация страны
        result['classification'] = self.classify_country(country)
        
        return result

    def calculate_overall_metrics(self, value_data: pd.DataFrame, weight_data: pd.DataFrame) -> Dict:
        """Расчет общих метрик импорта"""
        # Получаем список стран (исключаем World и пустые значения)
        countries = [c for c in value_data.iloc[:, 0].dropna().unique() 
                    if c != 'World' and c != 'Список стран-продавцов в Россию']
        
        # Суммируем импорт по группам за 2024 год
        total_import_2024 = value_data[value_data.iloc[:, 0] == 'World'].iloc[0, 3]
        
        unfriendly_import_2024 = 0
        friendly_import_2024 = 0
        
        for country in countries:
            country_data = value_data[value_data.iloc[:, 0] == country]
            if not country_data.empty:
                import_value = country_data.iloc[0, 3]
                if self.classify_country(country) == "недружественная":
                    unfriendly_import_2024 += import_value
                else:
                    friendly_import_2024 += import_value
        
        # Расчет доли недружественных стран
        unfriendly_share = (unfriendly_import_2024 / total_import_2024) * 100 if total_import_2024 > 0 else 0
        
        # Расчет динамики импорта из недружественных стран
        unfriendly_import_2023 = 0
        for country in countries:
            if self.classify_country(country) == "недружественная":
                country_data = value_data[value_data.iloc[:, 0] == country]
                if not country_data.empty and country_data.shape[1] > 2:
                    unfriendly_import_2023 += country_data.iloc[0, 2]
        
        import_growth = ((unfriendly_import_2024 - unfriendly_import_2023) / unfriendly_import_2023 * 100 
                        if unfriendly_import_2023 > 0 else 0)
        
        return {
            'unfriendly_share': unfriendly_share,
            'import_growth': import_growth,
            'total_import_2024': total_import_2024,
            'unfriendly_import_2024': unfriendly_import_2024
        }

    def apply_algorithm(self, tnved_code: str, overall_metrics: Dict) -> str:
        """Применение алгоритма для определения меры"""
        goods_info = self.goods_database[tnved_code]
        
        # Шаг 4: Анализ доли недружественных стран
        if overall_metrics['unfriendly_share'] >= 30 and overall_metrics['import_growth'] >= 0:
            # Шаг 4.1
            if goods_info['production_2024'] >= goods_info['consumption_2024']:
                return "Мера 2"
            else:
                return "Мера 6"
        else:
            # Шаг 4.2
            if goods_info['current_rate'] < goods_info['wto_rate']:
                if goods_info['production_2024'] >= goods_info['consumption_2024']:
                    return "Мера 1"
                else:
                    return "Мера 6"
            elif goods_info['current_rate'] == goods_info['wto_rate']:
                if goods_info['production_2024'] < goods_info['consumption_2024']:
                    return "Мера 6"  # Упрощенная логика для демонстрации
                else:
                    # Переход к нетарифным мерам
                    if goods_info['production_2024'] < goods_info['consumption_2024']:
                        return "Мера 6"
                    else:
                        if goods_info['government_procurement'] == 'да':
                            return "Мера 6"
                        else:
                            if (goods_info['certification'] == 'да' and 
                                goods_info['order_4114'] == 'нет'):
                                return "Мера 5"
                            else:
                                return "Мера 4"
        
        return "Мера 6"

    def run_analysis(self):
        """Запуск интерактивного анализа"""
        # Получение данных от пользователя
        tnved_code, country = self.get_user_input()
        
        goods_info = self.goods_database[tnved_code]
        
        print(f"\nАнализ для товара: {goods_info['name']} (код ТН ВЭД: {tnved_code})")
        print(f"Анализируемая страна: {country}")
        
        # Загрузка данных импорта
        value_data, weight_data = self.load_import_data(goods_info['data_file'])
        
        if value_data is None:
            print("Ошибка: не удалось загрузить данные импорта")
            return
        
        # Анализ конкретной страны
        country_analysis = self.analyze_specific_country(value_data, weight_data, country)
        
        # ПРЕКРАЩЕНИЕ АНАЛИЗА ЕСЛИ СТРАНА НЕ НАЙДЕНА
        if not country_analysis['found']:
            print(f"\n❌ ОШИБКА: Страна '{country}' не найдена в данных импорта!")
            print("Анализ невозможен. Пожалуйста, проверьте правильность написания названия страны.")
            return
        
        # Расчет общих метрик (только если страна найдена)
        overall_metrics = self.calculate_overall_metrics(value_data, weight_data)
        
        # Применение алгоритма (только если страна найдена)
        recommended_measure = self.apply_algorithm(tnved_code, overall_metrics)
        
        # Вывод результатов (только если страна найдена)
        self.display_results(goods_info, country, country_analysis, overall_metrics, recommended_measure)

    def display_results(self, goods_info: Dict, country: str, country_analysis: Dict, 
                       overall_metrics: Dict, measure: str):
        """Отображение результатов анализа"""
        print("\n" + "="*80)
        print("РЕЗУЛЬТАТЫ АНАЛИЗА")
        print("="*80)
        
        print(f"\nТовар: {goods_info['name']}")
        print(f"Код ТН ВЭД: {list(self.goods_database.keys())[list(self.goods_database.values()).index(goods_info)]}")
        
        print(f"\nОбщие параметры товара:")
        print(f"  - Производство (2024): {goods_info['production_2024']} млн$")
        print(f"  - Потребление (2024): {goods_info['consumption_2024']} млн$")
        print(f"  - Текущая ставка пошлины: {goods_info['current_rate']}")
        print(f"  - Ставка ВТО: {goods_info['wto_rate']}")
        print(f"  - Сертификация: {goods_info['certification']}")
        print(f"  - Госзакупки: {goods_info['government_procurement']}")
        
        print(f"\nАнализ по стране: {country}")
        if country_analysis['found']:
            print(f"  - Классификация: {country_analysis['classification']}")
            print(f"  - Импорт (2024): {country_analysis['import_2024']:.2f} млн$")
            print(f"  - Динамика импорта (2024 vs 2023): {country_analysis['growth_2024_vs_2023']:+.1f}%")
            if country_analysis['unit_price_2024'] > 0:
                print(f"  - Средняя цена (2024): {country_analysis['unit_price_2024']:.2f} $/тонна")
        else:
            print(f"  - Страна не найдена в данных импорта")
        
        print(f"\nОбщая статистика импорта:")
        print(f"  - Общий импорт (2024): {overall_metrics['total_import_2024']:.2f} млн$")
        print(f"  - Доля недружественных стран: {overall_metrics['unfriendly_share']:.1f}%")
        print(f"  - Рост импорта из недружественных стран: {overall_metrics['import_growth']:+.1f}%")
        
        print(f"\nРЕКОМЕНДУЕМАЯ МЕРА: {measure}")
        
        # Описание мер
        measure_descriptions = {
            "Мера 1": "Повышение ставки таможенной пошлины до максимально возможного уровня ВТО",
            "Мера 2": "Введение запретительных пошлин или иных ограничений на импорт из недружественных стран", 
            "Мера 3": "Антидемпинговое расследование или компенсационные пошлины",
            "Мера 4": "Ограничение госзакупок (запрет на закупку импортного товара)",
            "Мера 5": "Ужесточение сертификации",
            "Мера 6": "Отказ от применения мер (нецелесообразность)"
        }
        
        print(f"Описание: {measure_descriptions.get(measure, 'Неизвестная мера')}")
        
        # Обоснование рекомендации
        print(f"\nОБОСНОВАНИЕ РЕКОМЕНДАЦИИ:")
        if overall_metrics['unfriendly_share'] >= 30:
            print(f"- Доля импорта из недружественных стран ({overall_metrics['unfriendly_share']:.1f}%) превышает порог 30%")
        else:
            print(f"- Доля импорта из недружественных стран ({overall_metrics['unfriendly_share']:.1f}%) ниже порога 30%")
        
        if overall_metrics['import_growth'] >= 0:
            print(f"- Импорт из недружественных стран растет (+{overall_metrics['import_growth']:.1f}%)")
        else:
            print(f"- Импорт из недружественных стран снижается ({overall_metrics['import_growth']:.1f}%)")
        
        if goods_info['production_2024'] >= goods_info['consumption_2024']:
            print(f"- Производство ({goods_info['production_2024']} млн$) покрывает потребление ({goods_info['consumption_2024']} млн$)")
        else:
            print(f"- Производство ({goods_info['production_2024']} млн$) не покрывает потребление ({goods_info['consumption_2024']} млн$)")

# Запуск программы
if __name__ == "__main__":
    analyzer = InteractiveTradeAnalysis()
    analyzer.run_analysis()