package com.group01.dhsa.Model.CDAResources.AdapterPattern;

/**
 * Adapter interface for converting FHIR objects into CDA section objects.
 * This interface follows the Adapter design pattern, enabling the transformation
 * of FHIR objects (source) into CDA objects (target) while maintaining loose coupling.
 *
 * @param <T> The type of the CDA object (target format).
 * @param <U> The type of the FHIR object (source format).
 */
public interface CdaSection<T, U> {

    /**
     * Converts a FHIR object into a CDA section object.
     * This method takes an object in the FHIR format and transforms it
     * into the corresponding CDA representation.
     *
     * @param fhirObject The FHIR object to be converted.
     * @return The corresponding CDA object.
     */
    T toCdaObject(U fhirObject);
}
