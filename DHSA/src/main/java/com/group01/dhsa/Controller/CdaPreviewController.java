package com.group01.dhsa.Controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CdaPreviewController {

    @FXML
    private WebView webView;

    private File cdaFile;

    /**
     * Imposta il file CDA da visualizzare.
     * @param cdaFile File CDA
     */
    public void setCdaFile(File cdaFile) {
        this.cdaFile = cdaFile;
        renderCdaAsHtml(cdaFile);
    }

    /**
     * Carica il template HTML, sostituisce i segnaposto con i dati CDA e mostra il risultato.
     */
    private void renderCdaAsHtml(File cdaFile) {
        try {
            // Usa il ClassLoader per ottenere il percorso del template
            URL templateUrl = getClass().getResource("/com/group01/dhsa/PDFTools/CDALetterTemplate.html");
            if (templateUrl == null) {
                throw new IllegalArgumentException("Template CDA non trovato nel percorso specificato.");
            }

            // Leggi il contenuto del file template
            Path templatePath = Paths.get(templateUrl.toURI());
            String templateContent = Files.readString(templatePath);

            // Estrai i dati dal file CDA
            Map<String, String> cdaData = extractCdaData(cdaFile);

            // Popola il template con i dati estratti
            String populatedHtml = populateTemplate(templateContent, cdaData);

            // Mostra l'HTML popolato nella WebView
            webView.getEngine().loadContent(populatedHtml);
        } catch (Exception e) {
            e.printStackTrace();
            webView.getEngine().loadContent("<html><body><h1>Error rendering CDA file</h1></body></html>");
        }
    }

    /**
     * Analizza il file CDA e restituisce i dati estratti.
     * @param cdaFile File XML del CDA.
     * @return Mappa dei dati estratti.
     */
    private Map<String, String> extractCdaData(File cdaFile) {
        Map<String, String> data = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cdaFile);
            doc.getDocumentElement().normalize();

            // General document information
            Element clinicalDocument = doc.getDocumentElement();

// Template ID
            data.put("templateId", clinicalDocument.getElementsByTagName("templateId")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());

// Type ID
            data.put("typeId", clinicalDocument.getElementsByTagName("typeId")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());

// ID Documento e Authority Name
            Element idElement = (Element) clinicalDocument.getElementsByTagName("id").item(0);
            data.put("documentId", idElement.getAttribute("root"));
            data.put("assigningAuthorityName", idElement.getAttribute("assigningAuthorityName"));

// Realm Code
            data.put("realmCode", clinicalDocument.getElementsByTagName("realmCode")
                    .item(0).getAttributes().getNamedItem("code").getNodeValue());

// Tipo Documento
            data.put("documentType", clinicalDocument.getElementsByTagName("code")
                    .item(0).getAttributes().getNamedItem("displayName").getNodeValue());
            data.put("codeSystemName", clinicalDocument.getElementsByTagName("code")
                    .item(0).getAttributes().getNamedItem("codeSystemName").getNodeValue());

// Codice Riservatezza
            data.put("confidentialityCode", clinicalDocument.getElementsByTagName("confidentialityCode")
                    .item(0).getAttributes().getNamedItem("displayName").getNodeValue());

// Data Creazione
            data.put("creationDate", formatDate(clinicalDocument.getElementsByTagName("effectiveTime")
                    .item(0).getAttributes().getNamedItem("value").getNodeValue()));

// Lingua
            data.put("languageCode", clinicalDocument.getElementsByTagName("languageCode")
                    .item(0).getAttributes().getNamedItem("code").getNodeValue());

// Versione
            data.put("versionNumber", clinicalDocument.getElementsByTagName("versionNumber")
                    .item(0).getAttributes().getNamedItem("value").getNodeValue());




            // Dati del paziente
            Element patientRoleElement = (Element) doc.getElementsByTagName("patientRole").item(0);
            Element patientElement = (Element) patientRoleElement.getElementsByTagName("patient").item(0);
            Element addressElement = (Element) patientRoleElement.getElementsByTagName("addr").item(0);
            Element idElementP = (Element) patientRoleElement.getElementsByTagName("id").item(0);

// Nome e genere del paziente
            data.put("patientGiven", getTextContentByTagName(patientElement, "given"));
            data.put("patientFamily", getTextContentByTagName(patientElement, "family"));
            data.put("patientGender", patientElement.getElementsByTagName("administrativeGenderCode")
                    .item(0).getAttributes().getNamedItem("displayName").getNodeValue());

// Data di nascita
            data.put("patientBirthTime", patientElement.getElementsByTagName("birthTime")
                    .item(0).getAttributes().getNamedItem("value").getNodeValue());

// Indirizzo del paziente
            data.put("patientAddress", getTextContentByTagName(addressElement, "streetAddressLine"));
            data.put("patientCountry", getTextContentByTagName(addressElement, "country"));
            data.put("patientState", getTextContentByTagName(addressElement, "state"));
            data.put("patientCity", getTextContentByTagName(addressElement, "city"));

            // ID del paziente e autorità assegnante
            data.put("patientId", idElementP.getAttribute("extension"));

            // Informazioni sull'autore
            Element authorElement = (Element) doc.getElementsByTagName("assignedAuthor").item(0);

            // Nome dell'autore
            Element authorNameElement = (Element) authorElement.getElementsByTagName("name").item(0);
            data.put("authorGiven", getTextContentByTagName(authorNameElement, "given"));
            data.put("authorFamily", getTextContentByTagName(authorNameElement, "family"));

            // ID Autore
            data.put("authorId", authorElement.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());

            // Autorità Assegnante
            data.put("assigningAuthorityName", authorElement.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("assigningAuthorityName").getNodeValue());

            // Timestamp
            Element authorTimeElement = (Element) doc.getElementsByTagName("author").item(0);
            if (authorTimeElement != null && authorTimeElement.hasAttribute("time")) {
                data.put("authorTime", formatDate(authorTimeElement.getAttribute("time")));
            } else {
                data.put("authorTime", "Non disponibile");
            }

            // Custode del Documento
            Element custodianElement = (Element) doc.getElementsByTagName("representedCustodianOrganization").item(0);
            data.put("custodianName", getTextContentByTagName(custodianElement, "name"));

            Element custodianIdElement = (Element) custodianElement.getElementsByTagName("id").item(0);
            data.put("custodianId", custodianIdElement.getAttributes().getNamedItem("extension").getNodeValue());
            data.put("custodianAuthorityName", custodianIdElement.getAttributes().getNamedItem("assigningAuthorityName").getNodeValue());

            Element custodianAddrElement = (Element) custodianElement.getElementsByTagName("addr").item(0);
            data.put("custodianStreet", getTextContentByTagName(custodianAddrElement, "streetAddressLine"));
            data.put("custodianCity", getTextContentByTagName(custodianAddrElement, "city"));
            data.put("custodianState", getTextContentByTagName(custodianAddrElement, "state"));
            data.put("custodianCountry", getTextContentByTagName(custodianAddrElement, "country"));

            data.put("custodianAddress", data.get("custodianStreet") + ", " + data.get("custodianCity") + ", " +
                    data.get("custodianState") + ", " + data.get("custodianCountry"));

            // Legale Autenticatore
            Element legalAuthElement = (Element) doc.getElementsByTagName("assignedEntity").item(0);
            data.put("authenticatorId", legalAuthElement.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());
            data.put("authenticatorAuthorityName", legalAuthElement.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("assigningAuthorityName").getNodeValue());

            Element legalAddrElement = (Element) legalAuthElement.getElementsByTagName("addr").item(0);
            data.put("authenticatorStreet", getTextContentByTagName(legalAddrElement, "streetAddressLine"));
            data.put("authenticatorCity", getTextContentByTagName(legalAddrElement, "city"));
            data.put("authenticatorState", getTextContentByTagName(legalAddrElement, "state"));
            data.put("authenticatorCountry", getTextContentByTagName(legalAddrElement, "country"));

            data.put("authenticatorAddress", data.get("authenticatorStreet") + ", " + data.get("authenticatorCity") + ", " +
                    data.get("authenticatorState") + ", " + data.get("authenticatorCountry"));

            Element authenticatorPersonElement = (Element) legalAuthElement.getElementsByTagName("assignedPerson").item(0);
            data.put("authenticatorGiven", getTextContentByTagName(authenticatorPersonElement, "given"));
            data.put("authenticatorFamily", getTextContentByTagName(authenticatorPersonElement, "family"));

            Element representedOrgElement = (Element) legalAuthElement.getElementsByTagName("representedOrganization").item(0);
            data.put("authenticatorOrgName", getTextContentByTagName(representedOrgElement, "name"));
            data.put("authenticatorOrgTelecom", representedOrgElement.getElementsByTagName("telecom").item(0)
                    .getAttributes().getNamedItem("value").getNodeValue());

// Data e firma del legale autenticatore
            Element legalAuthTimeElement = (Element) doc.getElementsByTagName("legalAuthenticator").item(0);
            data.put("authenticatorTime", formatDate(legalAuthTimeElement.getElementsByTagName("time")
                    .item(0).getTextContent()));
            data.put("authenticatorSignatureCode", legalAuthTimeElement.getElementsByTagName("signatureCode")
                    .item(0).getAttributes().getNamedItem("code").getNodeValue());

            // Estrazione dei dati da <componentOf>
            Element encompassingEncounter = (Element) doc.getElementsByTagName("encompassingEncounter").item(0);

// ID incontro e autorità assegnante
            data.put("encounterId", encompassingEncounter.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());
            data.put("encounterAuthorityName", encompassingEncounter.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("assigningAuthorityName").getNodeValue());

            // Date di inizio e fine incontro
            Element effectiveTimeElement = (Element) encompassingEncounter.getElementsByTagName("effectiveTime").item(0);
            data.put("encounterStart", formatDate(effectiveTimeElement.getElementsByTagName("low")
                    .item(0).getAttributes().getNamedItem("value").getNodeValue()));
            data.put("encounterEnd", formatDate(effectiveTimeElement.getElementsByTagName("high")
                    .item(0).getAttributes().getNamedItem("value").getNodeValue()));

            // Responsabile dell'incontro
            Element responsibleParty = (Element) encompassingEncounter.getElementsByTagName("responsibleParty").item(0);
            Element assignedEntity = (Element) responsibleParty.getElementsByTagName("assignedEntity").item(0);
            data.put("responsibleId", assignedEntity.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());
            data.put("responsibleAuthorityName", assignedEntity.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("assigningAuthorityName").getNodeValue());

            Element responsiblePerson = (Element) assignedEntity.getElementsByTagName("assignedPerson").item(0);
            data.put("responsibleGiven", getTextContentByTagName(responsiblePerson, "given"));
            data.put("responsibleFamily", getTextContentByTagName(responsiblePerson, "family"));
            data.put("responsiblePrefix", getTextContentByTagName(responsiblePerson, "prefix"));

            // Informazioni sulla struttura sanitaria
            Element location = (Element) encompassingEncounter.getElementsByTagName("location").item(0);
            Element healthCareFacility = (Element) location.getElementsByTagName("healthCareFacility").item(0);
            data.put("facilityName", getTextContentByTagName(healthCareFacility, "name"));

            Element serviceProviderOrg = (Element) healthCareFacility.getElementsByTagName("serviceProviderOrganization").item(0);
            data.put("serviceProviderName", getTextContentByTagName(serviceProviderOrg, "name"));
            data.put("serviceProviderId", serviceProviderOrg.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("extension").getNodeValue());
            data.put("serviceProviderAuthorityName", serviceProviderOrg.getElementsByTagName("id")
                    .item(0).getAttributes().getNamedItem("assigningAuthorityName").getNodeValue());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    private String formatDate(String rawDate) {
        try {
            // Prova il primo formato (incluso il fuso orario)
            SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyyMMddHHmmssZ");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = inputFormat1.parse(rawDate);
            return outputFormat.format(date);
        } catch (Exception e1) {
            try {
                // Prova il secondo formato (senza fuso orario)
                SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = inputFormat2.parse(rawDate);
                return outputFormat.format(date);
            } catch (Exception e2) {
                // Se entrambi i formati falliscono, restituisci un messaggio di errore
                return "Formato data non valido";
            }
        }
    }




    private String getTextContentByTagName(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    private String populateTemplate(String templateContent, Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            templateContent = templateContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return templateContent;
    }
}
