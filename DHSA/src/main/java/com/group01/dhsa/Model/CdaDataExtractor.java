package com.group01.dhsa.Model;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class CdaDataExtractor {

    public Map<String, String> extractCdaData(File cdaFile) {
        Map<String, String> data = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cdaFile);
            doc.getDocumentElement().normalize();

            // Estrarre le informazioni generali
            extractGeneralInfo(doc, data);

            // Estrarre le informazioni del paziente
            extractPatientInfo(doc, data);

            // Estrarre le informazioni sull'autore
            extractAuthorInfo(doc, data);

            // Estrarre le informazioni del custode
            extractCustodianInfo(doc, data);

            // Estrarre le informazioni sul legale autenticatore
            extractAuthenticatorInfo(doc, data);

            // Estrarre le informazioni sull'incontro
            extractEncounterInfo(doc, data);
            extractEncounterLocation(doc, data);
            extractResponsibleParty(doc,data);
            // Estrarre sezioni specifiche (es. Diagnosi di Accettazione)
            extractSectionDetails(doc, data, "46241-6", "LOINC" , "admission");
            extractSectionDetails(doc, data, "47039-3", "LOINC","clinicalOverview");
            extractSectionDetails(doc, data, "11329-0", "LOINC", "anamnesis");
            extractSectionDetails(doc, data, "42346-7","LOINC", "medications");
            extractSectionDetails(doc, data, "8648-8", "LOINC","hospitalCourse");
            extractSectionDetails(doc, data, "11493-4","LOINC","findings");
            extractSectionDetails(doc, data, "30954-2","LOINC","exam");
            extractSectionDetails(doc, data, "47519-4","LOINC", "procedures");
            extractSectionDetails(doc, data, "48765-2","LOINC","allergies");
            extractSectionDetails(doc, data, "11535-2","LOINC", "conditions");
            extractSectionDetails(doc, data, "18776-5","LOINC", "followup");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private void extractGeneralInfo(Document doc, Map<String, String> data) {
        Element clinicalDocument = doc.getDocumentElement();
        data.put("templateId", getAttributeValue(clinicalDocument, "templateId", "extension"));
        data.put("typeId", getAttributeValue(clinicalDocument, "typeId", "extension"));
        data.put("documentId", getAttributeValueOrDefault(clinicalDocument, "setId", "extension", "N/A"));
        data.put("assigningAuthorityName", getAttributeValueOrDefault(clinicalDocument, "id", "assigningAuthorityName", "N/A"));
        data.put("realmCode", getAttributeValue(clinicalDocument, "realmCode", "code"));
        data.put("documentType", getAttributeValue(clinicalDocument, "code", "displayName"));
        data.put("codeSystemName", getAttributeValue(clinicalDocument, "code", "codeSystemName"));
        data.put("confidentialityCode", getAttributeValue(clinicalDocument, "confidentialityCode", "displayName"));
        data.put("creationDate", formatDate(getAttributeValue(clinicalDocument, "effectiveTime", "value")));
        data.put("languageCode", getAttributeValue(clinicalDocument, "languageCode", "code"));
        data.put("versionNumber", getAttributeValue(clinicalDocument, "versionNumber", "value"));
    }

    private void extractPatientInfo(Document doc, Map<String, String> data) {
        Element patientRoleElement = (Element) doc.getElementsByTagName("patientRole").item(0);
        Element patientElement = (Element) patientRoleElement.getElementsByTagName("patient").item(0);
        Element addressElement = (Element) patientRoleElement.getElementsByTagName("addr").item(0);

        data.put("patientId", getAttributeValue(patientRoleElement, "id", "extension"));
        data.put("patientGiven", getTextContentByTagName(patientElement, "given"));
        data.put("patientFamily", getTextContentByTagName(patientElement, "family"));
        data.put("patientGender", getAttributeValue(patientElement, "administrativeGenderCode", "displayName"));
        data.put("patientBirthTime", getAttributeValue(patientElement, "birthTime", "value"));
        data.put("patientAddress", getTextContentByTagName(addressElement, "streetAddressLine"));
        data.put("patientCountry", getTextContentByTagName(addressElement, "country"));
        data.put("patientState", getTextContentByTagName(addressElement, "state"));
        data.put("patientCity", getTextContentByTagName(addressElement, "city"));
    }

    private void extractAuthorInfo(Document doc, Map<String, String> data) {
        // Seleziona il nodo <author>
        Element authorElement = (Element) doc.getElementsByTagName("author").item(0);
        if (authorElement != null) {
            // Estrai il valore del tempo dal nodo <time>
            Element timeElement = (Element) authorElement.getElementsByTagName("time").item(0);
            if (timeElement != null && timeElement.hasAttribute("value")) {
                String authorTime = formatDate(timeElement.getAttribute("value"));
                data.put("authorTime", authorTime);
                System.out.println("Author Time: " + authorTime);
            } else {
                data.put("authorTime", "N/A");
                System.out.println("Author Time not found");
            }

            // Seleziona il nodo <assignedAuthor> all'interno di <author>
            Element assignedAuthor = (Element) authorElement.getElementsByTagName("assignedAuthor").item(0);
            if (assignedAuthor != null) {
                // Estrai ID e Authority Name dal nodo <id>
                String authorId = getAttributeValue(assignedAuthor, "id", "extension");
                String assigningAuthorityName = getAttributeValue(assignedAuthor, "id", "assigningAuthorityName");

                data.put("authorId", authorId);
                data.put("assigningAuthorityName", assigningAuthorityName);

                System.out.println("Author ID: " + authorId);
                System.out.println("Assigning Authority Name: " + assigningAuthorityName);

                // Seleziona il nodo <assignedPerson> all'interno di <assignedAuthor>
                Element assignedPerson = (Element) assignedAuthor.getElementsByTagName("assignedPerson").item(0);
                if (assignedPerson != null) {
                    // Estrai il nome del <assignedPerson>
                    String authorGiven = getTextContentByTagName(assignedPerson, "given");
                    String authorFamily = getTextContentByTagName(assignedPerson, "family");

                    data.put("authorGiven", authorGiven);
                    data.put("authorFamily", authorFamily);

                    System.out.println("Author Given: " + authorGiven);
                    System.out.println("Author Family: " + authorFamily);
                } else {
                    data.put("authorGiven", "N/A");
                    data.put("authorFamily", "N/A");
                    System.out.println("Assigned Person not found");
                }
            } else {
                data.put("authorId", "N/A");
                data.put("assigningAuthorityName", "N/A");
                System.out.println("Assigned Author not found");
            }
        } else {
            // Valori predefiniti se il nodo <author> non esiste
            data.put("authorTime", "N/A");
            data.put("authorId", "N/A");
            data.put("assigningAuthorityName", "N/A");
            data.put("authorGiven", "N/A");
            data.put("authorFamily", "N/A");
            System.out.println("Author element not found");
        }
    }


    private void extractCustodianInfo(Document doc, Map<String, String> data) {
        Element custodianElement = (Element) doc.getElementsByTagName("representedCustodianOrganization").item(0);
        if (custodianElement != null) {
            data.put("custodianName", getTextContentByTagName(custodianElement, "name"));
            data.put("custodianId", getAttributeValue(custodianElement, "id", "extension"));
            data.put("custodianAuthorityName", getAttributeValue(custodianElement, "id", "assigningAuthorityName"));
            Element custodianAddrElement = (Element) custodianElement.getElementsByTagName("addr").item(0);
            if (custodianAddrElement != null) {
                data.put("custodianStreet", getTextContentByTagName(custodianAddrElement, "streetAddressLine"));
                data.put("custodianCity", getTextContentByTagName(custodianAddrElement, "city"));
                data.put("custodianState", getTextContentByTagName(custodianAddrElement, "state"));
                data.put("custodianCountry", getTextContentByTagName(custodianAddrElement, "country"));
            }
        }
    }


    private void extractAuthenticatorInfo(Document doc, Map<String, String> data) {
        Element legalAuthElement = (Element) doc.getElementsByTagName("assignedEntity").item(0);
        if (legalAuthElement != null) {
            data.put("authenticatorId", getAttributeValue(legalAuthElement, "id", "extension"));
            data.put("authenticatorAuthorityName", getAttributeValue(legalAuthElement, "id", "assigningAuthorityName"));
            Element authPersonElement = (Element) legalAuthElement.getElementsByTagName("assignedPerson").item(0);
            if (authPersonElement != null) {
                data.put("authenticatorGiven", getTextContentByTagName(authPersonElement, "given"));
                data.put("authenticatorFamily", getTextContentByTagName(authPersonElement, "family"));
            }
            Element authOrgElement = (Element) legalAuthElement.getElementsByTagName("representedOrganization").item(0);
            if (authOrgElement != null) {
                data.put("authenticatorOrgName", getTextContentByTagName(authOrgElement, "name"));
                data.put("authenticatorOrgTelecom", getAttributeValue(authOrgElement, "telecom", "value"));
            }
            Element authAddrElement = (Element) legalAuthElement.getElementsByTagName("addr").item(0);
            if (authAddrElement != null) {
                data.put("authenticatorStreet", getTextContentByTagName(authAddrElement, "streetAddressLine"));
                data.put("authenticatorCity", getTextContentByTagName(authAddrElement, "city"));
                data.put("authenticatorState", getTextContentByTagName(authAddrElement, "state"));
                data.put("authenticatorCountry", getTextContentByTagName(authAddrElement, "country"));
            }
            Element legalAuthTimeElement = (Element) doc.getElementsByTagName("legalAuthenticator").item(0);
            data.put("authenticatorTime", formatDate(legalAuthTimeElement.getElementsByTagName("time")
                    .item(0).getAttributes()
                    .getNamedItem("value")
                    .getNodeValue()));
            data.put("authenticatorSignatureCode", legalAuthTimeElement.getElementsByTagName("signatureCode")
                    .item(0).getAttributes().getNamedItem("code").getNodeValue());
        }
    }

    private void extractResponsibleParty(Document doc, Map<String, String> data) {
        Element responsiblePartyElement = (Element) doc.getElementsByTagName("responsibleParty").item(0);
        if (responsiblePartyElement != null) {
            Element assignedEntity = (Element) responsiblePartyElement.getElementsByTagName("assignedEntity").item(0);
            if (assignedEntity != null) {
                data.put("responsibleId", getAttributeValue(assignedEntity, "id", "extension"));
                data.put("responsibleAuthorityName", getAttributeValue(assignedEntity, "id", "assigningAuthorityName"));
                Element assignedPerson = (Element) assignedEntity.getElementsByTagName("assignedPerson").item(0);
                if (assignedPerson != null) {
                    data.put("responsibleGiven", getTextContentByTagName(assignedPerson, "given"));
                    data.put("responsibleFamily", getTextContentByTagName(assignedPerson, "family"));
                    data.put("responsiblePrefix", getTextContentByTagName(assignedPerson, "prefix"));
                }
            }
        }
    }


    private void extractEncounterInfo(Document doc, Map<String, String> data) {
        Element encompassingEncounter = (Element) doc.getElementsByTagName("encompassingEncounter").item(0);
        data.put("encounterId", getAttributeValue(encompassingEncounter, "id", "extension"));
        data.put("encounterAuthorityName", getAttributeValue(encompassingEncounter, "id", "assigningAuthorityName"));
        data.put("encounterStart", formatDate(getAttributeValue(encompassingEncounter, "low", "value")));
        data.put("encounterEnd", formatDate(getAttributeValue(encompassingEncounter, "high", "value")));
    }
    private void extractEncounterLocation(Document doc, Map<String, String> data) {
        Element locationElement = (Element) doc.getElementsByTagName("healthCareFacility").item(0);
        if (locationElement != null) {
            data.put("facilityName", getTextContentByTagName(locationElement, "location"));
            Element serviceProvider = (Element) locationElement.getElementsByTagName("serviceProviderOrganization").item(0);
            if (serviceProvider != null) {
                data.put("serviceProviderId", getAttributeValue(serviceProvider, "id", "extension"));
                data.put("serviceProviderName", getTextContentByTagName(serviceProvider, "name"));
                data.put("serviceProviderAuthorityName", getAttributeValue(serviceProvider, "id", "assigningAuthorityName"));
            }
        }
    }


    private void extractSectionDetails(Document doc, Map<String, String> data, String code, String codeSystemName, String prefix) {
        Element section = getSectionByCode(doc, code);
        if (section != null) {
            // Estrarre i codici di sistema e i dettagli
            data.put(prefix + "Code", getAttributeValue(section, "code", "code"));
            data.put(prefix + "CodeSystemName", getAttributeValue(section, "code", "codeSystemName"));

            // Estrarre i dettagli della sezione <text>
            String sectionText = extractSectionText(section);
            if (code.equals("30954-2")) { // Se è la sezione "Esami diagnostici e/o di laboratorio significativi"
                sectionText = extractTable(section); // Gestione speciale per includere la tabella
            }

            if (sectionText.isEmpty()) {
                sectionText = "<p>No details available.</p>";
            }
            data.put(prefix + "Details", sectionText);
        } else {
            // Se la sezione non è presente, riempire con valori predefiniti
            data.put(prefix + "Code", code);
            data.put(prefix + "CodeSystemName", codeSystemName);
            data.put(prefix + "Details", "<p>No details available.</p>");
        }
    }

    private String extractTable(Element section) {
        StringBuilder tableBuilder = new StringBuilder();

        // Cerca il nodo <table> nella sezione <text>
        NodeList tableNodes = section.getElementsByTagName("table");
        if (tableNodes.getLength() > 0) {
            Element table = (Element) tableNodes.item(0);

            // Costruire l'intestazione della tabella
            tableBuilder.append("<table border='1'>");
            NodeList headerRows = table.getElementsByTagName("thead");
            if (headerRows.getLength() > 0) {
                Element thead = (Element) headerRows.item(0);
                tableBuilder.append("<thead>");
                NodeList headerCells = thead.getElementsByTagName("td");
                tableBuilder.append("<tr>");
                for (int i = 0; i < headerCells.getLength(); i++) {
                    tableBuilder.append("<th>").append(headerCells.item(i).getTextContent().trim()).append("</th>");
                }
                tableBuilder.append("</tr>");
                tableBuilder.append("</thead>");
            }

            // Costruire il corpo della tabella
            NodeList bodyRows = table.getElementsByTagName("tbody");
            if (bodyRows.getLength() > 0) {
                Element tbody = (Element) bodyRows.item(0);
                tableBuilder.append("<tbody>");
                NodeList rows = tbody.getElementsByTagName("tr");
                for (int i = 0; i < rows.getLength(); i++) {
                    tableBuilder.append("<tr>");
                    NodeList cells = ((Element) rows.item(i)).getElementsByTagName("td");
                    for (int j = 0; j < cells.getLength(); j++) {
                        tableBuilder.append("<td>").append(cells.item(j).getTextContent().trim()).append("</td>");
                    }
                    tableBuilder.append("</tr>");
                }
                tableBuilder.append("</tbody>");
            }

            tableBuilder.append("</table>");
        }

        return tableBuilder.toString();
    }




    private String getAttributeValue(Element parent, String tagName, String attributeName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getAttributes() != null) {
                Node attributeNode = node.getAttributes().getNamedItem(attributeName);
                return attributeNode != null ? attributeNode.getNodeValue() : "N/A";
            }
        }
        return "N/A"; // Valore di fallback se il tag o l'attributo non esiste
    }

    private String getAttributeValueOrDefault(Element parent, String tagName, String attributeName, String defaultValue) {
        Element element = (Element) parent.getElementsByTagName(tagName).item(0);
        return element != null && element.hasAttribute(attributeName) ? element.getAttribute(attributeName) : defaultValue;
    }

    private String extractSectionText(Element section) {
        StringBuilder detailsBuilder = new StringBuilder();
        NodeList children = section.getElementsByTagName("text").item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeName().equals("paragraph")) {
                detailsBuilder.append("<p>").append(node.getTextContent().trim()).append("</p>");
            } else if (node.getNodeName().equals("list")) {
                detailsBuilder.append("<ul>");
                NodeList items = ((Element) node).getElementsByTagName("item");
                for (int j = 0; j < items.getLength(); j++) {
                    detailsBuilder.append("<li>").append(items.item(j).getTextContent().trim()).append("</li>");
                }
                detailsBuilder.append("</ul>");
            }
        }
        return detailsBuilder.toString();
    }

    private String getTextContentByTagName(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "N/A";
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
                try {
                    // Prova il secondo formato (senza fuso orario)
                    SimpleDateFormat inputFormat3 = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = inputFormat3.parse(rawDate);
                    return outputFormat.format(date);
                } catch (Exception e3) {
                    // Se entrambi i formati falliscono, restituisci un messaggio di errore
                    return "Formato data non valido";
                }
            }
        }
    }

    private Element getSectionByCode(Document doc, String code) {
        NodeList sections = doc.getElementsByTagName("section");
        for (int i = 0; i < sections.getLength(); i++) {
            Element section = (Element) sections.item(i);
            Element codeElement = (Element) section.getElementsByTagName("code").item(0);
            if (codeElement != null && codeElement.getAttribute("code").equals(code)) {
                return section;
            }
        }
        return null;
    }
}
