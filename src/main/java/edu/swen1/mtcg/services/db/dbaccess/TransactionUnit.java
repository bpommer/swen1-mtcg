package edu.swen1.mtcg.services.db.dbaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionUnit implements AutoCloseable {

    private Connection connection;
    public TransactionUnit() {
        this.connection = DbCredentials.INSTANCE.getConnection();
        try {
            this.connection.setAutoCommit(false);
        }
        catch (SQLException e) {
            throw new DbAccessException("Autocommit cannot be deactivated", e);

        }

    }

    public void dbCommit() {

        if(this.connection != null) {
            try {
                this.connection.commit();
            } catch (SQLException e) {
                throw new DbAccessException("Transaction commit failed", e);

            }
        }
    }
    public void dbRollback() {
        if(this.connection != null) {
            try {
                this.connection.rollback();

            } catch (SQLException e) {
                throw new DbAccessException("Transaction rollback failed", e);
            }
        }
    }

    public void dbFinish() {
        if(this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException e) {
                throw new DbAccessException("Connection could not get closed", e);
            }
        }

    }

    public PreparedStatement prepareStatement(String sql) {
        if(this.connection != null) {
            try {
                return this.connection.prepareStatement(sql);
            } catch (SQLException e) {
                throw new DbAccessException("Prepare statement failed", e);
            }
        }
        throw new DbAccessException("No active connection");
    }

    public void close() throws Exception {
        this.dbFinish();
    }


}
