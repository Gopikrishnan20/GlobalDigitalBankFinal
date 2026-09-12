package com.gdb.exceptions;

/**
 * Base checked exception class for all account-related errors.
 * All banking operation exceptions should extend this class.
 */
public class AccountException extends Exception {
    
    /**
     * Constructor that accepts an error message.
     * 
     * @param message The error message describing the exception
     */
    public AccountException(String message) {
        super(message);
    }
}
