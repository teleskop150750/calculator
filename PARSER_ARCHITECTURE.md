# Архитектура парсера выражений

## Обзор

Новая реализация парсера математических выражений основана на JavaScript-версии и использует трёхкомпонентную архитектуру:

```
Входная строка
      ↓
ExpressionTokenizer → Токены
      ↓
PrattParser → RPN (Обратная польская нотация)
      ↓
TokenBasedEvaluator → Результат
```

## Компоненты

### 1. Token & TokenType

**Файлы:** `Token.java`, `TokenType.java`

Базовые классы для представления лексем (токенов):

- `TokenType` - перечисление типов токенов (NUMBER, OPERATOR, FUNCTION и т.д.)
- `Token` - класс, содержащий тип, значение и позицию токена в выражении

**Пример:**
```java
Token token = new Token(TokenType.NUMBER, "42", 0, 2);
```

### 2. ExpressionTokenizer

**Файл:** `ExpressionTokenizer.java`

Разбивает входную строку на последовательность токенов с помощью регулярных выражений.

**Поддерживаемые токены:**
- Числа: `123`, `45.67`
- Операторы: `+`, `-`, `*`, `/`, `^`, `×`, `÷`
- Функции: `sin`, `cos`, `tan`, `ln`, `log`, `sqrt`, `abs`, `exp`, `max`, `min`
- Константы: `pi`, `π`, `e`
- Скобки: `(`, `)`
- Разделитель: `,`
- Переменные: любые идентификаторы

**Пример:**
```java
ExpressionTokenizer tokenizer = new ExpressionTokenizer(
    Arrays.asList("sin", "cos", "ln"),
    Arrays.asList("pi", "e")
);
List<Token> tokens = tokenizer.tokenize("2 + sin(pi / 2)");
```

### 3. PrattParser

**Файл:** `PrattParser.java`

Парсер на основе алгоритма Пратта (Pratt Parser). Преобразует последовательность токенов в обратную польскую нотацию (RPN).

**Преимущества алгоритма Пратта:**
- Элегантная обработка приоритетов операторов
- Поддержка лево- и правоассоциативности
- Легко расширяется новыми операторами
- Компактный код

**Приоритеты операторов:**
1. `+`, `-` (сложение/вычитание) - приоритет 10
2. `*`, `/`, `×`, `÷` (умножение/деление) - приоритет 20
3. `^` (степень, правоассоциативная) - приоритет 30
4. `~` (унарный минус) - приоритет 40

**Пример:**
```java
PrattParser parser = new PrattParser(functions);
List<Token> rpn = parser.parse(tokens);
// "2 + 3 * 4" → ["2", "3", "4", "*", "+"]
```

### 4. TokenBasedEvaluator

**Файл:** `TokenBasedEvaluator.java`

Вычисляет выражение в RPN используя стек.

**Поддерживает:**
- Числа и константы
- Переменные (через Map)
- Бинарные операторы (+, -, *, /, ^)
- Унарные операторы (унарный минус ~)
- Функции (sin, cos, tan, ln, log, sqrt, abs, exp, max, min)

**Пример:**
```java
TokenBasedEvaluator evaluator = TokenBasedEvaluator.createDefault();
Map<String, Double> variables = new HashMap<>();
double result = evaluator.evaluate(rpn, variables);
```

### 5. ExpressionParser

**Файл:** `ExpressionParser.java`

Главный класс, объединяющий все компоненты. Аналог `ExpressionParser` из JS-версии.

**Пример использования:**
```java
// Простое вычисление
ExpressionParser parser = new ExpressionParser("2 + 3 * sin(pi / 2)");
double result = parser.evaluate();  // 5.0

// С переменными
ExpressionParser parser = new ExpressionParser("2 * x^2 + 3 * x + 1");
parser.setVariable("x", 4);
double result = parser.evaluate();  // 45.0
```

## Преимущества архитектуры

### Текущая реализация (ExpressionParser)

```java
// Автоматическая токенизация
ExpressionParser parser = new ExpressionParser("2 + max(3, 4) * sin(x)");
parser.setVariable("x", Math.PI / 2);
double result = parser.evaluate();
```

**Особенности:**
- ✅ Полная токенизация входной строки
- ✅ Поддержка функций с несколькими аргументами: `max(a, b)`
- ✅ Поддержка переменных: `x`, `y`, `z`
- ✅ Унарные операторы: `-5`, `-(2 + 3)`
- ✅ Алгоритм Пратта (мощный и расширяемый)
- ✅ Детальные сообщения об ошибках с позициями
- ✅ Поддержка констант: `pi`, `π`, `e`
- ✅ Интеграция с JavaFX UI через токенную систему ввода

## Интеграция с UI

### Токенная система ввода

Калькулятор использует систему ввода на основе токенов:

- **Функции** добавляются как токены: нажатие `sin` → `sin(` в поле ввода
- **Запятая** для разделения аргументов функций: `max(5, 3)`
- **Backspace** для удаления последнего символа или токена
- **Операторы** сразу добавляются в выражение

**Обработчики в PrimaryController:**
- `onFunctionToken()` - добавляет функцию с открывающей скобкой
- `onComma()` - добавляет разделитель аргументов
- `onBackspace()` - удаляет последний символ/токен
- `onEquals()` - вычисляет выражение через `ExpressionParser`

### Примеры ввода в UI

```
Действие пользователя:          Результат в поле ввода:
[2] [+] [sin] [(] [π] [÷] [2]   2+sin(π/2)
[max] [(] [5] [,] [3] [)]       max(5,3)
[2] [×] [π]                      2×π
```

## Примеры

### Базовая арифметика
```java
ExpressionParser parser = new ExpressionParser("2 + 3 * 4");
double result = parser.evaluate();  // 14.0
```

### Функции
```java
ExpressionParser parser = new ExpressionParser("sin(pi / 6) + cos(pi / 3)");
double result = parser.evaluate();  // 1.0
```

### Переменные
```java
ExpressionParser parser = new ExpressionParser("a * x^2 + b * x + c");
parser.setVariable("a", 2);
parser.setVariable("b", -3);
parser.setVariable("c", 1);
parser.setVariable("x", 5);
double result = parser.evaluate();  // 36.0
```

### Сложные выражения
```java
ExpressionParser parser = new ExpressionParser("sqrt(max(5, 3)) + log(100) / ln(e)");
double result = parser.evaluate();  // 4.236...
```

### Унарный минус
```java
ExpressionParser parser = new ExpressionParser("-5 + -(2 + 3)");
double result = parser.evaluate();  // -10.0
```

## Тестирование

Для тестирования используйте класс `ParserDemo`:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.example.ParserDemo"
```

Вывод покажет результаты различных выражений и их RPN-представление.

## Расширение

### Добавление новой функции

```java
// В ExpressionParser.initFunctions()
functions.put("myFunc", new TokenBasedEvaluator.FunctionDef(1, args -> {
    // Ваша логика
    return Math.custom(args[0]);
}));

// В ExpressionTokenizer
List<String> functionNames = Arrays.asList(..., "myFunc");
```

### Добавление нового оператора

```java
// В PrattParser.initPrecedence()
prec.put("%%", 25);  // Установить приоритет

// В TokenBasedEvaluator.initOperators()
operators.put("%%", new OperatorDef(2, args -> args[0] % args[1]));
```

## Архитектурные решения

### Почему алгоритм Пратта?

1. **Простота** - интуитивно понятный код
2. **Расширяемость** - легко добавлять новые операторы
3. **Производительность** - O(n) сложность
4. **Гибкость** - поддержка префиксных, инфиксных и постфиксных операторов

### Почему RPN?

1. **Простота вычисления** - нет необходимости в рекурсии
2. **Эффективность** - линейное время вычисления
3. **Отладка** - легко проверить промежуточные шаги
4. **Стандарт** - широко используется в калькуляторах

## Ссылки

- [Алгоритм Пратта (Википедия)](https://en.wikipedia.org/wiki/Operator-precedence_parser#Pratt_parsing)
- [RPN (Обратная польская нотация)](https://ru.wikipedia.org/wiki/Обратная_польская_запись)
- [Алгоритм сортировочной станции Дейкстры](https://ru.wikipedia.org/wiki/Алгоритм_сортировочной_станции)
