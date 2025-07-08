module com.cablemanagement {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.cablemanagement to javafx.fxml;
    opens com.cablemanagement.views to javafx.fxml, javafx.graphics;
    
    exports com.cablemanagement;
    exports com.cablemanagement.views;

    exports com.cablemanagement.database;
}
