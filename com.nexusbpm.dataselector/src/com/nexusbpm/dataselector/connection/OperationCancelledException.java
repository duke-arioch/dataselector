package com.nexusbpm.dataselector.connection;

import java.sql.SQLException;

public class OperationCancelledException extends SQLException {
    private static final long serialVersionUID = 1l;
    
    public static final String DEFAULT_MESSAGE = "Operation cancelled.";
    
    public OperationCancelledException() {
    }
    
    public OperationCancelledException(String reason) {
        super(reason);
    }
    
    public OperationCancelledException(Throwable cause) {
        super();
        initCause(cause);
    }
    
    public OperationCancelledException(String reason, String SQLState) {
        super(reason, SQLState);
    }
    
    public OperationCancelledException(String reason, Throwable cause) {
        super(reason);
        initCause(cause);
    }
    
    public OperationCancelledException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public OperationCancelledException(String reason, String sqlState, Throwable cause) {
        super(reason, sqlState);
        initCause(cause);
    }
    
    public OperationCancelledException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode);
        initCause(cause);
    }
    
    /** Convenience method to create an exception with the default message. */
    public static OperationCancelledException create() {
        return new OperationCancelledException(DEFAULT_MESSAGE);
    }
    
    public static OperationCancelledException create(Throwable t) {
        return new OperationCancelledException(DEFAULT_MESSAGE, t);
    }
}
