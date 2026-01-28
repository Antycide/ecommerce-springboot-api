package org.example.ecommerce.Exception;

public class AddressDoesNotMatchUserException extends RuntimeException {
    public AddressDoesNotMatchUserException(String message) {
        super(message);
    }
}
