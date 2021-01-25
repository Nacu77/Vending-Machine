package vending.exceptions;

public class NotFullPaidException extends RuntimeException {

    public NotFullPaidException(String message) {
        super(message);
    }
}
