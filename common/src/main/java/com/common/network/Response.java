package com.common.network;

import java.io.Serializable;

/**
 * Объект ответа, отправляемый от сервера клиенту.
 * Содержит статус операции (успех/неудача) и результат (если применимо).
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L; // Для сериализации

    private boolean success;
    private String message; // Сообщение об успехе или ошибке
    private Object data; // Результат операции (например, список пациентов, один пациент)

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Response(boolean success, String message) {
        this(success, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Ответ{" +
                "успех=" + success +
                ", сообщение='" + message + '\'' +
                ", данные=" + data +
                '}';
    }
}