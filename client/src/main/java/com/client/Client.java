package com.client;

import com.common.network.Request;
import com.common.network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Класс, отвечающий за сетевое взаимодействие клиента с сервером.
 * Отправляет запросы и получает ответы.
 */
public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // Адрес сервера
    private static final int SERVER_PORT = 12345; // Порт сервера

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Устанавливает соединение с сервером.
     * @return true, если соединение установлено успешно, иначе false.
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            // Порядок инициализации потоков важен: сначала OutputStream, потом InputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Клиент подключен к серверу.");
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
            close();
            return false;
        }
    }

    /**
     * Отправляет запрос на сервер и получает ответ.
     * @param request Объект запроса.
     * @return Объект ответа от сервера.
     */
    public Response sendRequest(Request request) {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            System.err.println("Соединение с сервером не установлено или закрыто. Попытка переподключения...");
            if (!connect()) {
                return new Response(false, "Не удалось подключиться к серверу.");
            }
        }

        try {
            out.writeObject(request);
            out.flush(); // Убедиться, что данные отправлены
            return (Response) in.readObject();
        } catch (IOException e) {
            System.err.println("Ошибка при отправке запроса или получении ответа: " + e.getMessage());
            close(); // Закрыть соединение при ошибке
            return new Response(false, "Ошибка связи с сервером: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка десериализации ответа от сервера: " + e.getMessage());
            return new Response(false, "Ошибка данных от сервера.");
        }
    }

    /**
     * Закрывает соединение с сервером.
     */
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Соединение с сервером закрыто.");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}
