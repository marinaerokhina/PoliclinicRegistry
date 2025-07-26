package com.client;

import com.common.model.Appointment;
import com.common.model.Doctor;
import com.common.model.Patient;
import com.common.network.OperationType;
import com.common.network.Request;
import com.common.network.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для пользовательского интерфейса JavaFX.
 * Обрабатывает события UI и взаимодействует с сетевым клиентом.
 */
public class PolyclinicController {
    // UI элементы для логина
    @FXML private TextField loginLastNameField;
    @FXML private TextField loginFirstNameField;
    @FXML private TextField loginMiddleNameField;
    @FXML private CheckBox isPatientCheckBox;
    @FXML private CheckBox isDoctorCheckBox;
    @FXML private Button loginButton;
    @FXML private VBox loginPanel;

    // UI элементы для расписания
    @FXML private VBox schedulePanel;
    @FXML private DatePicker scheduleDatePicker;
    @FXML private GridPane scheduleGrid;
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;

    private Client client;
    private UserSession userSession; // Сессия текущего пользователя

    // Рабочие часы по умолчанию для врачей (для отображения белых ячеек)
    private final LocalTime WORK_START_TIME = LocalTime.of(8, 0);
    private final LocalTime WORK_END_TIME = LocalTime.of(18, 0);
    private final int SLOT_DURATION_MINUTES = 30;

    /**
     * Устанавливает экземпляр сетевого клиента.
     * @param client Экземпляр Client.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        // Изначально показываем панель логина
        loginPanel.setVisible(true);
        loginPanel.setManaged(true);
        schedulePanel.setVisible(false);
        schedulePanel.setManaged(false);

        // Устанавливаем слушатели для чекбоксов, чтобы только один мог быть выбран
        isPatientCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) isDoctorCheckBox.setSelected(false);
        });
        isDoctorCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) isPatientCheckBox.setSelected(false);
        });

        // Устанавливаем текущую дату в DatePicker и слушатель изменений
        scheduleDatePicker.setValue(LocalDate.now());
        scheduleDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                displaySchedule();
            }
        });
    }

    /**
     * Обработчик кнопки "Войти".
     */
    @FXML
    private void handleLogin() {
        String lastName = loginLastNameField.getText().trim();
        String firstName = loginFirstNameField.getText().trim();
        String middleName = loginMiddleNameField.getText().trim();

        if (lastName.isEmpty() || firstName.isEmpty() || middleName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Пожалуйста, введите Фамилию, Имя и Отчество.");
            return;
        }
        if (!isPatientCheckBox.isSelected() && !isDoctorCheckBox.isSelected()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Пожалуйста, выберите роль (Пациент или Врач).");
            return;
        }
        // Отправляем запрос на сервер для аутентификации
        Request request;
        OperationType loginType;
        if (isPatientCheckBox.isSelected()) {
            loginType = OperationType.LOGIN_PATIENT;
            request = new Request(loginType, new Patient(lastName, firstName, middleName, null, null, null, null));
        } else { // isDoctorCheckBox.isSelected()
            loginType = OperationType.LOGIN_DOCTOR;
            request = new Request(loginType, new Doctor(lastName, firstName, middleName, null, null));
        }
        Response response = client.sendRequest(request);
        if (response.isSuccess()) {
            userSession = new UserSession();
            if (loginType == OperationType.LOGIN_PATIENT) {
                userSession.setPatient((Patient) response.getData());
                userSession.setPatient(true);
                loggedInUserLabel.setText("Вы вошли как: " + userSession.getPatient().getLastName() + " " +
                        userSession.getPatient().getFirstName() + " " +
                        userSession.getPatient().getMiddleName());
            } else {
                userSession.setDoctor((Doctor) response.getData());
                userSession.setDoctor(true);
                loggedInUserLabel.setText("Вы вошли как: " + userSession.getDoctor().getLastName() + " " +
                        userSession.getDoctor().getFirstName() + " " +
                        userSession.getDoctor().getMiddleName() + " (" +
                        userSession.getDoctor().getSpecialty() + ")");
            }
            showAlert(Alert.AlertType.INFORMATION, "Вход успешен", response.getMessage());
            showSchedulePanel();
            displaySchedule(); // Загружаем расписание после успешного входа
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка входа", response.getMessage());
        }
    }

    /**
     * Переключает видимость панелей на панель расписания.
     */
    private void showSchedulePanel() {
        loginPanel.setVisible(false);
        loginPanel.setManaged(false);
        schedulePanel.setVisible(true);
        schedulePanel.setManaged(true);
    }

    /**
     * Переключает видимость панелей на панель логина.
     */
    private void showLoginPanel() {
        loginPanel.setVisible(true);
        loginPanel.setManaged(true);
        schedulePanel.setVisible(false);
        schedulePanel.setManaged(false);
        clearLoginFields();
        userSession = null; // Сброс сессии
    }

    /**
     * Очищает поля логина.
     */
    private void clearLoginFields() {
        loginLastNameField.clear();
        loginFirstNameField.clear();
        loginMiddleNameField.clear();
        isPatientCheckBox.setSelected(false);
        isDoctorCheckBox.setSelected(false);
    }

    /**
     * Обработчик кнопки "Выйти".
     */
    @FXML
    private void handleLogout() {
        showAlert(Alert.AlertType.INFORMATION, "Выход", "Вы успешно вышли из системы.");
        showLoginPanel();
    }

    /**
     * Отображает расписание приемов.
     */
    private void displaySchedule() {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getRowConstraints().clear();
        scheduleGrid.getColumnConstraints().clear();
        LocalDate selectedDate = scheduleDatePicker.getValue();
        if (selectedDate == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пожалуйста, выберите дату.");
            return;
        }
        List<Doctor> doctorsToDisplay = new ArrayList<>();
        List<Appointment> appointmentsToDisplay = new ArrayList<>();
        if (userSession.isPatient()) {
            // Если вошел пациент, показываем расписание всех врачей
            Response doctorsResponse = client.sendRequest(new Request(OperationType.GET_ALL_DOCTORS, null));
            if (doctorsResponse.isSuccess() && doctorsResponse.getData() instanceof List) {
                doctorsToDisplay.addAll((List<Doctor>) doctorsResponse.getData());
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка загрузки врачей", doctorsResponse.getMessage());
                return;
            }
            Response appointmentsResponse = client.sendRequest(new Request(OperationType.GET_SCHEDULE, selectedDate));
            if (appointmentsResponse.isSuccess() && appointmentsResponse.getData() instanceof List) {
                appointmentsToDisplay.addAll((List<Appointment>) appointmentsResponse.getData());
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка загрузки расписания", appointmentsResponse.getMessage());
                return;
            }
        } else if (userSession.isDoctor()) {
            // Если вошел врач, показываем расписание только для этого врача
            Doctor loggedInDoctor = userSession.getDoctor();
            if (loggedInDoctor == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Данные врача не найдены.");
                return;
            }
            doctorsToDisplay.add(loggedInDoctor);
            // Запрашиваем записи только для этого врача на выбранную дату
            Request doctorAppointmentsRequest = new Request(OperationType.GET_APPOINTMENTS_BY_DOCTOR_DATE,
                    new Object[]{loggedInDoctor.getId(), selectedDate});
            Response doctorAppointmentsResponse = client.sendRequest(doctorAppointmentsRequest);
            if (doctorAppointmentsResponse.isSuccess() && doctorAppointmentsResponse.getData() instanceof List) {
                appointmentsToDisplay.addAll((List<Appointment>) doctorAppointmentsResponse.getData());
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка загрузки расписания врача", doctorAppointmentsResponse.getMessage());
                return;
            }
        }
        // Создаем карту для быстрого доступа к записям по времени и врачу
        Map<LocalTime, Map<Integer, Appointment>> appointmentsByTimeAndDoctor = appointmentsToDisplay.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentTime,
                        Collectors.toMap(Appointment::getDoctorId, app -> app)));
        // Добавляем колонку для времени
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setPrefWidth(80);
        scheduleGrid.getColumnConstraints().add(timeColumn);
        // Добавляем заголовки для врачей
        for (int i = 0; i < doctorsToDisplay.size(); i++) {
            Doctor doctor = doctorsToDisplay.get(i);
            Label doctorHeader = new Label(doctor.getSpecialty() + "\n" + doctor.getLastName());
            doctorHeader.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            scheduleGrid.add(doctorHeader, i + 1, 0); // +1 потому что первая колонка для времени
            ColumnConstraints doctorColumn = new ColumnConstraints();
            doctorColumn.setPrefWidth(120);
            scheduleGrid.getColumnConstraints().add(doctorColumn);
        }
        // Добавляем временные слоты и ячейки расписания
        int row = 1; // Начинаем после заголовков
        for (LocalTime time = WORK_START_TIME; time.isBefore(WORK_END_TIME); time = time.plusMinutes(SLOT_DURATION_MINUTES)) {
            // Добавляем временной заголовок
            Label timeLabel = new Label(time.toString());
            timeLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center-right; -fx-padding: 0 5 0 0;");
            scheduleGrid.add(timeLabel, 0, row);
            // Добавляем ячейки для каждого врача
            for (int col = 0; col < doctorsToDisplay.size(); col++) {
                Doctor currentDoctor = doctorsToDisplay.get(col);
                StackPane cell = createScheduleCell(time, currentDoctor, appointmentsByTimeAndDoctor, selectedDate);
                scheduleGrid.add(cell, col + 1, row);
            }
            row++;
        }
    }

    /**
     * Создает ячейку расписания с соответствующим цветом и обработчиком событий.
     * @param time Время приема.
     * @param doctor Врач, к которому относится ячейка.
     * @param appointmentsByTimeAndDoctor Карта существующих записей.
     * @param date Дата расписания.
     * @return StackPane, представляющий ячейку расписания.
     */
    private StackPane createScheduleCell(LocalTime time, Doctor doctor,
                                         Map<LocalTime, Map<Integer, Appointment>> appointmentsByTimeAndDoctor,
                                         LocalDate date) {
        StackPane cell = new StackPane();
        cell.setPrefSize(100, 30);
        cell.setStyle("-fx-border-color: #ccc; -fx-border-width: 0.5px;");
        Appointment appointment = null;
        if (appointmentsByTimeAndDoctor.containsKey(time) && appointmentsByTimeAndDoctor.get(time).containsKey(doctor.getId())) {
            appointment = appointmentsByTimeAndDoctor.get(time).get(doctor.getId());
        }
        // Определяем цвет ячейки
        String color = "#ffffff"; // Белый по умолчанию (врач не принимает)
        String cellText = "";
        // Проверяем, находится ли время в рабочем диапазоне врача
        boolean isDoctorAvailableAtTime = !time.isBefore(WORK_START_TIME) && !time.isAfter(WORK_END_TIME.minusMinutes(SLOT_DURATION_MINUTES));
        if (isDoctorAvailableAtTime) {
            if (appointment != null) {
                // Есть запись
                if (userSession.isPatient() && userSession.getPatient().getId() == appointment.getPatientId()) {
                    color = "#d1ecf1"; // Голубой - ваша запись
                    cellText = "Ваша запись";
                } else {
                    color = "#f8d7da"; // Красный - занято другим пациентом
                    cellText = "Занято";
                }
            } else {
                color = "#d4edda"; // Зеленый - свободно
                cellText = "Свободно";
            }
        } else {
            color = "#ffffff"; // Белый - врач не принимает
            cellText = "Не принимает";
        }
        cell.setStyle(cell.getStyle() + String.format("-fx-background-color: %s;", color));
        Label statusLabel = new Label(cellText);
        statusLabel.setStyle("-fx-font-size: 10px;");
        cell.getChildren().add(statusLabel);
        // Добавляем обработчик кликов
        final Appointment finalAppointment = appointment;
        cell.setOnMouseClicked(event -> handleCellClick(time, doctor, finalAppointment, date));
        return cell;
    }

    /**
     * Обрабатывает клики по ячейкам расписания.
     * @param time Время слота.
     * @param doctor Врач, связанный со слотом.
     * @param appointment Существующая запись (может быть null).
     * @param date Дата расписания.
     */
    private void handleCellClick(LocalTime time, Doctor doctor, Appointment appointment, LocalDate date) {
        if (userSession.isPatient()) {
            // Логика для пациента
            if (appointment != null && userSession.getPatient().getId() == appointment.getPatientId()) {
                // Голубая ячейка - ваша запись
                showAppointmentDetails(appointment, doctor, userSession.getPatient());
            } else if (appointment == null && !time.isBefore(WORK_START_TIME) && !time.isAfter(WORK_END_TIME.minusMinutes(SLOT_DURATION_MINUTES))) {
                // Зеленая ячейка - свободно
                showBookAppointmentDialog(time, doctor, date);
            }
        } else if (userSession.isDoctor()) {
            // Логика для врача
            if (appointment != null) {
                // Красная ячейка (или голубая, если это его запись) - занято
                showPatientDetailsForDoctor(appointment);
            }
        }
    }

    /**
     * Показывает диалог с деталями записи для пациента.
     * @param appointment Запись.
     * @param doctor Врач.
     * @param patient Пациент.
     */
    private void showAppointmentDetails(Appointment appointment, Doctor doctor, Patient patient) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Детали вашей записи");
        alert.setHeaderText("Информация о приеме");
        String content = String.format(
                "Дата: %s\nВремя: %s\n\nВрач:\n  Фамилия: %s\n  Имя: %s\n  Отчество: %s\n  Специальность: %s\n  Кабинет: %s\n\nВаши данные:\n  Фамилия: %s\n  Имя: %s\n  Отчество: %s\n  Номер полиса: %s",
                appointment.getAppointmentDate(), appointment.getAppointmentTime(),
                doctor.getLastName(), doctor.getFirstName(), doctor.getMiddleName(), doctor.getSpecialty(), doctor.getOfficeNumber(),
                patient.getLastName(), patient.getFirstName(), patient.getMiddleName(), patient.getPolicyNumber()
        );
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Показывает диалог для записи на прием.
     * @param time Время приема.
     * @param doctor Врач.
     * @param date Дата приема.
     */
    private void showBookAppointmentDialog(LocalTime time, Doctor doctor, LocalDate date) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Запись на прием");
        alert.setHeaderText("Записаться к врачу?");
        alert.setContentText(String.format("Вы хотите записаться к %s %s %s (%s) на %s %s?",
                doctor.getLastName(), doctor.getFirstName(), doctor.getMiddleName(), doctor.getSpecialty(), date, time));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Создаем новую запись
            Appointment newAppointment = new Appointment(
                    userSession.getPatient().getId(),
                    doctor.getId(),
                    date,
                    time,
                    "Запланировано"
            );
            Request request = new Request(OperationType.ADD_APPOINTMENT, newAppointment);
            Response response = client.sendRequest(request);
            if (response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Успех", response.getMessage());
                displaySchedule(); // Обновить расписание
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка записи", response.getMessage());
            }
        }
    }

    /**
     * Показывает диалог с деталями пациента для врача.
     * @param appointment Запись, по которой нужно получить данные пациента.
     */
    private void showPatientDetailsForDoctor(Appointment appointment) {
        // Запрашиваем данные пациента по ID
        Request request = new Request(OperationType.GET_PATIENT_DETAILS, appointment.getPatientId());
        Response response = client.sendRequest(request);
        if (response.isSuccess() && response.getData() instanceof Patient) {
            Patient patient = (Patient) response.getData();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Детали пациента");
            alert.setHeaderText("Информация о пациенте, записанном на прием");
            String content = String.format(
                    "Фамилия: %s\nИмя: %s\nОтчество: %s\nДата рождения: %s\nАдрес: %s\nТелефон: %s\nНомер полиса: %s\n\nВремя приема: %s",
                    patient.getLastName(), patient.getFirstName(), patient.getMiddleName(), patient.getDateOfBirth(),
                    patient.getAddress(), patient.getPhone(), patient.getPolicyNumber(),
                    appointment.getAppointmentTime()
            );
            alert.setContentText(content);
            alert.showAndWait();
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить данные пациента: " + response.getMessage());
        }
    }

    /**
     * Показывает диалоговое окно с сообщением.
     * @param type Тип оповещения (INFORMATION, ERROR, WARNING).
     * @param title Заголовок окна.
     * @param message Текст сообщения.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
