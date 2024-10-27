package edu.swen1.mtcg.services.db.dbaccess;




public class DbAccessException extends RuntimeException {

    // Pass exception to superclass RuntimeException
    public DbAccessException(String message) { super(message);}
    public DbAccessException(String message, Throwable cause) { super(message, cause); }
    public DbAccessException(Throwable cause) { super(cause); }
}
