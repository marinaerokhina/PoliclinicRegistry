package com.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Главный класс сервера "Регистратура поликлиники".
 * Отвечает за запуск сервера, прослушивание входящих соединений
 * и создание потоков для обработки клиентов.
 */
public class Server {
    private int port;
    private DatabaseManager dbManager;

    public Server(int port) {
        this.port = port;
        dbManager = new DatabaseManager(); // Инициализация менеджера БД
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) { // Используем введенный порт
            System.out.println("Сервер запущен и прослушивает порт " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Ожидание нового клиента
                new Thread(new ClientHandler(clientSocket, dbManager)).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Сервер остановлен.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int serverPort = 0;
        boolean validPort = false;

        while (!validPort) {
            System.out.print("Пожалуйста, введите номер порта для сервера (например, 12345): ");
            try {
                serverPort = Integer.parseInt(scanner.nextLine());
                if (serverPort > 0 && serverPort < 65536) { // Проверка на валидный диапазон портов
                    validPort = true;
                } else {
                    System.out.println("Некорректный номер порта. Порт должен быть в диапазоне от 1 до 65535.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод. Пожалуйста, введите целое число для номера порта.");
            }
        }
        scanner.close();

        Server server = new Server(serverPort);
        server.start();
    }
}
