package com.gdb.exceptions;

/**
 * Exception thrown when an operation is attempted on a suspended or closed account.
 */
public class InactiveAccountException extends AccountException {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message
     */
    public InactiveAccountException(String message) {
        super(message);
    }
}
