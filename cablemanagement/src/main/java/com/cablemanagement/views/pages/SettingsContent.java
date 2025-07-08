package com.cablemanagement.views.pages;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class SettingsContent {
    public static Node get() {
        Label label = new Label("⚙️ Settings Page");
        label.setFont(new Font(24));
        return new StackPane(label);
    }
}
