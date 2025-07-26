package com.server;

import com.common.model.Appointment;
import com.common.model.Doctor;
import com.common.model.Patient;
import com.common.network.Request;
import com.common.network.Response;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

/**
 * Класс-обработчик для каждого клиентского подключения.
 * Запускается в отдельном потоке для обработки запросов клиента.
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DatabaseManager dbManager;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
        try {
            // Порядок инициализации потоков важен: сначала OutputStream, потом InputStream
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("Новый клиент подключен: " + clientSocket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            System.err.println("Ошибка при создании потоков для клиента: " + e.getMessage());
            closeResources();
        }
    }

    @Override
    public void run() {
        try {
            while (clientSocket.isConnected()) {
                Request request = (Request) in.readObject();
                System.out.println("Получен запрос от клиента: " + request.getType());
                Response response = processRequest(request);
                out.writeObject(response);
                out.flush(); // Убедиться, что данные отправлены
            }
        } catch (IOException e) {
            System.err.println("Клиент отключился или ошибка ввода/вывода: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка десериализации объекта запроса: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    /**
     * Обрабатывает полученный запрос от клиента и формирует ответ.
     * @param request Объект запроса.
     * @return Объект ответа.
     */
    private Response processRequest(Request request) {
        try {
            switch (request.getType()) {
                case ADD_PATIENT:
                    Patient newPatient = (Patient) request.getData();
                    Patient addedPatient = dbManager.addPatient(newPatient);
                    if (addedPatient != null) {
                        return new Response(true, "Пациент успешно добавлен.", addedPatient);
                    } else {
                        return new Response(false, "Не удалось добавить пациента.");
                    }
                case GET_ALL_PATIENTS:
                    List<Patient> patients = dbManager.getAllPatients();
                    return new Response(true, "Список пациентов получен.", patients);
                case UPDATE_PATIENT:
                    Patient patientToUpdate = (Patient) request.getData();
                    boolean updated = dbManager.updatePatient(patientToUpdate);
                    if (updated) {
                        return new Response(true, "Данные пациента успешно обновлены.");
                    } else {
                        return new Response(false, "Не удалось обновить данные пациента.");
                    }
                case DELETE_PATIENT:
                    int patientIdToDelete = (int) request.getData();
                    boolean deleted = dbManager.deletePatient(patientIdToDelete);
                    if (deleted) {
                        return new Response(true, "Пациент успешно удален.");
                    } else {
                        return new Response(false, "Не удалось удалить пациента.");
                    }
                case SEARCH_PATIENTS:
                    String searchQuery = (String) request.getData();
                    List<Patient> foundPatients = dbManager.searchPatients(searchQuery);
                    return new Response(true, "Результаты поиска пациентов.", foundPatients);
                case ADD_DOCTOR:
                    Doctor newDoctor = (Doctor) request.getData();
                    Doctor addedDoctor = dbManager.addDoctor(newDoctor);
                    if (addedDoctor != null) {
                        return new Response(true, "Врач успешно добавлен.", addedDoctor);
                    } else {
                        return new Response(false, "Не удалось добавить врача.");
                    }
                case GET_ALL_DOCTORS:
                    List<Doctor> doctors = dbManager.getAllDoctors();
                    return new Response(true, "Список врачей получен.", doctors);
                case ADD_APPOINTMENT:
                    Appointment newAppointment = (Appointment) request.getData();
                    // Проверка на дублирование записи (например, если слот уже занят)
                    if (dbManager.isAppointmentSlotTaken(newAppointment.getDoctorId(), newAppointment.getAppointmentDate(), newAppointment.getAppointmentTime())) {
                        return new Response(false, "Выбранное время уже занято.");
                    }
                    Appointment addedAppointment = dbManager.addAppointment(newAppointment);
                    if (addedAppointment != null) {
                        return new Response(true, "Запись на прием успешно добавлена.", addedAppointment);
                    } else {
                        return new Response(false, "Не удалось добавить запись на прием.");
                    }
                case GET_APPOINTMENTS_BY_DOCTOR_DATE:
                    // Ожидаем массив Object[]: [doctorId (Integer), date (LocalDate)]
                    Object[] appointmentQueryData = (Object[]) request.getData();
                    int doctorId = (Integer) appointmentQueryData[0];
                    LocalDate appointmentDate = (LocalDate) appointmentQueryData[1];
                    List<Appointment> doctorAppointments = dbManager.getAppointmentsByDoctorAndDate(doctorId, appointmentDate);
                    return new Response(true, "Расписание врача получено.", doctorAppointments);
                case CANCEL_APPOINTMENT:
                    int appointmentIdToCancel = (int) request.getData();
                    boolean cancelled = dbManager.cancelAppointment(appointmentIdToCancel);
                    if (cancelled) {
                        return new Response(true, "Запись на прием успешно отменена.");
                    } else {
                        return new Response(false, "Не удалось отменить запись на прием.");
                    }
                case GET_PATIENT_HISTORY:
                    int patientIdForHistory = (int) request.getData();
                    List<Appointment> patientHistory = dbManager.getPatientAppointmentHistory(patientIdForHistory);
                    return new Response(true, "История записей пациента получена.", patientHistory);
                // Новые операции для логина и расписания
                case LOGIN_PATIENT:
                    Patient loginPatientData = (Patient) request.getData();
                    Patient authenticatedPatient = dbManager.loginPatient(
                            loginPatientData.getLastName(),
                            loginPatientData.getFirstName(),
                            loginPatientData.getMiddleName()
                    );
                    if (authenticatedPatient != null) {
                        return new Response(true, "Вход пациента успешен.", authenticatedPatient);
                    } else {
                        return new Response(false, "Пациент с указанными ФИО не найден.");
                    }
                case LOGIN_DOCTOR:
                    Doctor loginDoctorData = (Doctor) request.getData();
                    Doctor authenticatedDoctor = dbManager.loginDoctor(
                            loginDoctorData.getLastName(),
                            loginDoctorData.getFirstName(),
                            loginDoctorData.getMiddleName()
                    );
                    if (authenticatedDoctor != null) {
                        return new Response(true, "Вход врача успешен.", authenticatedDoctor);
                    } else {
                        return new Response(false, "Врач с указанными ФИО не найден.");
                    }
                case GET_SCHEDULE:
                    LocalDate scheduleDate = (LocalDate) request.getData();
                    List<Appointment> fullSchedule = dbManager.getAppointmentsByDate(scheduleDate);
                    return new Response(true, "Полное расписание получено.", fullSchedule);
                case GET_PATIENT_DETAILS:
                    int patientId = (int) request.getData();
                    Patient patientDetails = dbManager.getPatientById(patientId);
                    if (patientDetails != null) {
                        return new Response(true, "Детали пациента получены.", patientDetails);
                    } else {
                        return new Response(false, "Пациент не найден.");
                    }
                case GET_DOCTOR_DETAILS:
                    int docId = (int) request.getData();
                    Doctor doctorDetails = dbManager.getDoctorById(docId);
                    if (doctorDetails != null) {
                        return new Response(true, "Детали врача получены.", doctorDetails);
                    } else {
                        return new Response(false, "Врач не найден.");
                    }
                default:
                    return new Response(false, "Неизвестный тип операции.");
            }
        } catch (ClassCastException e) {
            System.err.println("Ошибка приведения типов в запросе: " + e.getMessage());
            return new Response(false, "Неверный формат данных для запрошенной операции.");
        } catch (Exception e) {
            System.err.println("Ошибка обработки запроса: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Закрывает все ресурсы (сокеты, потоки).
     */
    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            System.out.println("Клиент отключен: " + clientSocket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии ресурсов клиента: " + e.getMessage());
        }
    }
}
