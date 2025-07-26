package com.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Главный класс JavaFX приложения для клиентской части "Регистратура поликлиники".
 */
public class MainApp extends Application {

    private Client client; // Экземпляр сетевого клиента

    @Override
    public void init() throws Exception {
        super.init();
        client = new Client(); // Инициализация клиента
        // Попытка подключения при запуске приложения.
        // Если не удастся, пользователь увидит ошибку, но сможет попробовать войти позже.
        client.connect();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/client/main_view.fxml")); // Путь к FXML
        Parent root = loader.load();

        // Передача экземпляра клиента в контроллер
        PolyclinicController controller = loader.getController();
        controller.setClient(client);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Регистратура поликлиники");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (client != null) {
            client.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
