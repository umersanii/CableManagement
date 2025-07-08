package com.cablemanagement.database;

public interface db {

    // Sign In
    String connect(String url, String user, String password);

    void disconnect();

    boolean isConnected();

    Object executeQuery(String query);

    int executeUpdate(String query);

    boolean SignIn(String userId, String password);
}
