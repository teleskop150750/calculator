#!/bin/bash
# Скрипт для создания портативной версии FX Calc для Linux

set -e

echo "=== Сборка портативной версии FX Calc для Linux ==="

# Очистка предыдущих сборок
echo "Шаг 1: Очистка предыдущих сборок..."
rm -rf target/linux-portable
mkdir -p target/linux-portable

# Компиляция и упаковка
echo "Шаг 2: Компиляция и создание JAR файла..."
mvn clean package -q

# Создание кастомного JRE с помощью jlink
echo "Шаг 3: Создание кастомного JRE с JavaFX модулями..."
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
JAVAFX_MODS=target/modules

# Создаем минимальный runtime с необходимыми модулями
jlink \
    --module-path "$JAVAFX_MODS:$JAVA_HOME/jmods" \
    --add-modules java.base,java.logging,java.xml,java.desktop,java.prefs,java.scripting,jdk.unsupported \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
    --output target/linux-portable/jre \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress=2

# Копируем JAR файл
echo "Шаг 4: Копирование JAR файла..."
cp target/fx-calc-fat.jar target/linux-portable/

# Создаём скрипт запуска
echo "Шаг 5: Создание скрипта запуска..."
cat > target/linux-portable/FXCalc.sh << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"$SCRIPT_DIR/jre/bin/java" -jar "$SCRIPT_DIR/fx-calc-fat.jar" "$@"
EOF

chmod +x target/linux-portable/FXCalc.sh

# Создаём README
echo "Шаг 6: Создание README..."
cat > target/linux-portable/README.txt << 'EOF'
FX Calc - Портативная версия для Linux

Запуск приложения:
  ./FXCalc.sh

Или:
  ./jre/bin/java -jar fx-calc-fat.jar

Это портативная версия, которая не требует установки Java.
Все необходимые компоненты включены в папку jre/.

Требования:
  - Linux x86_64
  - Графический интерфейс (X11 или Wayland)
EOF

# Создаём архив
echo "Шаг 7: Создание архива..."
cd target/linux-portable
tar -czf ../FXCalc-linux-portable.tar.gz *
cd ../..

echo ""
echo "=== Сборка завершена! ==="
echo "Портативная версия создана: target/FXCalc-linux-portable.tar.gz"
echo "Размер архива: $(du -h target/FXCalc-linux-portable.tar.gz | cut -f1)"
echo ""
echo "Для распаковки и запуска:"
echo "  tar -xzf FXCalc-linux-portable.tar.gz"
echo "  cd linux-portable"
echo "  ./FXCalc.sh"
