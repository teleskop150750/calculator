#!/bin/bash
# Упрощенная версия для создания портативного приложения для Windows
# Создает только JAR файл со скриптом запуска (требуется установленная Java на целевом ПК)

set -e

echo "=== Сборка упрощенной портативной версии FX Calc для Windows ==="

# Очистка
echo "Шаг 1: Очистка..."
rm -rf target/windows-portable-light
mkdir -p target/windows-portable-light

# Компиляция
echo "Шаг 2: Компиляция..."
mvn clean package -q

# Копируем JAR
echo "Шаг 3: Копирование JAR файла..."
cp target/fx-calc-fat.jar target/windows-portable-light/

# Создаём bat-скрипт для запуска
echo "Шаг 4: Создание скрипта запуска..."
cat > target/windows-portable-light/FXCalc.bat << 'EOF'
@echo off
setlocal

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or later from:
    echo https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Run the application
start "FX Calc" javaw -jar "%~dp0fx-calc-fat.jar"
EOF

# Создаём альтернативный скрипт для запуска в консоли
cat > target/windows-portable-light/FXCalc-console.bat << 'EOF'
@echo off
setlocal

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or later from:
    echo https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Run the application with console output
java -jar "%~dp0fx-calc-fat.jar"
pause
EOF

# Создаём README
echo "Шаг 5: Создание README..."
cat > target/windows-portable-light/README.txt << 'EOF'
FX Calc - Portable Version for Windows (Lightweight)

REQUIREMENTS:
  - Windows 10 or later
  - Java 11 or later must be installed
  
If you don't have Java installed, download it from:
  - Temurin (Eclipse Adoptium): https://adoptium.net/
  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
  - Liberica JDK (with JavaFX): https://bell-sw.com/pages/downloads/

TO RUN:
  1. Double-click FXCalc.bat (recommended - no console window)
  2. Or double-click FXCalc-console.bat (shows console for debugging)
  3. Or run from command line: java -jar fx-calc-fat.jar

ADVANTAGES:
  - Small size (~8 MB) - only the application
  - Uses system-installed Java
  - Easy to update

DISADVANTAGES:
  - Requires Java to be installed separately
  
For a version with bundled Java (no installation required):
  - Run build-windows-portable.sh to create full portable version
  - Size will be ~200-300 MB but will work without Java installation
EOF

# Создаём архив
echo "Шаг 6: Создание ZIP архива..."
cd target/windows-portable-light
zip -q -r ../FXCalc-windows-portable-light.zip *
cd ../..

echo ""
echo "=== Сборка завершена! ==="
echo "Упрощенная портативная версия создана: target/FXCalc-windows-portable-light.zip"
echo "Размер архива: $(du -h target/FXCalc-windows-portable-light.zip | cut -f1)"
echo ""
echo "ВАЖНО: Для работы требуется установленная Java 11+ на целевом компьютере"
echo ""
echo "Для использования:"
echo "  1. Распакуйте FXCalc-windows-portable-light.zip"
echo "  2. Запустите FXCalc.bat"
