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

    /**
     * Cerca risorse in base a un campo specifico e un valore.
     *
     * @param searchField Il campo da cercare.
     * @param searchValue Il valore da cercare.
     * @return Una lista di mappe contenenti i dati delle risorse corrispondenti.
     */
    List<Map<String, String>> searchResources(String searchField, String searchValue);

    /**
     * Converte una risorsa in una mappa.
     *
     * @param resource La risorsa FHIR da convertire.
     * @return Una mappa contenente i dati della risorsa.
     */
    Map<String, String> convertResourceToMap(Object resource);
}
