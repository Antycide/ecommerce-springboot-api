package org.example.ecommerce.Exception;

public class OrderAlreadyCheckedOutException extends RuntimeException {
    public OrderAlreadyCheckedOutException(String message) {
        super(message);
    }
}
