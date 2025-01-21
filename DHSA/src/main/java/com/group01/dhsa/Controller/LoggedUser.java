package com.group01.dhsa.Controller;

public class LoggedUser {
    private static LoggedUser instance;
    private static String fhirId;  // Memorizza l'ID FHIR

    private LoggedUser() {}


    public static LoggedUser getInstance() {
        if (instance == null) {
            instance = new LoggedUser();
        }
        return instance;
    }


    public String getFhirId() {
        return fhirId;
    }

    public void setFhirId(String fhirId) {
        LoggedUser.fhirId = fhirId;
    }

    public void logout() {
        fhirId = null;
    }

}
