package com.cablemanagement.views.pages;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class ProfileContent {
    public static Node get() {
        Label label = new Label("👤 Profile Page");
        label.setFont(new Font(24));
        return new StackPane(label);
    }
}
