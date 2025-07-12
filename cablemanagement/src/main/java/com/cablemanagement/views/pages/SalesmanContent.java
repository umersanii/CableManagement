package com.cablemanagement.views.pages;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class SalesmanContent {

    public static Node get() {
        // TODO Auto-generated method stub
        Label label = new Label("Salesman Content");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #333333; -fx-padding: 20px;");
        return label;
    }

}
