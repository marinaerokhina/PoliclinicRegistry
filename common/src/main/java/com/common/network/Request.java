package com.common.network;

import java.io.Serializable;

/**
 * Объект запроса, отправляемый от клиента к серверу.
 * Содержит тип операции и данные, необходимые для выполнения этой операции.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L; // Для сериализации

    private OperationType type;
    private Object data; // Данные, связанные с запросом (например, объект Patient, ID, строка поиска)

    public Request(OperationType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public OperationType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Запрос{" +
                "тип=" + type +
                ", данные=" + data +
                '}';
    }
}
