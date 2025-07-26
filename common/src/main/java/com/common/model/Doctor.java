package com.common.model;

import java.io.Serializable;

/**
 * Модель данных для врача.
 * Реализует Serializable для передачи объектов по сети.
 */
public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L; // Для сериализации

    private int id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String specialty;
    private String officeNumber;

    public Doctor() {
    }

    public Doctor(int id, String lastName, String firstName, String middleName, String specialty, String officeNumber) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.specialty = specialty;
        this.officeNumber = officeNumber;
    }

    // Конструктор без ID для новых врачей
    public Doctor(String lastName, String firstName, String middleName, String specialty, String officeNumber) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.specialty = specialty;
        this.officeNumber = officeNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getOfficeNumber() {
        return officeNumber;
    }

    public void setOfficeNumber(String officeNumber) {
        this.officeNumber = officeNumber;
    }

    @Override
    public String toString() {
        return "Врач{" +
                "id=" + id +
                ", Фамилия='" + lastName + '\'' +
                ", Имя='" + firstName + '\'' +
                ", Отчество='" + middleName + '\'' +
                ", Специальность='" + specialty + '\'' +
                ", Кабинет='" + officeNumber + '\'' +
                '}';
    }
}