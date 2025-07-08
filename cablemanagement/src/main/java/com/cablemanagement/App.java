package com.cablemanagement;

import com.cablemanagement.views.signin_page;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        // initalize database
        config.database.connect(null, null, null);

        signin_page.main(args);

    } 
}