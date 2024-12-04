module com.group01.dhsa {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires org.mongodb.driver.sync.client;
    requires jbcrypt;

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
}
