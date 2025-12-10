package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Точка входа JavaFX‑калькулятора.
 * <p>
 * Создаёт основную сцену, подключает таблицу стилей и предоставляет вспомогательные
 * методы для загрузки и переключения FXML‑разметок.
 * </p>
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 380, 620);
        scene.getStylesheets().add(
                App.class.getResource("style.css").toExternalForm());
        stage.setTitle("FX Calc");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Заменяет корень текущей сцены макетом из указанного FXML‑файла.
     *
     * @param fxml базовое имя FXML без расширения
     * @throws IOException если не удалось загрузить FXML
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Загружает FXML из того же пакета, что и этот класс.
     *
     * @param fxml базовое имя FXML без расширения
     * @return корневой узел, определённый в FXML
     * @throws IOException если файл не удалось прочитать или разобрать
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Запускает JavaFX‑приложение.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch();
    }

}