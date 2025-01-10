package edu.swen1.mtcg.utils;

public class CardMoverException extends Exception {
    public CardMoverException(String message) {
        super(message);
    }
    public CardMoverException(String message, Throwable cause) { super(message, cause); }
    public CardMoverException(Throwable cause) { super(cause); }
}



