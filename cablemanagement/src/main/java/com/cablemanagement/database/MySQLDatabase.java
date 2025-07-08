package com.cablemanagement.database;

public class MySQLDatabase implements db {

    @Override
    public String connect(String url, String user, String password) {
        // Implement here
        return "null";
    }

    @Override
    public void disconnect() {
        // Implement here
    }

    @Override
    public boolean isConnected() {
        // Implement here
        return false;
    }

    @Override
    public Object executeQuery(String query) {

        return null;
    }

    @Override
    public int executeUpdate(String query) {
        

        return -1;
    }

    @Override
    public boolean SignIn(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'SignIn'");
        // return false; // This line is unreachable after the exception
    }
}
