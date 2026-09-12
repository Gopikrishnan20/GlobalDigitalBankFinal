package com.gdb.exceptions;

/**
 * Exception thrown when an incorrect PIN is entered for authentication.
 */
public class InvalidPinException extends AccountException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message
     */
    public InvalidPinException(String message) {
        super(message);
    }
}
