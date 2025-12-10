/**
 * Модуль JavaFX‑калькулятора с точкой входа и контроллерами.
 */
module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.example to javafx.fxml;

    exports com.example;
}
