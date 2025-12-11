# Структура пакетов

## Обзор

Проект организован в следующие пакеты:

```
com.example
├── parser/              # Парсинг математических выражений
├── evaluator/           # Вычисление выражений
├── util/                # Утилиты (форматирование, история)
└── (корневой пакет)     # UI контроллеры и главный класс
```

## Пакеты

### 📦 com.example.parser

**Назначение:** Лексический и синтаксический анализ математических выражений

**Классы:**
- `Token` - представление токена (лексемы)
- `TokenType` - перечисление типов токенов
- `ExpressionTokenizer` - токенизатор (разбивает строку на токены)
- `PrattParser` - парсер на основе алгоритма Пратта (преобразует токены в RPN)
- `ExpressionParser` - главный класс парсера (объединяет все компоненты)

**Зависимости:**
- → `com.example.evaluator` (использует `TokenBasedEvaluator`)

---

### 📦 com.example.evaluator

**Назначение:** Вычисление математических выражений

**Классы:**
- `TokenBasedEvaluator` - вычисление токенизированных выражений в RPN (основан на алгоритме Пратта)

**Зависимости:**
- → `com.example.parser` (использует `Token`, `TokenType`)

---

### 📦 com.example.util

**Назначение:** Вспомогательные утилиты

**Классы:**
- `NumberFormatter` - форматирование чисел для отображения
- `History` - управление историей вычислений

**Зависимости:** нет

---

### 📦 com.example (корневой)

**Назначение:** Основное приложение и UI-контроллеры

**Классы:**
- `App` - точка входа JavaFX приложения
- `PrimaryController` - контроллер основного окна калькулятора
- `SecondaryController` - контроллер окна "О программе"
- `ParserDemo` - демонстрационный класс для тестирования парсера

**Зависимости:**
- → `com.example.parser` (использует `ExpressionParser`)
- → `com.example.util` (использует `NumberFormatter`, `History`)

---

## Граф зависимостей

```
┌─────────────────┐
│  com.example    │  (UI слой)
│  - App          │
│  - Controllers  │
└────────┬────────┘
         │
         ├───────────────────────────────┐
         │                               │
         ▼                               ▼
┌─────────────────┐           ┌─────────────────┐
│  parser         │◄──────────│  evaluator      │
│  - Tokenizer    │           │  - Evaluators   │
│  - Parser       │           │  - MathOps      │
│  - Token        │           └─────────────────┘
└─────────────────┘                     │
         │                               │
         │                               ▼
         │                      ┌─────────────────┐
         └─────────────────────►│  util           │
                                │  - Formatter    │
                                │  - History      │
                                └─────────────────┘
```

## Принципы организации

### 1. Разделение ответственности (Separation of Concerns)
- **parser** - отвечает только за разбор выражений
- **evaluator** - отвечает только за вычисления
- **util** - предоставляет общие утилиты
- **корневой пакет** - координирует работу всех компонентов в UI

### 2. Минимизация зависимостей
- `util` не зависит ни от кого (может быть переиспользован)
- `parser` и `evaluator` зависят друг от друга для совместной работы
- UI слой зависит от всех, но остальные не знают о UI

### 3. Логическая группировка
- Классы с общей функциональностью находятся в одном пакете
- Имена пакетов отражают их назначение
- Легко найти нужный класс по его функции

## Миграция компонентов

### Было (все в одном пакете):
```
com.example/
├── App.java
├── PrimaryController.java
├── SecondaryController.java
├── Token.java
├── TokenType.java
├── ExpressionTokenizer.java
├── PrattParser.java
├── ExpressionParser.java
├── TokenBasedEvaluator.java
├── ExpressionEvaluator.java
├── MathOperations.java
├── NumberFormatter.java
├── History.java
└── ParserDemo.java
```

### Стало (организованная структура):
```
com.example/
├── App.java
├── PrimaryController.java
├── SecondaryController.java
├── ParserDemo.java
├── parser/
│   ├── Token.java
│   ├── TokenType.java
│   ├── ExpressionTokenizer.java
│   ├── PrattParser.java
│   └── ExpressionParser.java
├── evaluator/
│   └── TokenBasedEvaluator.java
└── util/
    ├── NumberFormatter.java
    └── History.java
```

## Преимущества новой структуры

✅ **Ясная организация** - легко понять назначение каждого компонента  
✅ **Масштабируемость** - легко добавлять новые классы в соответствующие пакеты  
✅ **Переиспользование** - пакеты `parser`, `evaluator`, `util` можно использовать в других проектах  
✅ **Тестируемость** - каждый пакет можно тестировать независимо  
✅ **Читаемость** - импорты явно показывают зависимости между компонентами  

## Примеры использования

### Использование парсера
```java
import com.example.parser.ExpressionParser;
import com.example.util.NumberFormatter;

ExpressionParser parser = new ExpressionParser("2 + 3 * sin(pi / 2)");
double result = parser.evaluate();
System.out.println(NumberFormatter.format(result));
```

### Использование утилит
```java
import com.example.util.History;
import com.example.util.NumberFormatter;

History history = new History();
history.addEntry("2 + 3 = " + NumberFormatter.format(5.0));
System.out.println(history.getFormattedHistory());
```
