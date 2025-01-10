package edu.swen1.mtcg.services.db.dbaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public enum DbCredentials {
    INSTANCE;
    private final String  connectionUrl = "jdbc:postgresql://localhost:5432/mtcgdb?user=webserver&password=crudaccess";
    public Connection getConnection() {

        try {
            return DriverManager.getConnection(connectionUrl);
        }
        catch (SQLException e) {
            throw new DbAccessException("Could not connect to database", e);
        }
    }

}
