package com.group01.dhsa.Model.FhirResources;

import java.util.List;
import java.util.Map;

/**
 * Interfaccia comune per tutti gli exporter FHIR.
 */
public interface FhirResourceExporter {

    /**
     * Esporta tutte le risorse.
     *
     * @return Una lista di mappe contenenti i dati delle risorse.
     */
    List<Map<String, String>> exportResources();

    /**
     * Cerca risorse in base a un termine di ricerca.
     *
     * @param searchTerm Il termine di ricerca.
     * @return Una lista di mappe contenenti i dati delle risorse corrispondenti.
     */
    List<Map<String, String>> searchResources(String searchTerm);

    List<Map<String, String>> searchResources(String searchField, String searchValue);
}
