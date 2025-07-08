package com.cablemanagement.views.pages;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class RegisterContent {
    public static Node get() {
        Label label = new Label("📝 Register Page");
        label.setFont(new Font(24));
        return new StackPane(label);
    }
}
