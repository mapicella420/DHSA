package com.group01.dhsa.Model;

import com.group01.dhsa.FHIRClient;

/**
 * This class represents the logged-in user in the system,
 * utilizing the Singleton design pattern to ensure a single instance exists.
 * It stores the FHIR ID of the current user for session management.
 */
public class LoggedUser {

    // The single instance of the LoggedUser class
    private static LoggedUser instance;

    // The FHIR ID of the logged-in user
    private static String fhirId;

    // The organization
    private static String organization;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * This enforces the Singleton pattern by restricting object creation.
     */
    private LoggedUser() {}

    /**
     * Retrieves the single instance of the LoggedUser class.
     * If the instance does not exist, it creates a new one.
     *
     * @return The singleton instance of LoggedUser.
     */
    public static LoggedUser getInstance() {
        if (instance == null) {
            instance = new LoggedUser(); // Create the instance if it does not exist
        }
        return instance;
    }

    /**
     * Gets the FHIR ID of the logged-in user.
     *
     * @return The FHIR ID as a String, or null if no user is logged in.
     */
    public String getFhirId() {
        return fhirId;
    }

    /**
     * Sets the FHIR ID of the logged-in user.
     *
     * @param fhirId The FHIR ID to associate with the logged-in user.
     */
    public void setFhirId(String fhirId) {
        LoggedUser.fhirId = fhirId;
    }

    /**
     * Logs out the current user by clearing the stored FHIR ID.
     * This effectively ends the user's session.
     */
    public void logout() {
        fhirId = null; // Clear the FHIR ID
        organization = null;
        FHIRClient.removeClient();
    }

    public static String getOrganization() {
        return organization;
    }

    public static void setOrganization(String organization) {
        LoggedUser.organization = organization;
    }
}
