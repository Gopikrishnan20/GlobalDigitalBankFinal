package com.gdb.exceptions;

/**
 * Exception thrown when an account does not have sufficient balance for a withdrawal.
 */
public class InsufficientBalanceException extends AccountException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message
     */
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
