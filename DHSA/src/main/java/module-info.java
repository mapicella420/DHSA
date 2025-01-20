module com.group01.dhsa {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires org.mongodb.driver.sync.client;
    requires jbcrypt;
    requires hapi.fhir.base;
    requires org.hl7.fhir.r5;
    requires org.apache.commons.csv;
    requires org.hl7.fhir.utilities;
    requires jakarta.xml.bind;
    requires com.sun.xml.bind;


    // Rende visibile il pacchetto principale (necessario per la classe MyHL7CdaConverter)
    exports com.group01.dhsa;

    // Rende visibile il pacchetto Model (se usato esternamente da altri moduli)
    exports com.group01.dhsa.Model to javafx.graphics;

    // Permette l'accesso tramite riflessione al pacchetto Model
    opens com.group01.dhsa.Model to javafx.fxml;

    // Rende visibile il pacchetto Controller (usato da JavaFX per i controller FXML)
    exports com.group01.dhsa.Controller;

    // Permette l'accesso tramite riflessione al pacchetto Controller
    opens com.group01.dhsa.Controller to javafx.fxml;
    exports com.group01.dhsa.ObserverPattern to javafx.graphics;
    opens com.group01.dhsa.ObserverPattern to javafx.fxml;
    exports com.group01.dhsa.Model.CDAResources to javafx.graphics;
    opens com.group01.dhsa.Model.CDAResources to javafx.fxml;
    exports com.group01.dhsa.Model.CDAResources.SectionModels to javafx.graphics;
    opens com.group01.dhsa.Model.CDAResources.SectionModels to jakarta.xml.bind, javafx.fxml, com.sun.xml.bind;
    // Rende visibile il pacchetto ClassXML per JAXB
    exports com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML to javafx.graphics;
    opens com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML to jakarta.xml.bind, javafx.fxml, com.sun.xml.bind;
    opens com.group01.dhsa to javafx.fxml;

}
