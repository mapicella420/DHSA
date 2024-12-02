module com.group01.dhsa {
    requires javafx.controls;
    requires javafx.fxml;


    exports com.group01.dhsa.Model to javafx.graphics;
    opens com.group01.dhsa.Model to javafx.fxml;
    exports com.group01.dhsa.Controller;
    opens com.group01.dhsa.Controller to javafx.fxml;
}