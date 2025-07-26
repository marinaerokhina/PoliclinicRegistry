package com.common.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Модель данных для пациента.
 * Реализует Serializable для передачи объектов по сети.
 */
public class Patient implements Serializable {
    private static final long serialVersionUID = 1L; // Для сериализации

    private int id;
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String policyNumber;

    public Patient() {
    }

    public Patient(int id, String lastName, String firstName, String middleName, LocalDate dateOfBirth, String address, String phone, String policyNumber) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.policyNumber = policyNumber;
    }

    // Конструктор без ID для новых пациентов
    public Patient(String lastName, String firstName, String middleName, LocalDate dateOfBirth, String address, String phone, String policyNumber) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.policyNumber = policyNumber;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String setAddress(String address) {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    @Override
    public String toString() {
        return "Пациент{" +
                "id=" + id +
                ", Фамилия='" + lastName + '\'' +
                ", Имя='" + firstName + '\'' +
                ", Отчество='" + middleName + '\'' +
                ", Дата рождения=" + dateOfBirth +
                ", Адрес='" + address + '\'' +
                ", Телефон='" + phone + '\'' +
                ", Номер полиса='" + policyNumber + '\'' +
                '}';
    }
}
