module com.cablemanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.cablemanagement to javafx.fxml;
    opens com.cablemanagement.views to javafx.fxml, javafx.graphics;
    opens com.cablemanagement.model to javafx.base, javafx.fxml;
    
    exports com.cablemanagement;
    exports com.cablemanagement.views;
    exports com.cablemanagement.model;
    exports com.cablemanagement.database;
}
