package com.gdb.exceptions;

/**
 * Exception thrown when an invalid (zero or negative) amount is used in a transaction.
 */
public class InvalidAmountException extends AccountException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
