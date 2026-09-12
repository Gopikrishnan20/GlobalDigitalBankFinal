package com.gdb.exceptions;

/**
 * Exception thrown when a withdrawal would breach minimum balance requirements.
 */
public class MinimumBalanceViolationException extends AccountException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message
     */
    public MinimumBalanceViolationException(String message) {
        super(message);
    }
}
