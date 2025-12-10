#!/bin/bash
# Скрипт для создания портативной версии FX Calc для Windows
# Запускается из Linux с установленным WiX Toolset

set -e

echo "=== Сборка портативной версии FX Calc для Windows ==="

# Проверка наличия необходимых инструментов
if ! command -v wget &> /dev/null; then
    echo "ОШИБКА: wget не установлен. Установите: sudo apt install wget"
    exit 1
fi

# Очистка предыдущих сборок
echo "Шаг 1: Очистка предыдущих сборок..."
rm -rf target/windows-portable
mkdir -p target/windows-portable

# Компиляция и упаковка
echo "Шаг 2: Компиляция и создание JAR файла..."
mvn clean package -q

# Скачиваем Windows JDK если его нет
WINDOWS_JDK_DIR="target/jdk-windows"
if [ ! -d "$WINDOWS_JDK_DIR" ]; then
    echo "Шаг 3: Скачивание Windows JDK..."
    mkdir -p "$WINDOWS_JDK_DIR"
    
    # Используем Liberica JDK с JavaFX (Full version)
    # URL: https://bell-sw.com/pages/downloads/
    JDK_URL="https://download.bell-sw.com/java/21.0.5+11/bellsoft-jdk21.0.5+11-windows-amd64-full.zip"
    
    echo "Скачивание Liberica JDK Full (с JavaFX) для Windows..."
    if ! wget --show-progress "$JDK_URL" -O target/jdk-windows.zip 2>&1; then
        echo "ОШИБКА: Не удалось скачать JDK"
        echo "Попробуйте скачать вручную и поместить в target/jdk-windows/"
        exit 1
    fi
    
    echo "Распаковка JDK..."
    unzip -q target/jdk-windows.zip -d target/
    mv target/jdk-*-full/* "$WINDOWS_JDK_DIR/"
    rm target/jdk-windows.zip
fi

# Копируем необходимые файлы
echo "Шаг 4: Подготовка портативной версии..."
cp target/fx-calc-fat.jar target/windows-portable/

# Копируем JRE (можно оптимизировать размер, удалив ненужное)
echo "Шаг 5: Копирование JRE для Windows..."
cp -r "$WINDOWS_JDK_DIR" target/windows-portable/jre

# Создаём bat-скрипт для запуска
echo "Шаг 6: Создание скрипта запуска..."
cat > target/windows-portable/FXCalc.bat << 'EOF'
@echo off
setlocal

set SCRIPT_DIR=%~dp0
set JAVA_EXE=%SCRIPT_DIR%jre\bin\java.exe

if not exist "%JAVA_EXE%" (
    echo ERROR: Java not found at %JAVA_EXE%
    pause
    exit /b 1
)

start "FX Calc" "%JAVA_EXE%" -jar "%SCRIPT_DIR%fx-calc-fat.jar"
EOF

# Создаём README
echo "Шаг 7: Создание README..."
cat > target/windows-portable/README.txt << 'EOF'
FX Calc - Portable Version for Windows

To run the application:
  Double-click FXCalc.bat

Or run from command line:
  FXCalc.bat

Or directly with Java:
  jre\bin\java.exe -jar fx-calc-fat.jar

This is a portable version that does not require Java installation.
All necessary components are included in the jre\ folder.

Requirements:
  - Windows 10 or later
  - x86_64 architecture

Package size may be large due to included JRE and JavaFX libraries.
EOF

# Создаём архив
echo "Шаг 8: Создание ZIP архива..."
cd target/windows-portable
zip -q -r ../FXCalc-windows-portable.zip *
cd ../..

echo ""
echo "=== Сборка завершена! ==="
echo "Портативная версия для Windows создана: target/FXCalc-windows-portable.zip"
echo "Размер архива: $(du -h target/FXCalc-windows-portable.zip | cut -f1)"
echo ""
echo "Для использования:"
echo "  1. Распакуйте FXCalc-windows-portable.zip"
echo "  2. Запустите FXCalc.bat"
echo ""
echo "ПРИМЕЧАНИЕ: Архив может быть большим (~200-300 МБ) из-за включенного JRE"
