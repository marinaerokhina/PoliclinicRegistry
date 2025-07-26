package com.server;

import com.common.model.Appointment;
import com.common.model.Doctor;
import com.common.model.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для управления взаимодействием с базой данных H2.
 * Содержит методы для выполнения CRUD-операций над сущностями Patient, Doctor, Appointment.
 */
public class DatabaseManager {

    private static final String JDBC_URL = "jdbc:h2:./polyclinic_registry_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public DatabaseManager() {
        initializeDatabase();
    }

    /**
     * Инициализирует базу данных, создавая таблицы, если они не существуют,
     * и заполняя их тестовыми данными программно.
     */
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            System.out.println("Подключение к базе данных H2...");

            // Создание таблицы Пациенты
            stmt.execute("CREATE TABLE IF NOT EXISTS Patients (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "lastName VARCHAR(255) NOT NULL," +
                    "firstName VARCHAR(255) NOT NULL," +
                    "middleName VARCHAR(255)," +
                    "dateOfBirth DATE," +
                    "address VARCHAR(255)," +
                    "phone VARCHAR(20)," +
                    "policyNumber VARCHAR(50) UNIQUE NOT NULL" +
                    ")");

            // Создание таблицы Врачи
            stmt.execute("CREATE TABLE IF NOT EXISTS Doctors (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "lastName VARCHAR(255) NOT NULL," +
                    "firstName VARCHAR(255) NOT NULL," +
                    "middleName VARCHAR(255)," +
                    "specialty VARCHAR(100) NOT NULL," +
                    "officeNumber VARCHAR(10)" +
                    ")");

            // Создание таблицы Записи на прием
            stmt.execute("CREATE TABLE IF NOT EXISTS Appointments (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "patientId INT NOT NULL," +
                    "doctorId INT NOT NULL," +
                    "appointmentDate DATE NOT NULL," +
                    "appointmentTime TIME NOT NULL," +
                    "status VARCHAR(50) DEFAULT 'Запланировано'," +
                    "FOREIGN KEY (patientId) REFERENCES Patients(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (doctorId) REFERENCES Doctors(id) ON DELETE CASCADE," +
                    "UNIQUE (doctorId, appointmentDate, appointmentTime)" + // Запрет на дублирование записей
                    ")");

            // Добавление тестовых данных для пациентов
            // Перед добавлением проверяем, существует ли пациент с таким номером полиса
            if (getPatientByPolicyNumber("POL1234567890") == null) {
                addPatient(new Patient("Иванов", "Иван", "Иванович", LocalDate.of(1990, 5, 15), "ул. Пушкина, д.1", "+79001234567", "POL1234567890"));
            }
            if (getPatientByPolicyNumber("POL0987654321") == null) {
                addPatient(new Patient("Петрова", "Анна", "Сергеевна", LocalDate.of(1985, 11, 20), "пр. Ленина, д.10", "+79012345678", "POL0987654321"));
            }
            if (getPatientByPolicyNumber("POL9876543210") == null) {
                addPatient(new Patient("Сидорова", "Мария", "Ивановна", LocalDate.of(1992, 3, 1), "ул. Цветочная, д.5", "+79023456789", "POL9876543210"));
            }


            // Добавление тестовых данных для врачей
            // Перед добавлением проверяем, существует ли врач с таким ФИО
            if (getDoctorIdByFIO("Сидоров", "Петр", "Алексеевич") == -1) {
                addDoctor(new Doctor("Сидоров", "Петр", "Алексеевич", "Терапевт", "101"));
            }
            if (getDoctorIdByFIO("Кузнецова", "Елена", "Игоревна") == -1) {
                addDoctor(new Doctor("Кузнецова", "Елена", "Игоревна", "Хирург", "205"));
            }
            if (getDoctorIdByFIO("Смирнов", "Дмитрий", "Викторович") == -1) {
                addDoctor(new Doctor("Смирнов", "Дмитрий", "Викторович", "Окулист", "303"));
            }
            if (getDoctorIdByFIO("Волкова", "Ольга", "Николаевна") == -1) {
                addDoctor(new Doctor("Волкова", "Ольга", "Николаевна", "УЗИ", "401"));
            }


            // Добавление тестовых данных для записей на прием
            // Перед добавлением проверяем, существует ли запись на это время
            int patientId1 = getPatientIdByFIO("Иванов", "Иван", "Иванович");
            int patientId2 = getPatientIdByFIO("Петрова", "Анна", "Сергеевна");
            int doctorId1 = getDoctorIdByFIO("Сидоров", "Петр", "Алексеевич");
            int doctorId2 = getDoctorIdByFIO("Кузнецова", "Елена", "Игоревна");
            int doctorId3 = getDoctorIdByFIO("Смирнов", "Дмитрий", "Викторович");
            int doctorId4 = getDoctorIdByFIO("Волкова", "Ольга", "Николаевна");

            if (patientId1 != -1 && doctorId1 != -1 && !isAppointmentSlotTaken(doctorId1, LocalDate.now(), LocalTime.of(9, 0))) {
                addAppointment(new Appointment(patientId1, doctorId1, LocalDate.now(), LocalTime.of(9, 0), "Запланировано"));
            }
            if (patientId2 != -1 && doctorId2 != -1 && !isAppointmentSlotTaken(doctorId2, LocalDate.now(), LocalTime.of(10, 30))) {
                addAppointment(new Appointment(patientId2, doctorId2, LocalDate.now(), LocalTime.of(10, 30), "Запланировано"));
            }
            if (patientId1 != -1 && doctorId3 != -1 && !isAppointmentSlotTaken(doctorId3, LocalDate.now(), LocalTime.of(11, 0))) {
                addAppointment(new Appointment(patientId1, doctorId3, LocalDate.now(), LocalTime.of(11, 0), "Запланировано"));
            }
            if (patientId2 != -1 && doctorId4 != -1 && !isAppointmentSlotTaken(doctorId4, LocalDate.now().plusDays(1), LocalTime.of(14, 0))) {
                addAppointment(new Appointment(patientId2, doctorId4, LocalDate.now().plusDays(1), LocalTime.of(14, 0), "Запланировано"));
            }


            System.out.println("База данных H2 инициализирована программно.");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Вспомогательный метод для получения ID пациента по ФИО.
     * Используется для инициализации тестовых данных.
     */
    private int getPatientIdByFIO(String lastName, String firstName, String middleName) {
        String sql = "SELECT id FROM Patients WHERE lastName = ? AND firstName = ? AND middleName = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID пациента: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Вспомогательный метод для получения ID врача по ФИО.
     * Используется для инициализации тестовых данных.
     */
    private int getDoctorIdByFIO(String lastName, String firstName, String middleName) {
        String sql = "SELECT id FROM Doctors WHERE lastName = ? AND firstName = ? AND middleName = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID врача: " + e.getMessage());
        }
        return -1;
    }


    /**
     * Получает соединение с базой данных.
     * @return Объект Connection.
     * @throws SQLException Если произошла ошибка SQL.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * Добавляет нового пациента в базу данных.
     * @param patient Объект Patient для добавления.
     * @return Добавленный пациент с присвоенным ID, или null в случае ошибки.
     */
    public Patient addPatient(Patient patient) {
        String sql = "INSERT INTO Patients (lastName, firstName, middleName, dateOfBirth, address, phone, policyNumber) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, patient.getLastName());
            pstmt.setString(2, patient.getFirstName());
            pstmt.setString(3, patient.getMiddleName());
            pstmt.setDate(4, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getPhone());
            pstmt.setString(7, patient.getPolicyNumber());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        patient.setId(generatedKeys.getInt(1));
                        System.out.println("Пациент добавлен: " + patient.getLastName());
                        return patient;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пациента: " + e.getMessage());
            // Дополнительная обработка для UNIQUE-конфликта (например, если policyNumber уже существует)
            if (e.getSQLState().startsWith("23")) {
                System.err.println("Возможно, пациент с таким номером полиса уже существует.");
            }
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получает всех пациентов из базы данных.
     * @return Список объектов Patient.
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM Patients";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("lastName"),
                        rs.getString("firstName"),
                        rs.getString("middleName"),
                        rs.getDate("dateOfBirth").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("policyNumber")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех пациентов: " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    /**
     * Обновляет данные существующего пациента.
     * @param patient Объект Patient с обновленными данными.
     * @return true, если обновление прошло успешно, иначе false.
     */
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE Patients SET lastName = ?, firstName = ?, middleName = ?, dateOfBirth = ?, address = ?, phone = ?, policyNumber = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getLastName());
            pstmt.setString(2, patient.getFirstName());
            pstmt.setString(3, patient.getMiddleName());
            pstmt.setDate(4, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getPhone());
            pstmt.setString(7, patient.getPolicyNumber());
            pstmt.setInt(8, patient.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Пациент обновлен: " + patient.getLastName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении пациента: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Удаляет пациента по ID.
     * @param patientId ID пациента для удаления.
     * @return true, если удаление прошло успешно, иначе false.
     */
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM Patients WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Пациент с ID " + patientId + " удален.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пациента: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Ищет пациентов по ФИО, дате рождения или номеру полиса.
     * @param query Строка запроса для поиска.
     * @return Список найденных пациентов.
     */
    public List<Patient> searchPatients(String query) {
        List<Patient> patients = new ArrayList<>();
        // Расширенный поиск, учитывающий частичное совпадение по всем строковым полям
        String sql = "SELECT * FROM Patients WHERE " +
                "LOWER(lastName) LIKE LOWER(?) OR " +
                "LOWER(firstName) LIKE LOWER(?) OR " +
                "LOWER(middleName) LIKE LOWER(?) OR " +
                "LOWER(policyNumber) LIKE LOWER(?) OR " +
                "LOWER(address) LIKE LOWER(?) OR " +
                "LOWER(phone) LIKE LOWER(?) OR " +
                "dateOfBirth = ?"; // Для поиска по дате, предполагаем формат 'YYYY-MM-DD'

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String likeQuery = "%" + query + "%";
            pstmt.setString(1, likeQuery);
            pstmt.setString(2, likeQuery);
            pstmt.setString(3, likeQuery);
            pstmt.setString(4, likeQuery);
            pstmt.setString(5, likeQuery);
            pstmt.setString(6, likeQuery);
            try {
                LocalDate date = LocalDate.parse(query);
                pstmt.setDate(7, Date.valueOf(date));
            } catch (Exception e) {
                pstmt.setNull(7, Types.DATE); // Если не дата, игнорируем поиск по дате
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(new Patient(
                            rs.getInt("id"),
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getDate("dateOfBirth") != null ? rs.getDate("dateOfBirth").toLocalDate() : null,
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("policyNumber")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пациентов: " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    /**
     * Получает пациента по его ID.
     * @param patientId ID пациента.
     * @return Объект Patient, если найден, иначе null.
     */
    public Patient getPatientById(int patientId) {
        String sql = "SELECT * FROM Patients WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                            rs.getInt("id"),
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getDate("dateOfBirth") != null ? rs.getDate("dateOfBirth").toLocalDate() : null,
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("policyNumber")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении пациента по ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получает пациента по его номеру полиса.
     * @param policyNumber Номер полиса пациента.
     * @return Объект Patient, если найден, иначе null.
     */
    public Patient getPatientByPolicyNumber(String policyNumber) {
        String sql = "SELECT * FROM Patients WHERE policyNumber = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, policyNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                            rs.getInt("id"),
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getDate("dateOfBirth") != null ? rs.getDate("dateOfBirth").toLocalDate() : null,
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("policyNumber")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении пациента по номеру полиса: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Аутентифицирует пациента по ФИО.
     * @param lastName Фамилия пациента.
     * @param firstName Имя пациента.
     * @param middleName Отчество пациента.
     * @return Объект Patient, если найден и ФИО совпадает, иначе null.
     */
    public Patient loginPatient(String lastName, String firstName, String middleName) {
        String sql = "SELECT * FROM Patients WHERE LOWER(lastName) = LOWER(?) AND LOWER(firstName) = LOWER(?) AND LOWER(middleName) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                            rs.getInt("id"),
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getDate("dateOfBirth") != null ? rs.getDate("dateOfBirth").toLocalDate() : null,
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("policyNumber")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при входе пациента: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Добавляет нового врача в базу данных.
     * @param doctor Объект Doctor для добавления.
     * @return Добавленный врач с присвоенным ID, или null в случае ошибки.
     */
    public Doctor addDoctor(Doctor doctor) {
        String sql = "INSERT INTO Doctors (lastName, firstName, middleName, specialty, officeNumber) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, doctor.getLastName());
            pstmt.setString(2, doctor.getFirstName());
            pstmt.setString(3, doctor.getMiddleName());
            pstmt.setString(4, doctor.getSpecialty());
            pstmt.setString(5, doctor.getOfficeNumber());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        doctor.setId(generatedKeys.getInt(1));
                        System.out.println("Врач добавлен: " + doctor.getLastName());
                        return doctor;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении врача: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Получает всех врачей из базы данных.
     * @return Список объектов Doctor.
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM Doctors";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getInt("id"),
                        rs.getString("lastName"),
                        rs.getString("firstName"),
                        rs.getString("middleName"),
                        rs.getString("specialty"),
                        rs.getString("officeNumber")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех врачей: " + e.getMessage());
            e.printStackTrace();
        }
        return doctors;
    }

    /**
     * Получает врача по его ID.
     * @param doctorId ID врача.
     * @return Объект Doctor, если найден, иначе null.
     */
    public Doctor getDoctorById(int doctorId) {
        String sql = "SELECT * FROM Doctors WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                            rs.getInt("id"),
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getString("specialty"),
                            rs.getString("officeNumber")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении врача по ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Аутентифицирует врача по ФИО.
     * @param lastName Фамилия врача.
     * @param firstName Имя врача.
     * @param middleName Отчество врача.
     * @return Объект Doctor, если найден и ФИО совпадает, иначе null.
     */
    public Doctor loginDoctor(String lastName, String firstName, String middleName) {
        String sql = "SELECT * FROM Doctors WHERE LOWER(lastName) = LOWER(?) AND LOWER(firstName) = LOWER(?) AND LOWER(middleName) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                            rs.getInt("id"),
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getString("specialty"),
                            rs.getString("officeNumber")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при входе врача: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Добавляет новую запись на прием в базу данных.
     * @param appointment Объект Appointment для добавления.
     * @return Добавленная запись с присвоенным ID, или null в случае ошибки.
     */
    public Appointment addAppointment(Appointment appointment) {
        String sql = "INSERT INTO Appointments (patientId, doctorId, appointmentDate, appointmentTime, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            pstmt.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
            pstmt.setString(5, appointment.getStatus());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        appointment.setId(generatedKeys.getInt(1));
                        System.out.println("Запись на прием добавлена: " + appointment);
                        return appointment;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении записи на прием: " + e.getMessage());
            // Проверка на UNIQUE-конфликт (если слот уже занят)
            if (e.getSQLState().startsWith("23")) {
                System.err.println("Попытка добавить запись на уже занятое время.");
            }
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Проверяет, занят ли слот приема для конкретного врача на определенную дату и время.
     * @param doctorId ID врача.
     * @param date Дата приема.
     * @param time Время приема.
     * @return true, если слот занят, иначе false.
     */
    public boolean isAppointmentSlotTaken(int doctorId, LocalDate date, LocalTime time) {
        String sql = "SELECT COUNT(*) FROM Appointments WHERE doctorId = ? AND appointmentDate = ? AND appointmentTime = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setTime(3, Time.valueOf(time));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке занятости слота: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получает записи на прием для конкретного врача на определенную дату.
     * @param doctorId ID врача.
     * @param date Дата приема.
     * @return Список объектов Appointment.
     */
    public List<Appointment> getAppointmentsByDoctorAndDate(int doctorId, LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM Appointments WHERE doctorId = ? AND appointmentDate = ? ORDER BY appointmentTime";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                            rs.getInt("id"),
                            rs.getInt("patientId"),
                            rs.getInt("doctorId"),
                            rs.getDate("appointmentDate").toLocalDate(),
                            rs.getTime("appointmentTime").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении записей для врача и даты: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    /**
     * Получает все записи на прием для указанной даты (для всех врачей).
     * @param date Дата, для которой нужно получить расписание.
     * @return Список объектов Appointment.
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM Appointments WHERE appointmentDate = ? ORDER BY doctorId, appointmentTime";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                            rs.getInt("id"),
                            rs.getInt("patientId"),
                            rs.getInt("doctorId"),
                            rs.getDate("appointmentDate").toLocalDate(),
                            rs.getTime("appointmentTime").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении полного расписания на дату: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    /**
     * Отменяет запись на прием по ID.
     * @param appointmentId ID записи для отмены.
     * @return true, если отмена прошла успешно, иначе false.
     */
    public boolean cancelAppointment(int appointmentId) {
        String sql = "UPDATE Appointments SET status = 'Отменено' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Запись на прием с ID " + appointmentId + " отменена.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при отмене записи на прием: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получает историю записей для конкретного пациента.
     * @param patientId ID пациента.
     * @return Список объектов Appointment.
     */
    public List<Appointment> getPatientAppointmentHistory(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM Appointments WHERE patientId = ? ORDER BY appointmentDate DESC, appointmentTime DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                            rs.getInt("id"),
                            rs.getInt("patientId"),
                            rs.getInt("doctorId"),
                            rs.getDate("appointmentDate").toLocalDate(),
                            rs.getTime("appointmentTime").toLocalTime(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении истории записей пациента: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }
}
