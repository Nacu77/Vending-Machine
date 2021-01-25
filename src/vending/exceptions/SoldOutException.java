package vending.exceptions;

public class SoldOutException extends RuntimeException {

    public SoldOutException(String message) {
        super(message);
    }
}
