package com.client;

import com.common.model.Doctor;
import com.common.model.Patient;

public class UserSession {
    private Patient patient;
    private Doctor doctor;
    private boolean isPatient;
    private boolean isDoctor;

    public UserSession() {
        this.patient = null;
        this.doctor = null;
        this.isPatient = false;
        this.isDoctor = false;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public boolean isPatient() {
        return isPatient;
    }

    public void setPatient(boolean patient) {
        isPatient = patient;
    }

    public boolean isDoctor() {
        return isDoctor;
    }

    public void setDoctor(boolean doctor) {
        isDoctor = doctor;
    }

    /**
     * Проверяет, вошел ли пользователь в систему.
     * @return true, если пользователь вошел (как пациент или врач), иначе false.
     */
    public boolean isLoggedIn() {
        return isPatient || isDoctor;
    }
}

