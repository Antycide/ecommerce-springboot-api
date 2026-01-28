package org.example.ecommerce.Exception;

public class NoProductsInWishlistException extends RuntimeException {
    public NoProductsInWishlistException(String message) {
        super(message);
    }
}
