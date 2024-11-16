module com.group01.dhsa {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.group01.dhsa to javafx.fxml;
    exports com.group01.dhsa;
}