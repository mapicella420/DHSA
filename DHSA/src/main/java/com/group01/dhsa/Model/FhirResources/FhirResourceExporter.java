package com.group01.dhsa.Model.FhirResources;

import java.util.List;
import java.util.Map;

/**
 * Common interface for all FHIR resource exporters.
 * This interface defines methods to export, search, and convert FHIR resources into a mappable format.
 */
public interface FhirResourceExporter {

    /**
     * Exports all resources.
     * This method retrieves all available resources and returns them as a list of maps.
     *
     * @return A list of maps where each map represents the data of a FHIR resource.
     */
    List<Map<String, String>> exportResources();

    /**
     * Searches resources based on a search term.
     * This method allows searching for resources by specifying a general search term.
     *
     * @param searchTerm The term to search for.
     * @return A list of maps representing the data of resources that match the search term.
     */
    List<Map<String, String>> searchResources(String searchTerm);

    /**
     * Searches resources based on specific fields and their corresponding values.
     * This method performs field-specific searches, where each field is matched against a specific value.
     *
     * @param searchField The fields to search within (e.g., "name", "id").
     * @param searchValue The values to search for in the specified fields.
     * @return A list of maps representing the data of resources that match the search criteria.
     */
    List<Map<String, String>> searchResources(String[] searchField, String[] searchValue);

    /**
     * Converts a FHIR resource into a map.
     * This method takes a FHIR resource object and extracts its data into a key-value format.
     *
     * @param resource The FHIR resource to convert.
     * @return A map containing the key-value representation of the resource data.
     */
    Map<String, String> convertResourceToMap(Object resource);
}
