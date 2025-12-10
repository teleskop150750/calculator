# FX Calc - Инструкции по сборке портативных версий

Этот проект содержит скрипты для создания портативных версий JavaFX калькулятора для Linux и Windows.

## Доступные скрипты сборки

### 1. build-linux-portable.sh
Создаёт полностью автономную портативную версию для Linux со встроенной JRE.

**Что создаётся:**
- `target/FXCalc-linux-portable.tar.gz` (~48 MB)
- Содержит кастомный JRE с JavaFX модулями
- НЕ требует установленной Java на целевом компьютере

**Запуск сборки:**
```bash
./build-linux-portable.sh
```

**Использование:**
```bash
tar -xzf FXCalc-linux-portable.tar.gz
./FXCalc.sh
```

---

### 2. build-windows-portable.sh
Создаёт полную портативную версию для Windows со встроенной JRE.

**Что создаётся:**
- `target/FXCalc-windows-portable.zip` (~200-300 MB)
- Содержит полный JRE для Windows
- НЕ требует установленной Java на целевом компьютере

**Запуск сборки:**
```bash
./build-windows-portable.sh
```

**Примечание:** 
- Скачивает Liberica JDK Full (~293 MB) при первом запуске
- Скачанный JDK сохраняется в `target/jdk-windows/` для повторного использования
- Сборка может занять время в зависимости от скорости интернета

---

### 3. build-windows-portable-light.sh ⭐ РЕКОМЕНДУЕТСЯ
Создаёт облегчённую портативную версию для Windows.

**Что создаётся:**
- `target/FXCalc-windows-portable-light.zip` (~7 MB)
- Содержит только JAR файл и скрипты запуска
- **ТРЕБУЕТ** установленной Java 11+ на целевом компьютере

**Запуск сборки:**
```bash
./build-windows-portable-light.sh
```

**Использование на Windows:**
1. Распакуйте архив
2. Запустите `FXCalc.bat` (запуск без консоли)
3. Или `FXCalc-console.bat` (запуск с консолью для отладки)

**Где скачать Java для Windows:**
- Eclipse Temurin: https://adoptium.net/
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/
- Liberica JDK (с JavaFX): https://bell-sw.com/pages/downloads/

---

## Результаты сборки

После выполнения скриптов в папке `target/` будут созданы:

```
target/
├── FXCalc-linux-portable.tar.gz        # Linux версия со встроенной JRE
├── FXCalc-windows-portable.zip         # Windows версия со встроенной JRE (если собрана)
└── FXCalc-windows-portable-light.zip   # Windows версия (требует Java)
```

## Требования для сборки

### Для всех платформ:
- Maven 3.x
- JDK 11 или выше

### Для Linux сборки:
- tar
- bash

### Для Windows сборки:
- wget
- unzip
- zip
- bash

## Быстрый старт

### Сборка для Linux:
```bash
chmod +x build-linux-portable.sh
./build-linux-portable.sh
```

### Сборка для Windows (облегчённая версия):
```bash
chmod +x build-windows-portable-light.sh
./build-windows-portable-light.sh
```

## Размеры итоговых архивов

| Версия | Размер | Требует Java | Платформа |
|--------|--------|--------------|-----------|
| Linux Portable | ~48 MB | Нет | Linux x86_64 |
| Windows Portable (Full) | ~200-300 MB | Нет | Windows 10+ |
| Windows Portable (Light) | ~7 MB | Да (11+) | Windows 10+ |

## Часто задаваемые вопросы

**Q: Какую версию для Windows выбрать?**
A: Если на целевом компьютере установлена Java 11+, используйте light версию (7 MB). Если Java не установлена или вы не уверены, используйте full версию (~200-300 MB).

**Q: Почему Linux версия меньше Windows full версии?**
A: Linux версия использует jlink для создания минимального runtime только с необходимыми модулями. Windows full версия включает полный JDK.

**Q: Можно ли запустить на macOS?**
A: Текущие скрипты предназначены для Linux и Windows. Для macOS нужно создать отдельный скрипт сборки.

**Q: Как обновить приложение?**
A: Просто пересоберите нужную версию с помощью соответствующего скрипта.

## Структура портативных версий

### Linux Portable:
```
linux-portable/
├── fx-calc-fat.jar      # Приложение
├── FXCalc.sh           # Скрипт запуска
├── jre/                # Кастомный JRE
│   └── bin/
│       └── java
└── README.txt
```

### Windows Portable (Light):
```
windows-portable-light/
├── fx-calc-fat.jar           # Приложение
├── FXCalc.bat                # Скрипт запуска (без консоли)
├── FXCalc-console.bat        # Скрипт запуска (с консолью)
└── README.txt
```

## Поддержка

Для вопросов и предложений создавайте issue в репозитории проекта.
