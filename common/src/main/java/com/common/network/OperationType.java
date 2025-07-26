package com.common.network;

import java.io.Serializable;

/**
 * Перечисление, определяющее типы операций, которые клиент может запросить у сервера.
 */
public enum OperationType implements Serializable {
    ADD_PATIENT,
    GET_ALL_PATIENTS,
    UPDATE_PATIENT,
    DELETE_PATIENT,
    SEARCH_PATIENTS,

    ADD_DOCTOR,
    GET_ALL_DOCTORS,
    GET_DOCTOR_DETAILS,

    ADD_APPOINTMENT,
    GET_APPOINTMENTS_BY_DOCTOR_DATE,
    CANCEL_APPOINTMENT,
    GET_PATIENT_HISTORY,

    // Операции для логина и расписания
    LOGIN_PATIENT,
    LOGIN_DOCTOR,
    GET_SCHEDULE,
    GET_PATIENT_DETAILS
}

