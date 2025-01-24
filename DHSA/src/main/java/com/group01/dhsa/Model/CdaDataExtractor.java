package com.group01.dhsa.Model;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class CdaDataExtractor {

    /**
     * Analizza il file CDA e restituisce i dati estratti.
     *
     * @param cdaFile File XML del CDA.
     * @return Mappa dei dati estratti.
     */
    public Map<String, String> extractCdaData(File cdaFile) {
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

            // ID Documento
            Element setIdElement = (Element) clinicalDocument.getElementsByTagName("setId").item(0);
            if (setIdElement != null) {
                data.put("documentId", setIdElement.getAttribute("extension"));
            } else {
                data.put("documentId", "N/A");
            }

            // Authority Name
            Element idElement = (Element) clinicalDocument.getElementsByTagName("id").item(0);
            if (idElement != null) {
                data.put("assigningAuthorityName", idElement.getAttribute("assigningAuthorityName"));
            } else {
                data.put("assigningAuthorityName", "N/A");
            }

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

            if (authorElement != null) {
                // Nome dell'autore
                Element authorNameElement = (Element) authorElement.getElementsByTagName("name").item(0);
                if (authorNameElement != null) {
                    data.put("authorGiven", getTextContentByTagName(authorNameElement, "given"));
                    data.put("authorFamily", getTextContentByTagName(authorNameElement, "family"));
                } else {
                    data.put("authorGiven", "N/A");
                    data.put("authorFamily", "N/A");
                }

                // ID Autore
                Element idElement2 = (Element) authorElement.getElementsByTagName("id").item(0);
                if (idElement2 != null) {
                    data.put("authorId", idElement2.getAttribute("extension"));
                    data.put("assigningAuthorityName", idElement2.getAttribute("assigningAuthorityName"));
                } else {
                    data.put("authorId", "N/A");
                    data.put("assigningAuthorityName", "N/A");
                }
            }

// Timestamp dell'autore
            Element authorTimeElement = (Element) doc.getElementsByTagName("author").item(0);
            if (authorTimeElement != null) {
                Element timeElement = (Element) authorTimeElement.getElementsByTagName("time").item(0);
                if (timeElement != null && timeElement.hasAttribute("value")) {
                    String rawDate = timeElement.getAttribute("value");
                    data.put("authorTime", formatDate(rawDate));
                } else {
                    data.put("authorTime", "N/A");
                }
            } else {
                data.put("authorTime", "N/A");
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


            // Estrarre i dati relativi alla Diagnosi di Accettazione
            Element admissionSection = getSectionByCode(doc, "46241-6");

            if (admissionSection != null) {
                // Estrarre i dettagli del codice
                Element codeElement = (Element) admissionSection.getElementsByTagName("code").item(0);
                if (codeElement != null) {
                    data.put("admissionCode", codeElement.hasAttribute("code") ? codeElement.getAttribute("code") : "N/A");
                    data.put("admissionCodeSystem", codeElement.hasAttribute("codeSystem") ? codeElement.getAttribute("codeSystem") : "N/A");
                    data.put("admissionCodeSystemName", codeElement.hasAttribute("codeSystemName") ? codeElement.getAttribute("codeSystemName") : "N/A");
                } else {
                    data.put("admissionCode", "N/A");
                    data.put("admissionCodeSystem", "N/A");
                    data.put("admissionCodeSystemName", "N/A");
                }

                // Estrarre i dettagli del testo
                Element textElement = (Element) admissionSection.getElementsByTagName("text").item(0);
                if (textElement != null) {
                    StringBuilder detailsBuilder = new StringBuilder();
                    NodeList children = textElement.getChildNodes();

                    for (int i = 0; i < children.getLength(); i++) {
                        Node node = children.item(i);

                        // Gestire paragrafi
                        if (node.getNodeName().equals("paragraph")) {
                            String value = node.getTextContent().trim();
                            if (!value.isEmpty()) {
                                detailsBuilder.append("<p>").append(value).append("</p>");
                            }
                        }

                        // Gestire liste
                        if (node.getNodeName().equals("list")) {
                            Element listElement = (Element) node;
                            NodeList items = listElement.getElementsByTagName("item");

                            if (items.getLength() > 0) {
                                detailsBuilder.append("<ul>"); // Lista non ordinata
                                for (int j = 0; j < items.getLength(); j++) {
                                    String itemValue = items.item(j).getTextContent().trim();
                                    if (!itemValue.isEmpty()) {
                                        detailsBuilder.append("<li>").append(itemValue).append("</li>");
                                    }
                                }
                                detailsBuilder.append("</ul>");
                            }
                        }
                    }

                    // Inserire i dettagli nella mappa dati
                    if (!detailsBuilder.isEmpty()) {
                        data.put("admissionDetails", detailsBuilder.toString());
                    } else {
                        data.put("admissionDetails", "<p>No additional details available.</p>");
                    }
                } else {
                    data.put("admissionDetails", "<p>No details available.</p>");
                }
            } else {
                data.put("admissionCode", "N/A");
                data.put("admissionCodeSystem", "N/A");
                data.put("admissionCodeSystemName", "N/A");
                data.put("admissionDetails", "<p>No details available.</p>");
            }


            // Estrarre i dati relativi all'inquadramento clinico iniziale
            Element clinicalOverviewSection = getSectionByCode(doc, "47039-3");

            if (clinicalOverviewSection != null) {
                // Estrarre dettagli della sezione principale
                Element codeElement = (Element) clinicalOverviewSection.getElementsByTagName("code").item(0);
                if (codeElement != null) {
                    data.put("clinicalOverviewCode", codeElement.hasAttribute("code") ? codeElement.getAttribute("code") : "N/A");
                    data.put("clinicalOverviewCodeSystem", codeElement.hasAttribute("codeSystem") ? codeElement.getAttribute("codeSystem") : "N/A");
                    data.put("clinicalOverviewCodeSystemName", codeElement.hasAttribute("codeSystemName") ? codeElement.getAttribute("codeSystemName") : "N/A");
                } else {
                    data.put("clinicalOverviewCode", "N/A");
                    data.put("clinicalOverviewCodeSystem", "N/A");
                    data.put("clinicalOverviewCodeSystemName", "N/A");
                }

                // Estrarre dettagli testuali (paragrafi e liste)
                Element textElement = (Element) clinicalOverviewSection.getElementsByTagName("text").item(0);
                if (textElement != null) {
                    StringBuilder detailsBuilder = new StringBuilder();
                    NodeList children = textElement.getChildNodes();

                    for (int i = 0; i < children.getLength(); i++) {
                        Node node = children.item(i);

                        // Gestire paragrafi
                        if (node.getNodeName().equals("paragraph")) {
                            String value = node.getTextContent().trim();
                            if (!value.isEmpty()) {
                                detailsBuilder.append("<p>").append(value).append("</p>");
                            }
                        }

                        // Gestire liste
                        if (node.getNodeName().equals("list")) {
                            Element listElement = (Element) node;
                            NodeList items = listElement.getElementsByTagName("item");

                            if (items.getLength() > 0) {
                                detailsBuilder.append("<ul>"); // Lista non ordinata
                                for (int j = 0; j < items.getLength(); j++) {
                                    String itemValue = items.item(j).getTextContent().trim();
                                    if (!itemValue.isEmpty()) {
                                        detailsBuilder.append("<li>").append(itemValue).append("</li>");
                                    }
                                }
                                detailsBuilder.append("</ul>");
                            }
                        }
                    }

                    // Aggiungere dettagli alla mappa
                    data.put("clinicalOverviewDetails", detailsBuilder.toString());
                } else {
                    data.put("clinicalOverviewDetails", "<p>No details available.</p>");
                }


                // Estrarre "Anamnesi Generale"
                Element anamnesisSection = getSectionByCode(doc, "11329-0");
                if (anamnesisSection != null) {
                        // Estrarre i codici dalla sezione <code>
                        Element codeElement2 = (Element) anamnesisSection.getElementsByTagName("code").item(0);
                        if (codeElement2 != null) {
                            data.put("anamnesisCode", codeElement2.hasAttribute("code") ? codeElement2.getAttribute("code") : "11329-0");
                            data.put("anamnesisCodeSystem", codeElement2.hasAttribute("codeSystem") ? codeElement2.getAttribute("codeSystem") : "2.16.840.1.113883.6.1");
                            data.put("anamnesisDisplayName", codeElement2.hasAttribute("codeSystemName") ? codeElement2.getAttribute("codeSystemName") : "LOINC");
                        } else {
                            // Fallback per i codici mancanti
                            data.put("anamnesisCode", "11329-0");
                            data.put("anamnesisCodeSystem", "2.16.840.1.113883.6.1");
                            data.put("anamnesisDisplayName", "LOINC");
                        }
                    // Estrarre il testo
                    Element anamnesisText = (Element) anamnesisSection.getElementsByTagName("text").item(0);
                    if (anamnesisText != null) {
                        StringBuilder anamnesisDetails = new StringBuilder();
                        NodeList anamnesisChildren = anamnesisText.getChildNodes();

                        for (int i = 0; i < anamnesisChildren.getLength(); i++) {
                            Node node = anamnesisChildren.item(i);

                            // Gestire i paragrafi nel formato <paragraph>
                            if (node.getNodeName().equals("paragraph")) {
                                String value = node.getTextContent().trim();
                                if (!value.isEmpty()) {
                                    anamnesisDetails.append("<p>").append(value).append("</p>");
                                }
                            }

                            // Gestire le liste
                            if (node.getNodeName().equals("list")) {
                                Element listElement = (Element) node;
                                NodeList items = listElement.getElementsByTagName("item");

                                if (items.getLength() > 0) {
                                    anamnesisDetails.append("<ul>");
                                    for (int j = 0; j < items.getLength(); j++) {
                                        String itemValue = items.item(j).getTextContent().trim();
                                        if (!itemValue.isEmpty()) {
                                            anamnesisDetails.append("<li>").append(itemValue).append("</li>");
                                        }
                                    }
                                    anamnesisDetails.append("</ul>");
                                }
                            }
                        }

                        // Inserire i dettagli nella mappa dati
                        if (anamnesisDetails.length() > 0) {
                            data.put("anamnesisDetails", anamnesisDetails.toString());
                        } else {
                            data.put("anamnesisDetails", "<p>No Anamnesis details available.</p>");
                        }
                    } else {
                        data.put("anamnesisDetails", "<p>No details available.</p>");
                    }
                } else {
                    data.put("anamnesisDetails", "<p>No Anamnesis details available.</p>");
                }


                // Estrarre "Terapia Farmacologica all’Ingresso"
                Element medicationsSection = getSectionByCode(doc, "42346-7");
                if (medicationsSection != null) {
                    // Estrarre i codici dalla sezione <code>
                    Element codeElement2 = (Element) medicationsSection.getElementsByTagName("code").item(0);
                    if (codeElement2 != null) {
                        data.put("medicationsCode", codeElement2.hasAttribute("code") ? codeElement2.getAttribute("code") : "N/A");
                        data.put("medicationsCodeSystem", codeElement2.hasAttribute("codeSystem") ? codeElement2.getAttribute("codeSystem") : "N/A");
                        data.put("medicationsCodeDisplayName", codeElement2.hasAttribute("codeSystemName") ? codeElement2.getAttribute("codeSystemName") : "N/A");
                    } else {
                        // Fallback per i codici mancanti
                        data.put("medicationsCode", "42346-7");
                        data.put("medicationsCodeSystem", "2.16.840.1.113883.6.1");
                        data.put("medicationsCodeDisplayName", "LOINC");
                    }

                    // Estrarre il contenuto del testo
                    Element medicationsText = (Element) medicationsSection.getElementsByTagName("text").item(0);
                    if (medicationsText != null) {
                        StringBuilder medicationsDetails = new StringBuilder();
                        NodeList medicationParagraphs = medicationsText.getChildNodes();

                        for (int i = 0; i < medicationParagraphs.getLength(); i++) {
                            Node node = medicationParagraphs.item(i);

                            // Gestire i paragrafi nel formato <paragraph>
                            if (node.getNodeName().equals("paragraph")) {
                                String value = node.getTextContent().trim();
                                if (!value.isEmpty()) {
                                    medicationsDetails.append("<p>").append(value).append("</p>");
                                }
                            }
                        }

                        // Aggiungere i dettagli alla mappa dati
                        if (medicationsDetails.length() > 0) {
                            data.put("medicationsDetails", medicationsDetails.toString());
                        } else {
                            data.put("medicationsDetails", "<p>No medications available.</p>");
                        }
                    } else {
                        data.put("medicationsDetails", "<p>No medications available.</p>");
                    }
                } else {
                    // Sezione non presente
                    data.put("medicationsCode", "N/A");
                    data.put("medicationsCodeSystem", "N/A");
                    data.put("medicationsDisplayName", "N/A");
                    data.put("medicationsDetails", "<p>No medications available.</p>");
                }



            } else {
                data.put("clinicalOverviewDetails", "<p>No clinical overview available.</p>");
                data.put("anamnesisDetails", "<p>No Anamnesis details available.</p>");
                data.put("medicationsDetails", "<p>No medications available.</p>");
            }

            // Estrarre i dati relativi al Decorso Ospedaliero
            Element hospitalCourseSection = getSectionByCode(doc, "8648-8");

            if (hospitalCourseSection != null) {
                // Estrarre i dettagli del codice
                Element codeElement = (Element) hospitalCourseSection.getElementsByTagName("code").item(0);
                if (codeElement != null) {
                    data.put("hospitalCourseCode", codeElement.hasAttribute("code") ? codeElement.getAttribute("code") : "8648-8");
                    data.put("hospitalCourseCodeSystem", codeElement.hasAttribute("codeSystem") ? codeElement.getAttribute("codeSystem") : "2.16.840.1.113883.6.1");
                    data.put("hospitalCourseCodeSystemName", codeElement.hasAttribute("codeSystemName") ? codeElement.getAttribute("codeSystemName") : "LOINC");
                } else {
                    data.put("hospitalCourseCode", "8648-8");
                    data.put("hospitalCourseCodeSystem", "2.16.840.1.113883.6.1");
                    data.put("hospitalCourseCodeSystemName", "LOINC");
                }

                // Estrarre i dettagli del testo
                Element textElement = (Element) hospitalCourseSection.getElementsByTagName("text").item(0);
                if (textElement != null) {
                    StringBuilder detailsBuilder = new StringBuilder();
                    NodeList children = textElement.getChildNodes();

                    for (int i = 0; i < children.getLength(); i++) {
                        Node node = children.item(i);

                        // Gestire paragrafi
                        if (node.getNodeName().equals("paragraph")) {
                            String value = node.getTextContent().trim();
                            if (!value.isEmpty()) {
                                detailsBuilder.append("<p>").append(value).append("</p>");
                            }
                        }

                        // Gestire liste
                        if (node.getNodeName().equals("list")) {
                            Element listElement = (Element) node;
                            NodeList items = listElement.getElementsByTagName("item");

                            if (items.getLength() > 0) {
                                detailsBuilder.append("<ul>"); // Lista non ordinata
                                for (int j = 0; j < items.getLength(); j++) {
                                    String itemValue = items.item(j).getTextContent().trim();
                                    if (!itemValue.isEmpty()) {
                                        detailsBuilder.append("<li>").append(itemValue).append("</li>");
                                    }
                                }
                                detailsBuilder.append("</ul>");
                            }
                        }
                    }

                    // Inserire i dettagli nella mappa dati
                    if (!detailsBuilder.isEmpty()) {
                        data.put("hospitalCourseDetails", detailsBuilder.toString());
                    } else {
                        data.put("hospitalCourseDetails", "<p>No hospital course details available.</p>");
                    }
                } else {
                    data.put("hospitalCourseDetails", "<p>No details available.</p>");
                }
            } else {
                data.put("hospitalCourseDetails", "<p>No hospital course available.</p>");
            }

            // Estrarre i dati relativi ai Riscontri ed Accertamenti Significativi
            Element findingsSection = getSectionByCode(doc, "11493-4");

            if (findingsSection != null) {
                // Estrarre i dettagli del codice
                Element codeElement = (Element) findingsSection.getElementsByTagName("code").item(0);
                if (codeElement != null) {
                    data.put("findingsCode", codeElement.hasAttribute("code") ? codeElement.getAttribute("code") : "11493-4");
                    data.put("findingsCodeSystem", codeElement.hasAttribute("codeSystem") ? codeElement.getAttribute("codeSystem") : "2.16.840.1.113883.6.1");
                    data.put("findingsCodeSystemName", codeElement.hasAttribute("codeSystemName") ? codeElement.getAttribute("codeSystemName") : "LOINC");
                }

                // Estrarre i dettagli testuali (paragrafi e liste)
                Element textElement = (Element) findingsSection.getElementsByTagName("text").item(0);
                if (textElement != null) {
                    StringBuilder detailsBuilder = new StringBuilder();
                    NodeList children = textElement.getChildNodes();

                    for (int i = 0; i < children.getLength(); i++) {
                        Node node = children.item(i);

                        // Gestire paragrafi
                        if (node.getNodeName().equals("paragraph")) {
                            String value = node.getTextContent().trim();
                            if (!value.isEmpty()) {
                                detailsBuilder.append("<p>").append(value).append("</p>");
                            }
                        }

                        // Gestire liste
                        if (node.getNodeName().equals("list")) {
                            Element listElement = (Element) node;
                            NodeList items = listElement.getElementsByTagName("item");

                            if (items.getLength() > 0) {
                                detailsBuilder.append("<ul>"); // Lista non ordinata
                                for (int j = 0; j < items.getLength(); j++) {
                                    String itemValue = items.item(j).getTextContent().trim();
                                    if (!itemValue.isEmpty()) {
                                        detailsBuilder.append("<li>").append(itemValue).append("</li>");
                                    }
                                }
                                detailsBuilder.append("</ul>");
                            }
                        }
                    }
                    if (!detailsBuilder.isEmpty()) {
                        data.put("findingsDetails", detailsBuilder.toString());
                    } else {
                        data.put("findingsDetails", "<p>No details available.</p>");
                    }
                }
            }
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

    private String getTextContentByTagName(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
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
