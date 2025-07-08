module com.cablemanagement {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.cablemanagement to javafx.fxml;
    exports com.cablemanagement;
}
