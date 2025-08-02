package com.cablemanagement;

/**
 * Launcher class to bootstrap JavaFX application.
 * This class doesn't extend Application and serves as the main entry point
 * to properly launch JavaFX applications from executable JARs.
 */
public class Launcher {
    public static void main(String[] args) {
        // Suppress Gdk-WARNING about XSetErrorHandler
        System.setProperty("jdk.gtk.version", "2");
        
        // Launch the JavaFX application
        App.main(args);
    }
}
