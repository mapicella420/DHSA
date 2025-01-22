package com.group01.dhsa.Model;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class CdaToHtmlConverter {
    private final EventObservable eventObservable;

    public CdaToHtmlConverter() {
        this.eventObservable = EventManager.getInstance().getEventObservable();
    }

    public void convertAndNotify(File cdaFile) {
        try {
            System.out.println("[DEBUG] Starting conversion for file: " + cdaFile.getAbsolutePath());

            // Usa il ClassLoader per ottenere il percorso del template e dello stile
            URL templateUrl = getClass().getResource("/com/group01/dhsa/PDFTools/CDALetterTemplate.html");
            URL cssUrl = getClass().getResource("/com/group01/dhsa/styles/CDAStyles.css");

            if (templateUrl == null || cssUrl == null) {
                throw new IllegalArgumentException("Template o CSS non trovati nel percorso specificato.");
            }

            System.out.println("[DEBUG] Template path: " + templateUrl);
            System.out.println("[DEBUG] CSS path: " + cssUrl);

            // Leggi il contenuto del file template
            Path templatePath = Paths.get(templateUrl.toURI());
            String templateContent = Files.readString(templatePath);

            // Leggi il contenuto del file CSS
            Path cssPath = Paths.get(cssUrl.toURI());
            String cssContent = Files.readString(cssPath);

            // Inserisci lo stile CSS all'inizio del file HTML
            templateContent = templateContent.replace("</head>", "<style>" + cssContent + "</style></head>");

            // Estrai i dati dal file CDA
            Map<String, String> cdaData = new CdaDataExtractor().extractCdaData(cdaFile);
            System.out.println("[DEBUG] Extracted CDA data: " + cdaData);

            // Popola il template con i dati estratti
            String populatedHtml = populateTemplate(templateContent, cdaData);

            // Crea un file temporaneo per l'HTML generato
            Path tempHtmlPath = Files.createTempFile("CDA_Preview", ".html");
            Files.writeString(tempHtmlPath, populatedHtml);

            System.out.println("[DEBUG] Generated HTML file: " + tempHtmlPath.toAbsolutePath());

            // Notifica il completamento della conversione
            eventObservable.notify("html_generated", tempHtmlPath.toFile());
        } catch (Exception e) {
            e.printStackTrace();
            // In caso di errore, notifica il fallimento
            System.out.println("[ERROR] HTML generation failed: " + e.getMessage());
            eventObservable.notify("html_generation_failed", null);
        }
    }

    private String populateTemplate(String templateContent, Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            templateContent = templateContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return templateContent;
    }
}
